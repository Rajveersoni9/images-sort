#!/usr/bin/env python3
"""
MediaFlow AI — FastAPI Real-time Media Server & WebSocket Service
"""

import os
import json
import asyncio
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Query, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from sorter import MediaFlowSorter

app = FastAPI(
    title="MediaFlow AI Core API",
    description="Real-time intelligent media sorting and neural indexing engine",
    version="1.0.0"
)

# Enable CORS for local Android development and web clients
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

@app.get("/")
def root():
    return {
        "app": "MediaFlow AI",
        "status": "online",
        "version": "1.0.0",
        "engine": "Python 3.12 + PyTorch & PIL"
    }

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
    with sorter._init_db() or None:
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
            # Keep-alive ping
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        manager.disconnect(websocket)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)
