#!/usr/bin/env python3
"""
MediaFlow AI — Unified Python Runner
Usage:
  python run.py scan <folder_path>
  python run.py watch <folder_path>
  python run.py serve
"""

import sys
import os

def main():
    if len(sys.argv) < 2:
        print("""
┌──────────────────────────────────────────────────────────┐
│                   MediaFlow AI Python Core               │
└──────────────────────────────────────────────────────────┘

Commands:
  python run.py scan [folder]    - Scan and organize media in folder
  python run.py watch [folder]   - Watch folder in real time for new photos
  python run.py serve            - Launch FastAPI & WebSocket media server
""")
        return

    cmd = sys.argv[1].lower()
    arg = sys.argv[2] if len(sys.argv) > 2 else "."

    if cmd == "scan":
        from sorter import MediaFlowSorter
        sorter = MediaFlowSorter()
        print(f"[*] Scanning {arg}...")
        count = sorter.scan_directory(arg)
        print(f"[✔] Organized {count} items.")
    elif cmd == "watch":
        from watcher import RealtimeMediaWatcher
        watcher = RealtimeMediaWatcher(arg)
        watcher.start()
    elif cmd == "serve":
        import uvicorn
        print("[*] Starting MediaFlow AI API & WebSocket server on http://127.0.0.1:8000...")
        uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)
    else:
        print(f"[!] Unknown command: {cmd}")

if __name__ == "__main__":
    main()
