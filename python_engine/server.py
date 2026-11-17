#!/usr/bin/env python3
"""
MediaFlow AI — Live FastAPI Server & 3D Sphere Sorting Web Application
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
    description="Real-time intelligent media sorting and neural 3D indexing engine",
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

class OrganizeRequest(BaseModel):
    source_dir: str
    dest_dir: Optional[str] = None
    mode: str = "copy"

@app.post("/api/organize")
def organize_media(req: OrganizeRequest):
    return sorter.organize_and_export(req.source_dir, req.dest_dir, req.mode)

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
# MEDIAFLOW AI — EXACT 3D SPHERE & DISPERSAL ANIMATION WEB INTERFACE
# ---------------------------------------------------------------------------

@app.get("/", response_class=HTMLResponse)
def get_web_app():
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MediaFlow AI — Intelligent 3D Photo Sorter</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=Playfair+Display:ital,wght@1,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --bg: #F9FAFB;
            --surface: #FFFFFF;
            --border: #E5E7EB;
            --text-main: #111827;
            --text-muted: #6B7280;
            --primary: #7C3AED;
            --accent-orange: #F59E0B;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Plus Jakarta Sans', sans-serif;
            user-select: none;
        }

        body {
            background-color: var(--bg);
            color: var(--text-main);
            overflow: hidden;
            width: 100vw;
            height: 100vh;
            display: flex;
            flex-direction: column;
        }

        /* Top Brand & Stats Bar */
        .header {
            padding: 14px 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            z-index: 50;
        }

        .brand-title {
            font-size: 20px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .brand-title span.serif {
            font-family: 'Playfair Display', serif;
            font-style: italic;
            color: #DC2626;
            font-weight: 600;
        }

        .stats-strip {
            display: flex;
            align-items: center;
            gap: 24px;
        }

        .stat-item {
            display: flex;
            flex-direction: column;
        }

        .stat-label {
            font-size: 11px;
            color: var(--text-muted);
            font-weight: 500;
        }

        .stat-value {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-main);
        }

        .pause-btn {
            background: #F59E0B;
            color: white;
            border: none;
            padding: 8px 18px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 13px;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 6px;
            box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
            transition: transform 0.15s;
        }

        .pause-btn:hover {
            transform: scale(1.04);
        }

        .sort-action-btn {
            background: linear-gradient(135deg, #7C3AED, #2563EB);
            color: white;
            border: none;
            padding: 8px 18px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 13px;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 6px;
            box-shadow: 0 4px 12px rgba(124, 58, 237, 0.35);
            transition: transform 0.15s, box-shadow 0.15s;
        }

        .sort-action-btn:hover {
            transform: scale(1.04);
            box-shadow: 0 6px 18px rgba(124, 58, 237, 0.5);
        }

        /* Modal Backdrop */
        .modal-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(8px);
            z-index: 1000;
            display: none;
            align-items: center;
            justify-content: center;
        }

        .modal-box {
            background: white;
            width: 520px;
            max-width: 90vw;
            border-radius: 24px;
            box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
            padding: 28px;
            position: relative;
            animation: modalFade 0.25s ease-out;
        }

        @keyframes modalFade {
            from { transform: scale(0.92); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        .modal-title {
            font-size: 18px;
            font-weight: 700;
            color: #111827;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .modal-close {
            background: #F3F4F6;
            border: none;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            cursor: pointer;
            font-size: 14px;
            color: #6B7280;
        }

        .modal-desc {
            font-size: 13px;
            color: #6B7280;
            line-height: 1.5;
            margin-bottom: 20px;
        }

        .input-group {
            margin-bottom: 16px;
        }

        .input-label {
            font-size: 12px;
            font-weight: 600;
            color: #374151;
            margin-bottom: 6px;
            display: block;
        }

        .text-input {
            width: 100%;
            padding: 10px 14px;
            border: 1.5px solid #E5E7EB;
            border-radius: 12px;
            font-size: 13px;
            outline: none;
            transition: border-color 0.2s;
        }

        .text-input:focus {
            border-color: #7C3AED;
        }

        .radio-options {
            display: flex;
            gap: 16px;
            margin-top: 6px;
            font-size: 13px;
            color: #374151;
        }

        .btn-choose-folder {
            width: 100%;
            background: #F3F4F6;
            color: #111827;
            border: 1.5px dashed #CBD5E1;
            border-radius: 14px;
            padding: 14px;
            font-weight: 600;
            font-size: 13px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-bottom: 16px;
            transition: all 0.2s;
        }

        .btn-choose-folder:hover {
            background: #EDE9FE;
            border-color: #7C3AED;
            color: #7C3AED;
        }

        .btn-start-sort {
            width: 100%;
            background: linear-gradient(135deg, #7C3AED, #2563EB);
            color: white;
            border: none;
            border-radius: 14px;
            padding: 14px;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            box-shadow: 0 4px 14px rgba(124, 58, 237, 0.4);
            transition: transform 0.15s;
        }

        .btn-start-sort:hover {
            transform: scale(1.02);
        }

        /* Main Workspace: Folders on Left, 3D Globe in Center, Folders on Right */
        .workspace {
            flex: 1;
            display: grid;
            grid-template-columns: 280px 1fr 280px;
            padding: 16px 24px;
            gap: 16px;
            position: relative;
            overflow: hidden;
        }

        .column-folders {
            display: flex;
            flex-direction: column;
            gap: 12px;
            z-index: 20;
            overflow-y: auto;
            max-height: calc(100vh - 150px);
            padding-right: 4px;
        }

        .column-folders::-webkit-scrollbar {
            width: 4px;
        }
        .column-folders::-webkit-scrollbar-thumb {
            background: #E5E7EB;
            border-radius: 4px;
        }

        /* Folder Card matching the video */
        .folder-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 10px 12px;
            display: flex;
            flex-direction: column;
            gap: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.03);
            transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
            position: relative;
        }

        .folder-card.pulse {
            transform: scale(1.04);
            border-color: #7C3AED;
            box-shadow: 0 8px 24px rgba(124, 58, 237, 0.2);
        }

        .folder-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .folder-title-wrap {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .folder-icon {
            width: 22px;
            height: 22px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            color: white;
        }

        .folder-name {
            font-size: 13px;
            font-weight: 600;
            color: var(--text-main);
        }

        .folder-count {
            font-size: 11px;
            font-weight: 700;
            color: var(--text-muted);
            background: #F3F4F6;
            padding: 2px 6px;
            border-radius: 8px;
        }

        /* Mini Photo Grid inside each folder */
        .folder-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 3px;
            height: 48px;
        }

        .mini-slot {
            background: #F3F4F6;
            border-radius: 4px;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .mini-slot img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            animation: slotPop 0.3s cubic-bezier(0.16, 1, 0.3, 1);
        }

        @keyframes slotPop {
            0% { transform: scale(0.4); opacity: 0; }
            100% { transform: scale(1); opacity: 1; }
        }

        /* Center Canvas & 3D Globe Area */
        .center-stage {
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
        }

        .stage-title {
            font-size: 26px;
            font-weight: 800;
            margin-bottom: 4px;
            text-align: center;
        }

        .stage-title span.serif {
            font-family: 'Playfair Display', serif;
            font-style: italic;
            color: #F59E0B;
        }

        .stage-subtitle {
            font-size: 13px;
            color: var(--text-muted);
            margin-bottom: 12px;
        }

        /* 3D Sphere Canvas Container */
        .sphere-container {
            width: 440px;
            height: 440px;
            position: relative;
            perspective: 1000px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .sphere-world {
            width: 100%;
            height: 100%;
            position: absolute;
            transform-style: preserve-3d;
            animation: sphereRotate 32s linear infinite;
        }

        @keyframes sphereRotate {
            0% { transform: rotateY(0deg) rotateX(15deg); }
            100% { transform: rotateY(360deg) rotateX(15deg); }
        }

        .globe-photo {
            position: absolute;
            width: 36px;
            height: 36px;
            border-radius: 6px;
            object-fit: cover;
            border: 1px solid rgba(255,255,255,0.7);
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
            transform-origin: center center;
            pointer-events: none;
        }

        /* Active Spotlight Center Photo */
        .spotlight-card {
            position: absolute;
            z-index: 100;
            width: 130px;
            height: 130px;
            border-radius: 18px;
            background: white;
            box-shadow: 0 16px 40px rgba(0,0,0,0.25);
            border: 3px solid white;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
            animation: spotlightPop 0.4s ease-out;
        }

        @keyframes spotlightPop {
            0% { transform: scale(0.6); opacity: 0; }
            100% { transform: scale(1); opacity: 1; }
        }

        .spotlight-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .spotlight-badge {
            position: absolute;
            bottom: 8px;
            background: rgba(0,0,0,0.75);
            backdrop-filter: blur(6px);
            color: white;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        /* Trajectory SVG Layer for Dotted Vector Line */
        .trajectory-svg {
            position: absolute;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            pointer-events: none;
            z-index: 40;
        }

        .trajectory-line {
            stroke: #F59E0B;
            stroke-width: 2.5;
            stroke-dasharray: 6, 6;
            animation: dashMove 0.6s linear infinite;
        }

        @keyframes dashMove {
            to { stroke-dashoffset: -12; }
        }

        /* Flying Photo Particle */
        .flying-photo {
            position: fixed;
            z-index: 150;
            width: 70px;
            height: 70px;
            border-radius: 12px;
            object-fit: cover;
            border: 2px solid white;
            box-shadow: 0 12px 28px rgba(0,0,0,0.3);
            pointer-events: none;
            transition: all 0.75s cubic-bezier(0.2, 0.8, 0.2, 1);
        }

        /* Bottom Search Pill */
        .bottom-search {
            position: absolute;
            bottom: 24px;
            width: 480px;
            display: flex;
            align-items: center;
            gap: 12px;
            background: #111827;
            border-radius: 30px;
            padding: 12px 20px;
            color: white;
            box-shadow: 0 12px 30px rgba(0,0,0,0.25);
            z-index: 50;
        }

        .bottom-search input {
            background: transparent;
            border: none;
            outline: none;
            color: white;
            font-size: 14px;
            width: 100%;
        }

        .bottom-search input::placeholder {
            color: #9CA3AF;
        }
    </style>
</head>
<body>

    <!-- Trajectory Vector SVG -->
    <svg id="trajectorySvg" class="trajectory-svg">
        <line id="trajLine" x1="0" y1="0" x2="0" y2="0" class="trajectory-line" style="display: none;"/>
    </svg>

    <!-- Top Stats Header -->
    <header class="header">
        <div class="brand-title">
            <i class="fa-solid fa-shapes" style="color: #7C3AED;"></i>
            <span>Sorted</span>
            <span class="serif">by MediaFlow AI</span>
        </div>

        <div class="stats-strip">
            <div class="stat-item">
                <span class="stat-label">Target Size</span>
                <span id="statTargetSize" class="stat-value">17,233</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Photos</span>
                <span id="statPhotos" class="stat-value">0</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Screenshots & docs</span>
                <span id="statDocs" class="stat-value">0</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Folders created</span>
                <span class="stat-value">11</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Duplicates found</span>
                <span id="statDuplicates" class="stat-value">0</span>
            </div>
            <button id="pauseBtn" class="pause-btn" onclick="togglePause()">
                <i class="fa-solid fa-pause"></i>
                <span>Pause</span>
            </button>
            <button id="openSortModalBtn" class="sort-action-btn" onclick="openSortModal()">
                <i class="fa-solid fa-folder-tree"></i>
                <span>Sort Local Folder</span>
            </button>
        </div>
    </header>

    <!-- Modal for Sorting Local Folder -->
    <div id="sortModal" class="modal-backdrop">
        <div class="modal-box">
            <div class="modal-header">
                <div class="modal-title">
                    <i class="fa-solid fa-folder-open" style="color: #7C3AED;"></i>
                    <span>Sort Media On Your Computer</span>
                </div>
                <button class="modal-close" onclick="closeSortModal()"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <p class="modal-desc">
                Select your photos and videos folder. MediaFlow AI will classify every item and organize them into categorized folders (Selfies, Travel, Screenshots, etc.) saved directly on your local machine.
            </p>

            <button class="btn-choose-folder" onclick="selectAndSortLocalFolder()">
                <i class="fa-solid fa-folder-plus" style="color: #7C3AED; font-size: 16px;"></i>
                <span>Browse & Pick Folder On Disk (Direct)</span>
            </button>

            <div style="text-align: center; font-size: 11px; color: #9CA3AF; margin-bottom: 14px;">— OR ENTER PATH FOR LOCAL PYTHON ENGINE —</div>

            <div class="input-group">
                <label class="input-label">Folder Path (Local PC)</label>
                <input id="folderPathInput" type="text" class="text-input" placeholder="e.g. C:\Users\rajve\Pictures or E:\MyPhotos">
            </div>

            <div class="input-group">
                <label class="input-label">Destination Folder</label>
                <input id="destPathInput" type="text" class="text-input" placeholder="Default: [Source]\MediaFlow_Sorted\">
            </div>

            <div class="input-group">
                <label class="input-label">Organization Mode</label>
                <div class="radio-options">
                    <label><input type="radio" name="sortMode" value="copy" checked> Copy (Safe - Keep Originals)</label>
                    <label><input type="radio" name="sortMode" value="move"> Move (Clean up & Free Space)</label>
                </div>
            </div>

            <button class="btn-start-sort" onclick="startServerSort()">
                <i class="fa-solid fa-bolt"></i>
                <span>Start AI Organization</span>
            </button>
        </div>
    </div>

    <!-- Main Workspace -->
    <div class="workspace">
        
        <!-- Left Side: 6 Folder Cards -->
        <div class="column-folders" id="colLeft">
            <!-- Injected by JS -->
        </div>

        <!-- Center Stage: 3D Photo Globe & Spotlight -->
        <div class="center-stage">
            <h1 class="stage-title" id="stageHeadline">Claude is looking at <span class="serif">every photo.</span></h1>
            <p class="stage-subtitle" id="stageSubline">Sorting into 11 smart folders in real time</p>

            <div class="sphere-container">
                <!-- 3D Rotating Sphere -->
                <div class="sphere-world" id="sphereWorld"></div>

                <!-- Center Active Spotlight Photo -->
                <div class="spotlight-card" id="spotlightCard">
                    <img id="spotlightImg" class="spotlight-img" src="" alt="Active">
                    <div id="spotlightBadge" class="spotlight-badge">
                        <i class="fa-solid fa-wand-magic-sparkles"></i>
                        <span id="spotlightTag">Analyzing...</span>
                    </div>
                </div>
            </div>

            <!-- Bottom Search Pill -->
            <div class="bottom-search">
                <i class="fa-solid fa-magnifying-glass" style="color: #9CA3AF;"></i>
                <input type="text" placeholder="Search by tag, folder name, or wedding...">
            </div>
        </div>

        <!-- Right Side: 5 Folder Cards -->
        <div class="column-folders" id="colRight">
            <!-- Injected by JS -->
        </div>

    </div>

    <script>
        // Folder Configuration matching the video exactly (11 Folders)
        const FOLDERS = [
            // Left Column
            { id: "selfies", name: "Selfies", icon: "fa-user", color: "#F97316", col: "left", count: 0, items: [] },
            { id: "events", name: "Events & weddings", icon: "fa-champagne-glasses", color: "#8B5CF6", col: "left", count: 0, items: [] },
            { id: "travel", name: "Travel", icon: "fa-plane-departure", color: "#3B82F6", col: "left", count: 0, items: [] },
            { id: "screenshots", name: "Screenshots", icon: "fa-mobile-screen", color: "#6366F1", col: "left", count: 0, items: [] },
            { id: "receipts", name: "Receipts & docs", icon: "fa-receipt", color: "#F59E0B", col: "left", count: 0, items: [] },
            { id: "duplicates", name: "Duplicates", icon: "fa-clone", color: "#64748B", col: "left", count: 0, items: [] },
            
            // Right Column
            { id: "family", name: "Family & friends", icon: "fa-users", color: "#EC4899", col: "right", count: 0, items: [] },
            { id: "food", name: "Food", icon: "fa-utensils", color: "#EF4444", col: "right", count: 0, items: [] },
            { id: "nature", name: "Nature & views", icon: "fa-mountain-sun", color: "#10B981", col: "right", count: 0, items: [] },
            { id: "animals", name: "Animals & pets", icon: "fa-paw", color: "#14B8A6", col: "right", count: 0, items: [] },
            { id: "shopping", name: "Things & shopping", icon: "fa-bag-shopping", color: "#F59E0B", col: "right", count: 0, items: [] }
        ];

        // Sample photos stream
        const PHOTO_STREAM = [
            { url: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80", target: "selfies", tag: "Selfie", icon: "fa-camera" },
            { url: "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=600&q=80", target: "events", tag: "Wedding Ceremony", icon: "fa-ring" },
            { url: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80", target: "travel", tag: "Tropical Beach", icon: "fa-sun" },
            { url: "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80", target: "screenshots", tag: "App Code Screenshot", icon: "fa-code" },
            { url: "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80", target: "receipts", tag: "Passport / Doc", icon: "fa-file-invoice" },
            { url: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80", target: "animals", tag: "Golden Retriever", icon: "fa-dog" },
            { url: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=600&q=80", target: "food", tag: "Gourmet Meal", icon: "fa-burger" },
            { url: "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=600&q=80", target: "nature", tag: "Alpine Mountains", icon: "fa-tree" },
            { url: "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80", target: "shopping", tag: "Red Sports Car", icon: "fa-car" },
            { url: "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?auto=format&fit=crop&w=600&q=80", target: "family", tag: "Friends Group", icon: "fa-heart" },
            { url: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80", target: "duplicates", tag: "Duplicate Photo", icon: "fa-clone" }
        ];

        let targetRemaining = 17233;
        let photosSorted = 0;
        let docsCount = 0;
        let dupsCount = 0;
        let isPaused = false;
        let streamIndex = 0;

        // Render Folder Cards in Left and Right Columns
        function initFolderCards() {
            const colLeft = document.getElementById("colLeft");
            const colRight = document.getElementById("colRight");

            FOLDERS.forEach(folder => {
                const card = document.createElement("div");
                card.className = "folder-card";
                card.id = `card_${folder.id}`;

                let slotsHtml = "";
                for (let i = 0; i < 18; i++) {
                    slotsHtml += `<div class="mini-slot" id="slot_${folder.id}_${i}"></div>`;
                }

                card.innerHTML = `
                    <div class="folder-header">
                        <div class="folder-title-wrap">
                            <div class="folder-icon" style="background: ${folder.color}">
                                <i class="fa-solid ${folder.icon}"></i>
                            </div>
                            <span class="folder-name">${folder.name}</span>
                        </div>
                        <span class="folder-count" id="count_${folder.id}">0</span>
                    </div>
                    <div class="folder-grid">
                        ${slotsHtml}
                    </div>
                `;

                if (folder.col === "left") {
                    colLeft.appendChild(card);
                } else {
                    colRight.appendChild(card);
                }
            });
        }

        // Generate Fibonacci Sphere 3D Cloud
        function initSphere() {
            const world = document.getElementById("sphereWorld");
            const numPoints = 85;
            const radius = 175; // px

            for (let i = 0; i < numPoints; i++) {
                const phi = Math.acos(1 - 2 * (i + 0.5) / numPoints);
                const theta = Math.PI * (1 + 5 ** 0.5) * (i + 0.5);

                const x = radius * Math.cos(theta) * Math.sin(phi);
                const y = radius * Math.sin(theta) * Math.sin(phi);
                const z = radius * Math.cos(phi);

                const img = document.createElement("img");
                img.className = "globe-photo";
                img.src = PHOTO_STREAM[i % PHOTO_STREAM.length].url;

                img.style.transform = `translate3d(${x + 200}px, ${y + 200}px, ${z}px)`;
                world.appendChild(img);
            }
        }

        // Flying Particle Animation Pipeline
        function triggerFileSortAnimation(photo) {
            if (!photo || !photo.url) return;

            // 1. Spotlight Active Photo in Center
            const spotlight = document.getElementById("spotlightCard");
            const spotlightImg = document.getElementById("spotlightImg");
            const spotlightTag = document.getElementById("spotlightTag");

            spotlightImg.src = photo.url;
            if (spotlightTag) {
                spotlightTag.innerText = photo.tag || photo.name || "AI Classified";
            }

            // 2. Dotted Ray Trajectory to Target Folder
            const targetFolder = FOLDERS.find(f => f.id === photo.target) || FOLDERS[0];
            const targetCard = document.getElementById(`card_${targetFolder.id}`);
            if (!spotlight || !targetCard) return;

            const centerRect = spotlight.getBoundingClientRect();
            const targetRect = targetCard.getBoundingClientRect();

            const startX = centerRect.left + centerRect.width / 2;
            const startY = centerRect.top + centerRect.height / 2;
            const endX = targetRect.left + targetRect.width / 2;
            const endY = targetRect.top + targetRect.height / 2;

            const trajLine = document.getElementById("trajLine");
            if (trajLine) {
                trajLine.setAttribute("x1", startX);
                trajLine.setAttribute("y1", startY);
                trajLine.setAttribute("x2", endX);
                trajLine.setAttribute("y2", endY);
                trajLine.style.display = "block";
            }

            // 3. Create Flying Photo Particle
            const flying = document.createElement("img");
            flying.className = "flying-photo";
            flying.src = photo.url;
            flying.style.left = `${startX - 35}px`;
            flying.style.top = `${startY - 35}px`;
            document.body.appendChild(flying);

            // Animate Flight along trajectory
            requestAnimationFrame(() => {
                flying.style.left = `${endX - 20}px`;
                flying.style.top = `${endY - 20}px`;
                flying.style.width = "40px";
                flying.style.height = "40px";
                flying.style.opacity = "0.7";
            });

            // 4. Land in Folder Card
            setTimeout(() => {
                flying.remove();
                if (trajLine) trajLine.style.display = "none";

                // Card Absorption Pulse
                targetCard.classList.add("pulse");
                setTimeout(() => targetCard.classList.remove("pulse"), 400);

                // Insert into mini-grid slot
                targetFolder.count++;
                const countEl = document.getElementById(`count_${targetFolder.id}`);
                if (countEl) countEl.innerText = targetFolder.count;

                const slotIdx = (targetFolder.count - 1) % 18;
                const slot = document.getElementById(`slot_${targetFolder.id}_${slotIdx}`);
                if (slot) {
                    slot.innerHTML = `<img src="${photo.url}" alt="thumb">`;
                }

                // Update Stats Header
                targetRemaining = Math.max(0, targetRemaining - 1);
                photosSorted++;
                if (photo.target === "screenshots" || photo.target === "receipts") docsCount++;
                if (photo.target === "duplicates") dupsCount++;

                const statTargetEl = document.getElementById("statTargetSize");
                if (statTargetEl) statTargetEl.innerText = targetRemaining.toLocaleString();
                const statPhotosEl = document.getElementById("statPhotos");
                if (statPhotosEl) statPhotosEl.innerText = photosSorted.toLocaleString();
                const statDocsEl = document.getElementById("statDocs");
                if (statDocsEl) statDocsEl.innerText = docsCount.toLocaleString();
                const statDupEl = document.getElementById("statDuplicates");
                if (statDupEl) statDupEl.innerText = dupsCount.toLocaleString();

                if (photosSorted % 5 === 0) {
                    const headline = document.getElementById("stageHeadline");
                    if (headline && !headline.innerText.includes("Sorting local")) {
                        headline.innerHTML = `Sorting into 11 <span class="serif">folders.</span>`;
                    }
                }
            }, 750);
        }

        function triggerNextSortingStep() {
            if (isPaused) return;
            const photo = PHOTO_STREAM[streamIndex % PHOTO_STREAM.length];
            streamIndex++;
            triggerFileSortAnimation(photo);
        }

        function togglePause() {
            isPaused = !isPaused;
            const btn = document.getElementById("pauseBtn");
            if (isPaused) {
                btn.innerHTML = `<i class="fa-solid fa-play"></i><span>Resume</span>`;
                btn.style.background = "#10B981";
            } else {
                btn.innerHTML = `<i class="fa-solid fa-pause"></i><span>Pause</span>`;
                btn.style.background = "#F59E0B";
            }
        }

        function openSortModal() {
            document.getElementById("sortModal").style.display = "flex";
        }

        function closeSortModal() {
            document.getElementById("sortModal").style.display = "none";
        }

        async function selectAndSortLocalFolder() {
            if (!window.showDirectoryPicker) {
                alert("Your browser does not support the File System Access API directly. Please use Chrome/Edge or type your folder path in the input box below.");
                return;
            }
            try {
                const dirHandle = await window.showDirectoryPicker({ mode: "readwrite" });
                closeSortModal();
                
                document.getElementById("stageHeadline").innerHTML = `Sorting local folder <span class="serif">${dirHandle.name}</span>`;
                
                // Create MediaFlow_Sorted folder inside
                const sortedHandle = await dirHandle.getDirectoryHandle("MediaFlow_Sorted", { create: true });
                
                // Create target subfolders
                const folderHandles = {};
                for (const f of FOLDERS) {
                    const cleanName = f.name.replace(/[^a-zA-Z0-9]/g, "_");
                    folderHandles[f.id] = await sortedHandle.getDirectoryHandle(cleanName, { create: true });
                }
                
                let sortedTotal = 0;
                for await (const entry of dirHandle.values()) {
                    if (entry.kind === "file") {
                        const name = entry.name;
                        const ext = name.slice((name.lastIndexOf(".") - 1 >>> 0) + 2).toLowerCase();
                        if (['jpg', 'jpeg', 'png', 'webp', 'mp4', 'mov', 'heic'].includes(ext)) {
                            sortedTotal++;
                            const file = await entry.getFile();
                            const objectUrl = URL.createObjectURL(file);
                            
                            // Categorize based on metadata & name
                            const lower = name.toLowerCase();
                            let targetId = "nature";
                            if (lower.includes("screen")) targetId = "screenshots";
                            else if (lower.includes("selfie") || lower.includes("portrait") || lower.includes("face")) targetId = "selfies";
                            else if (lower.includes("receipt") || lower.includes("doc") || lower.includes("bill") || lower.includes("pdf")) targetId = "receipts";
                            else if (lower.includes("food") || lower.includes("dinner") || lower.includes("meal")) targetId = "food";
                            else if (lower.includes("pet") || lower.includes("dog") || lower.includes("cat")) targetId = "animals";
                            else if (lower.includes("trip") || lower.includes("beach") || lower.includes("travel")) targetId = "travel";
                            else if (lower.includes("dup") || lower.includes("copy")) targetId = "duplicates";
                            else if (lower.includes("family") || lower.includes("friend")) targetId = "family";
                            else if (lower.includes("event") || lower.includes("wedding") || lower.includes("party")) targetId = "events";
                            else if (lower.includes("car") || lower.includes("bike") || lower.includes("item") || lower.includes("shop")) targetId = "shopping";

                            // Copy file directly to destination subfolder on local machine
                            const destSub = folderHandles[targetId];
                            if (destSub) {
                                const newHandle = await destSub.getFileHandle(name, { create: true });
                                const writable = await newHandle.createWritable();
                                await writable.write(file);
                                await writable.close();
                            }

                            // Trigger visual flight to target folder card
                            triggerFileSortAnimation({ url: objectUrl, target: targetId, name: name });
                            await new Promise(r => setTimeout(r, 650));
                        }
                    }
                }
                document.getElementById("stageHeadline").innerHTML = `✓ Completed! <span class="serif">${sortedTotal} items organized</span>`;
                alert(`✓ Done! Successfully sorted ${sortedTotal} photos and saved them on your computer under:\n${dirHandle.name}/MediaFlow_Sorted/`);
            } catch (err) {
                if (err.name !== 'AbortError') {
                    console.error(err);
                    alert("Folder access: " + err.message);
                }
            }
        }

        async function startServerSort() {
            const pathInput = document.getElementById("folderPathInput").value.trim();
            const destInput = document.getElementById("destPathInput").value.trim();
            const mode = document.querySelector('input[name="sortMode"]:checked')?.value || "copy";

            if (!pathInput) {
                alert("Please enter a folder path on your computer (e.g. C:\\Users\\...\\Pictures).");
                return;
            }

            closeSortModal();
            document.getElementById("stageHeadline").innerHTML = `Processing <span class="serif">${pathInput}</span>`;

            try {
                const res = await fetch("/api/organize", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        source_dir: pathInput,
                        dest_dir: destInput || null,
                        mode: mode
                    })
                });
                const data = await res.json();
                if (data.status === "success") {
                    document.getElementById("stageHeadline").innerHTML = `✓ Completed! <span class="serif">${data.total_organized} files sorted</span>`;
                    alert(`✓ Success!\nOrganized ${data.total_organized} files into:\n${data.destination}\n\nMode: ${data.mode.toUpperCase()}`);
                } else {
                    alert(data.message || "Could not complete sorting.");
                }
            } catch (e) {
                alert("Local engine not running. Opening browser direct folder selector instead!");
                selectAndSortLocalFolder();
            }
        }

        // Initialize application on load
        window.addEventListener("DOMContentLoaded", () => {
            initFolderCards();
            initSphere();
            setInterval(triggerNextSortingStep, 1100);
        });
    </script>
</body>
</html>
    """

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
