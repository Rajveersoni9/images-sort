#!/usr/bin/env python3
"""
MediaFlow AI — Live FastAPI Server & Web Application Version
"""

import os
import sys
import json
import asyncio
from pathlib import Path
from typing import List, Dict, Any, Optional

from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Query, BackgroundTasks
from fastapi.responses import HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

# Add current directory to path
sys.path.append(str(Path(__file__).parent.resolve()))
from sorter import MediaFlowSorter

app = FastAPI(
    title="MediaFlow AI",
    description="Real-time intelligent media sorting and neural indexing engine",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

sorter = MediaFlowSorter()

class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        for connection in list(self.active_connections):
            try:
                await connection.send_json(message)
            except Exception:
                self.disconnect(connection)

manager = ConnectionManager()

class ScanRequest(BaseModel):
    directory: str = "."

@app.get("/api/media")
def get_media():
    items = sorter.get_all_media()
    for item in items:
        if isinstance(item.get("ai_tags"), str):
            try:
                item["ai_tags"] = json.loads(item["ai_tags"])
            except Exception:
                item["ai_tags"] = []
    return {"count": len(items), "items": items}

@app.get("/api/search")
def search_media(q: str = Query(..., min_length=1)):
    results = sorter.search_media(q)
    for item in results:
        if isinstance(item.get("ai_tags"), str):
            try:
                item["ai_tags"] = json.loads(item["ai_tags"])
            except Exception:
                item["ai_tags"] = []
    return {"query": q, "count": len(results), "results": results}

@app.get("/api/albums")
def get_albums():
    import sqlite3
    with sqlite3.connect(sorter.db_path) as conn:
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM smart_albums ORDER BY item_count DESC")
        return [dict(row) for row in cursor.fetchall()]

@app.get("/api/duplicates")
def get_duplicates():
    import sqlite3
    with sqlite3.connect(sorter.db_path) as conn:
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM media_items WHERE is_duplicate = 1 ORDER BY duplicate_group_id")
        return [dict(row) for row in cursor.fetchall()]

@app.post("/api/scan")
async def trigger_scan(request: ScanRequest, background_tasks: BackgroundTasks):
    def run_scan():
        count = sorter.scan_directory(request.directory)
        asyncio.run(manager.broadcast({
            "event": "media_organized",
            "message": f"{count} new photos organized",
            "items_affected": count
        }))

    background_tasks.add_task(run_scan)
    return {"status": "started", "target_directory": request.directory}

@app.websocket("/ws/realtime")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        manager.disconnect(websocket)

# ---------------------------------------------------------------------------
# MEDIAFLOW AI — LIVE APPLICATION VERSION (WEB CLIENT)
# ---------------------------------------------------------------------------

@app.get("/", response_class=HTMLResponse)
def get_web_app():
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MediaFlow AI — Premium Media Organizer</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --primary: #7C3AED;
            --primary-gradient: linear-gradient(135deg, #7C3AED, #2563EB);
            --bg-color: #0B0F19;
            --surface-color: #131B2E;
            --surface-variant: #1E293B;
            --border-color: rgba(255, 255, 255, 0.08);
            --text-primary: #F8FAFC;
            --text-secondary: #94A3B8;
            --card-radius: 22px;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Plus Jakarta Sans', sans-serif;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-primary);
            overflow-x: hidden;
            padding-bottom: 100px;
        }

        /* Glassmorphic Container */
        .glass {
            background: rgba(19, 27, 46, 0.75);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--border-color);
            border-radius: var(--card-radius);
            box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
        }

        /* Top Arc-Style Search Bar */
        .top-nav {
            position: sticky;
            top: 0;
            z-index: 100;
            padding: 16px 24px;
            display: flex;
            align-items: center;
            gap: 16px;
            background: rgba(11, 15, 25, 0.85);
            backdrop-filter: blur(20px);
        }

        .search-container {
            flex: 1;
            display: flex;
            align-items: center;
            padding: 12px 20px;
            gap: 12px;
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            border-radius: 30px;
            transition: all 0.3s ease;
        }

        .search-container:focus-within {
            border-color: #7C3AED;
            box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.25);
        }

        .search-input {
            width: 100%;
            background: transparent;
            border: none;
            outline: none;
            color: var(--text-primary);
            font-size: 15px;
        }

        .search-input::placeholder {
            color: var(--text-secondary);
        }

        .icon-btn {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--text-primary);
            cursor: pointer;
            transition: transform 0.2s;
        }

        .icon-btn:hover {
            transform: scale(1.05);
        }

        .avatar-btn {
            background: var(--primary-gradient);
            color: white;
            border: none;
        }

        /* Floating Real-time Banner */
        .floating-banner {
            position: fixed;
            top: 80px;
            left: 50%;
            transform: translateX(-50%) translateY(-30px);
            opacity: 0;
            visibility: hidden;
            z-index: 200;
            padding: 10px 20px;
            border-radius: 30px;
            background: #131B2E;
            border: 1px solid #7C3AED;
            box-shadow: 0 10px 25px rgba(124, 58, 237, 0.4);
            display: flex;
            align-items: center;
            gap: 12px;
            transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
        }

        .floating-banner.show {
            transform: translateX(-50%) translateY(0);
            opacity: 1;
            visibility: visible;
        }

        .pulse-icon {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: var(--primary-gradient);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            animation: spin 6s linear infinite;
        }

        @keyframes spin {
            100% { transform: rotate(360deg); }
        }

        /* Density & Controls Bar */
        .controls-bar {
            padding: 8px 24px 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .density-toggle {
            display: flex;
            gap: 4px;
            background: var(--surface-color);
            padding: 4px;
            border-radius: 12px;
            border: 1px solid var(--border-color);
        }

        .density-btn {
            padding: 4px 10px;
            border-radius: 8px;
            border: none;
            background: transparent;
            color: var(--text-secondary);
            font-weight: 600;
            font-size: 12px;
            cursor: pointer;
        }

        .density-btn.active {
            background: var(--primary);
            color: white;
        }

        /* Gallery Grid */
        .gallery-grid {
            display: grid;
            gap: 14px;
            padding: 0 24px;
            transition: all 0.3s ease;
        }

        .cols-2 { grid-template-columns: repeat(2, 1fr); }
        .cols-3 { grid-template-columns: repeat(3, 1fr); }
        .cols-4 { grid-template-columns: repeat(4, 1fr); }

        .media-card {
            position: relative;
            aspect-ratio: 1;
            border-radius: 20px;
            overflow: hidden;
            background: var(--surface-color);
            cursor: pointer;
            transition: transform 0.25s, box-shadow 0.25s;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        }

        .media-card:hover {
            transform: scale(1.02);
            box-shadow: 0 8px 25px rgba(124, 58, 237, 0.25);
        }

        .media-card img {
            width: 100%;
            height: 100%;
            object-fit: crop;
            object-position: center;
        }

        .tag-badge {
            position: absolute;
            bottom: 10px;
            right: 10px;
            background: rgba(0, 0, 0, 0.65);
            backdrop-filter: blur(8px);
            padding: 3px 8px;
            border-radius: 8px;
            font-size: 11px;
            font-weight: 500;
        }

        .fav-btn {
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(0, 0, 0, 0.5);
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            cursor: pointer;
        }

        .fav-btn.active {
            color: #EF4444;
        }

        /* Floating Pill Bottom Navigation */
        .bottom-nav {
            position: fixed;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 100;
            display: flex;
            gap: 24px;
            padding: 10px 28px;
            border-radius: 40px;
            background: rgba(19, 27, 46, 0.9);
            backdrop-filter: blur(20px);
            border: 1px solid rgba(124, 58, 237, 0.3);
            box-shadow: 0 12px 35px rgba(0, 0, 0, 0.5);
        }

        .nav-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;
            color: var(--text-secondary);
            font-size: 11px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
        }

        .nav-item.active {
            color: var(--primary);
            font-weight: 600;
            transform: scale(1.1);
        }

        .nav-item i {
            font-size: 18px;
        }

        /* Fullscreen Viewer Modal */
        .viewer-modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: black;
            z-index: 300;
            display: none;
            align-items: center;
            justify-content: center;
        }

        .viewer-modal.open {
            display: flex;
        }

        .viewer-img {
            max-width: 90vw;
            max-height: 80vh;
            border-radius: 16px;
            object-fit: contain;
        }

        .close-viewer {
            position: absolute;
            top: 24px;
            left: 24px;
            font-size: 24px;
            color: white;
            cursor: pointer;
            z-index: 310;
        }

        /* Views (Tabs) */
        .tab-content {
            display: none;
        }
        .tab-content.active {
            display: block;
        }

        .collections-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 16px;
            padding: 0 24px;
        }

        .album-card {
            height: 180px;
            border-radius: 22px;
            position: relative;
            overflow: hidden;
            cursor: pointer;
            box-shadow: 0 6px 20px rgba(0,0,0,0.3);
        }

        .album-card img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .album-overlay {
            position: absolute;
            bottom: 0;
            width: 100%;
            padding: 16px;
            background: linear-gradient(transparent, rgba(0,0,0,0.85));
        }
    </style>
</head>
<body>

    <!-- Top Arc Bar -->
    <header class="top-nav">
        <div class="search-container">
            <i class="fa-solid fa-magnifying-glass" style="color: #7C3AED;"></i>
            <input type="text" id="searchInput" class="search-input" placeholder="Search photos, videos, people, places...">
        </div>
        <button class="icon-btn" onclick="triggerNotification('AI batch sync complete!')"><i class="fa-solid fa-bell"></i></button>
        <button class="icon-btn avatar-btn"><i class="fa-solid fa-user"></i></button>
    </header>

    <!-- Floating Real-Time Animation Capsule -->
    <div id="floatingBanner" class="floating-banner">
        <div class="pulse-icon"><i class="fa-solid fa-wand-magic-sparkles"></i></div>
        <span id="bannerText" style="font-weight: 600; font-size: 13px;">5 new photos organized</span>
        <span style="font-size: 10px; background: rgba(96, 165, 250, 0.2); color: #60A5FA; padding: 2px 6px; border-radius: 6px; font-weight: 700;">AI READY</span>
    </div>

    <!-- GALLERY TAB -->
    <main id="tabGallery" class="tab-content active">
        <div class="controls-bar">
            <span id="mediaCountText" style="font-size: 13px; color: var(--text-secondary); font-weight: 500;">Loading photos...</span>
            <div class="density-toggle">
                <button class="density-btn" onclick="setDensity(2)">2x</button>
                <button class="density-btn active" onclick="setDensity(3)">3x</button>
                <button class="density-btn" onclick="setDensity(4)">4x</button>
            </div>
        </div>

        <div id="galleryGrid" class="gallery-grid cols-3">
            <!-- Dynamic cards injected via JS -->
        </div>
    </main>

    <!-- COLLECTIONS TAB -->
    <main id="tabCollections" class="tab-content">
        <div style="padding: 16px 24px;">
            <h2 style="font-size: 26px; font-weight: 700; margin-bottom: 4px;">Collections</h2>
            <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 20px;">AI generated categories & smart albums</p>
            <div id="albumsGrid" class="collections-grid">
                <!-- Albums injected via JS -->
            </div>
        </div>
    </main>

    <!-- ACTIVITY TAB -->
    <main id="tabActivity" class="tab-content">
        <div style="padding: 16px 24px;">
            <h2 style="font-size: 26px; font-weight: 700; margin-bottom: 4px;">Activity</h2>
            <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 20px;">Real-time background AI tasks</p>
            <div style="display: flex; flex-direction: column; gap: 12px;">
                <div class="glass" style="padding: 16px; display: flex; align-items: center; gap: 14px;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: rgba(16, 185, 129, 0.15); display: flex; align-items: center; justify-content: center; color: #10B981;">
                        <i class="fa-solid fa-circle-check"></i>
                    </div>
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">12 photos organized</div>
                        <div style="font-size: 12px; color: var(--text-secondary);">Categorized into Nature & Sunset albums</div>
                    </div>
                </div>
                <div class="glass" style="padding: 16px; display: flex; align-items: center; gap: 14px;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: rgba(124, 58, 237, 0.15); display: flex; align-items: center; justify-content: center; color: #7C3AED;">
                        <i class="fa-solid fa-clone"></i>
                    </div>
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">Duplicate removed</div>
                        <div style="font-size: 12px; color: var(--text-secondary);">Saved 14.2 MB of storage</div>
                    </div>
                </div>
                <div class="glass" style="padding: 16px; display: flex; align-items: center; gap: 14px;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: rgba(37, 99, 235, 0.15); display: flex; align-items: center; justify-content: center; color: #2563EB;">
                        <i class="fa-solid fa-file-lines"></i>
                    </div>
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">OCR completed</div>
                        <div style="font-size: 12px; color: var(--text-secondary);">Indexed Passport and Receipt text</div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- SETTINGS TAB -->
    <main id="tabSettings" class="tab-content">
        <div style="padding: 16px 24px;">
            <h2 style="font-size: 26px; font-weight: 700; margin-bottom: 4px;">Settings</h2>
            <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 20px;">AI neural preferences & privacy</p>
            <div class="glass" style="padding: 20px; display: flex; flex-direction: column; gap: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">Real-Time AI Sorting</div>
                        <div style="font-size: 12px; color: var(--text-secondary);">Auto-sort photos as they arrive in background</div>
                    </div>
                    <input type="checkbox" checked style="accent-color: var(--primary); transform: scale(1.3);">
                </div>
                <hr style="border-color: var(--border-color);">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">100% On-Device Neural Engine</div>
                        <div style="font-size: 12px; color: #10B981;">Active • Zero Cloud Telemetry</div>
                    </div>
                    <i class="fa-solid fa-shield-halved" style="color: #10B981; font-size: 20px;"></i>
                </div>
            </div>
        </div>
    </main>

    <!-- Fullscreen Media Viewer Modal -->
    <div id="viewerModal" class="viewer-modal">
        <i class="fa-solid fa-arrow-left close-viewer" onclick="closeViewer()"></i>
        <img id="viewerImage" class="viewer-img" src="" alt="Full view">
    </div>

    <!-- Floating Pill Bottom Navigation -->
    <nav class="bottom-nav">
        <div class="nav-item active" onclick="switchTab('tabGallery', this)">
            <i class="fa-solid fa-images"></i>
            <span>Gallery</span>
        </div>
        <div class="nav-item" onclick="switchTab('tabCollections', this)">
            <i class="fa-solid fa-folder-tree"></i>
            <span>Collections</span>
        </div>
        <div class="nav-item" onclick="focusSearch()">
            <i class="fa-solid fa-magnifying-glass"></i>
            <span>Search</span>
        </div>
        <div class="nav-item" onclick="switchTab('tabActivity', this)">
            <i class="fa-solid fa-clock-rotate-left"></i>
            <span>Activity</span>
        </div>
        <div class="nav-item" onclick="switchTab('tabSettings', this)">
            <i class="fa-solid fa-gear"></i>
            <span>Settings</span>
        </div>
    </nav>

    <script>
        let allMedia = [];

        async function loadMedia() {
            try {
                const res = await fetch('/api/media');
                const data = await res.json();
                allMedia = data.items;
                renderGallery(allMedia);
                document.getElementById('mediaCountText').innerText = `${allMedia.length} items organized`;
            } catch(e) {
                console.error(e);
            }
        }

        async function loadAlbums() {
            try {
                const res = await fetch('/api/albums');
                const albums = await res.json();
                const container = document.getElementById('albumsGrid');
                container.innerHTML = albums.map(a => `
                    <div class="album-card" onclick="filterByScene('${a.category}')">
                        <img src="${a.cover_path}" alt="${a.title}">
                        <div class="album-overlay">
                            <div style="font-weight: 700; font-size: 16px;">${a.title}</div>
                            <div style="font-size: 12px; color: rgba(255,255,255,0.7);">${a.item_count} items</div>
                        </div>
                    </div>
                `).join('');
            } catch(e) {
                console.error(e);
            }
        }

        function renderGallery(items) {
            const grid = document.getElementById('galleryGrid');
            grid.innerHTML = items.map(item => `
                <div class="media-card" onclick="openViewer('${item.path}')">
                    <img src="${item.path}" alt="${item.display_name}" loading="lazy">
                    <div class="fav-btn ${item.is_favorite ? 'active' : ''}" onclick="event.stopPropagation(); this.classList.toggle('active')">
                        <i class="fa-solid fa-heart"></i>
                    </div>
                    ${item.ai_tags && item.ai_tags.length ? `<div class="tag-badge">${item.ai_tags[0]}</div>` : ''}
                </div>
            `).join('');
        }

        function setDensity(cols) {
            document.querySelectorAll('.density-btn').forEach(b => b.classList.remove('active'));
            event.target.classList.add('active');
            const grid = document.getElementById('galleryGrid');
            grid.className = `gallery-grid cols-${cols}`;
        }

        function openViewer(src) {
            document.getElementById('viewerImage').src = src;
            document.getElementById('viewerModal').classList.add('open');
        }

        function closeViewer() {
            document.getElementById('viewerModal').classList.remove('open');
        }

        function switchTab(tabId, el) {
            document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            document.getElementById(tabId).classList.add('active');
            if (el) el.classList.add('active');
        }

        function focusSearch() {
            document.getElementById('searchInput').focus();
        }

        function filterByScene(scene) {
            switchTab('tabGallery', document.querySelector('.nav-item'));
            const filtered = allMedia.filter(m => m.scene_category === scene);
            renderGallery(filtered);
            document.getElementById('mediaCountText').innerText = `${filtered.length} items in ${scene}`;
        }

        function triggerNotification(msg) {
            const banner = document.getElementById('floatingBanner');
            document.getElementById('bannerText').innerText = msg;
            banner.classList.add('show');
            setTimeout(() => banner.classList.remove('show'), 3500);
        }

        // Live Search Input Debounce
        document.getElementById('searchInput').addEventListener('input', async (e) => {
            const query = e.target.value.trim();
            if (query.length === 0) {
                renderGallery(allMedia);
                document.getElementById('mediaCountText').innerText = `${allMedia.length} items organized`;
                return;
            }
            try {
                const res = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
                const data = await res.json();
                renderGallery(data.results);
                document.getElementById('mediaCountText').innerText = `${data.count} matches for "${query}"`;
            } catch(e) {}
        });

        // Initialize
        loadMedia();
        loadAlbums();
    </script>
</body>
</html>
    """

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
