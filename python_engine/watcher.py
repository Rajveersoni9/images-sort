#!/usr/bin/env python3
"""
MediaFlow AI — Real-time File System Observer
Watches directories and triggers instant on-the-fly AI sorting whenever photos or videos arrive.
"""

import sys
import time
import os
from pathlib import Path
from sorter import MediaFlowSorter, IMAGE_EXTENSIONS, VIDEO_EXTENSIONS

class RealtimeMediaWatcher:
    def __init__(self, watch_dir: str, poll_interval: float = 2.0):
        self.watch_dir = Path(watch_dir)
        self.poll_interval = poll_interval
        self.sorter = MediaFlowSorter()
        self.known_files = set()

    def scan_existing(self):
        if not self.watch_dir.exists():
            print(f"[!] Watch directory does not exist: {self.watch_dir}")
            return
        for root, _, files in os.walk(self.watch_dir):
            for f in files:
                p = Path(root) / f
                if p.suffix.lower() in IMAGE_EXTENSIONS or p.suffix.lower() in VIDEO_EXTENSIONS:
                    self.known_files.add(str(p.resolve()))
        print(f"[*] Initialized observer with {len(self.known_files)} existing media items.")

    def start(self):
        self.scan_existing()
        print(f"[*] MediaFlow AI real-time watcher active on: {self.watch_dir.resolve()}")
        print("[*] Press Ctrl+C to stop.\n")

        try:
            while True:
                current_files = set()
                new_files = []

                for root, _, files in os.walk(self.watch_dir):
                    for f in files:
                        p = Path(root) / f
                        if p.suffix.lower() in IMAGE_EXTENSIONS or p.suffix.lower() in VIDEO_EXTENSIONS:
                            resolved = str(p.resolve())
                            current_files.add(resolved)
                            if resolved not in self.known_files:
                                new_files.append(p)

                if new_files:
                    print(f"\n[✨ NEW MEDIA DETECTED] Processing {len(new_files)} new items...")
                    for nf in new_files:
                        category, tags = self.sorter.classify_scene_and_tags(nf)
                        dominant = self.sorter.extract_dominant_color(nf)
                        dhash = self.sorter.calculate_dhash(nf)
                        print(f"  • {nf.name} ➔ Smart Album: [{category}] | Tags: {tags[:3]} | Tone: {dominant}")

                    # Refresh index
                    self.sorter.scan_directory(str(self.watch_dir))
                    self.known_files = current_files
                    print(f"  ✔ Real-Time Sorting Completed: {len(new_files)} new photos organized!\n")

                time.sleep(self.poll_interval)
        except KeyboardInterrupt:
            print("\n[*] MediaFlow AI watcher stopped.")


if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else "."
    watcher = RealtimeMediaWatcher(target)
    watcher.start()
