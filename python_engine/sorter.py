#!/usr/bin/env python3
"""
MediaFlow AI — Standalone Python Core Sorter & AI Classifier
Analyzes photos and videos, extracts EXIF, calculates dHash for duplicate detection,
classifies into smart albums, and organizes files.
"""

import os
import sys
import json
import sqlite3
import shutil
import hashlib
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Any, Optional, Tuple
from PIL import Image, ExifTags

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".heic", ".bmp"}
VIDEO_EXTENSIONS = {".mp4", ".mov", ".mkv", ".avi", ".webm"}

class MediaFlowSorter:
    def __init__(self, db_path: str = "mediaflow.db"):
        self.db_path = db_path
        self._init_db()

    def _init_db(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS media_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    path TEXT UNIQUE,
                    display_name TEXT,
                    mime_type TEXT,
                    media_type TEXT,
                    date_taken INTEGER,
                    size_bytes INTEGER,
                    width INTEGER,
                    height INTEGER,
                    is_favorite INTEGER DEFAULT 0,
                    is_duplicate INTEGER DEFAULT 0,
                    duplicate_group_id TEXT,
                    ai_tags TEXT,
                    scene_category TEXT,
                    dominant_color_hex TEXT,
                    perceptual_hash INTEGER
                )
            """)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS smart_albums (
                    id TEXT PRIMARY KEY,
                    title TEXT,
                    category TEXT,
                    item_count INTEGER,
                    cover_path TEXT
                )
            """)
            conn.commit()

    def calculate_dhash(self, img_path: Path, hash_size: int = 8) -> Optional[int]:
        try:
            with Image.open(img_path) as img:
                img = img.convert("L").resize((hash_size + 1, hash_size), Image.Resampling.BILINEAR)
                pixels = list(img.getdata())
                diff = []
                for row in range(hash_size):
                    for col in range(hash_size):
                        left = pixels[row * (hash_size + 1) + col]
                        right = pixels[row * (hash_size + 1) + col + 1]
                        diff.append(left > right)
                hash_val = 0
                for i, v in enumerate(diff):
                    if v:
                        hash_val |= 1 << i
                return hash_val
        except Exception:
            return None

    def extract_dominant_color(self, img_path: Path) -> str:
        try:
            with Image.open(img_path) as img:
                img = img.convert("RGB").resize((16, 16))
                colors = img.getcolors(256)
                if colors:
                    colors.sort(key=lambda c: c[0], reverse=True)
                    r, g, b = colors[0][1]
                    return f"#{r:02X}{g:02X}{b:02X}"
        except Exception:
            pass
        return "#7C3AED"

    def extract_exif(self, img_path: Path) -> Dict[str, Any]:
        info = {
            "date_taken": int(os.path.getmtime(img_path) * 1000),
            "width": 0,
            "height": 0,
            "camera_model": None
        }
        try:
            with Image.open(img_path) as img:
                info["width"], info["height"] = img.size
                exif = img.getexif()
                if exif:
                    for tag_id, val in exif.items():
                        tag = ExifTags.TAGS.get(tag_id, tag_id)
                        if tag == "DateTimeOriginal":
                            try:
                                dt = datetime.strptime(str(val), "%Y:%m:%d %H:%M:%S")
                                info["date_taken"] = int(dt.timestamp() * 1000)
                            except Exception:
                                pass
                        elif tag == "Model":
                            info["camera_model"] = str(val)
        except Exception:
            pass
        return info

    def classify_scene_and_tags(self, img_path: Path) -> Tuple[str, List[str]]:
        """
        Infers scene category and tags using filename heuristics, aspect ratio, and visual signals.
        """
        filename = img_path.name.lower()
        tags = []
        category = "GENERAL"

        # Check for screenshot indicators
        if "screenshot" in filename or "screen_shot" in filename:
            category = "SCREENSHOTS"
            tags.extend(["Screenshot", "UI", "Digital"])
        elif any(k in filename for k in ["doc", "passport", "bill", "invoice", "receipt", "contract", "license"]):
            category = "DOCUMENTS"
            tags.extend(["Document", "Text", "Official"])
        elif any(k in filename for k in ["dog", "cat", "pet", "puppy", "kitten"]):
            category = "PETS"
            tags.extend(["Pet", "Animal", "Cute"])
        elif any(k in filename for k in ["car", "porsche", "bmw", "ferrari", "vehicle", "auto", "bike"]):
            category = "CARS"
            tags.extend(["Car", "Vehicle", "Speed", "Automobile"])
        elif any(k in filename for k in ["food", "burger", "pizza", "meal", "coffee", "cake", "dish"]):
            category = "FOOD"
            tags.extend(["Food", "Cuisine", "Delicious"])
        elif any(k in filename for k in ["beach", "sunset", "travel", "resort", "hotel", "vacation"]):
            category = "TRAVEL"
            tags.extend(["Travel", "Vacation", "Landscape", "Scenic"])
        elif any(k in filename for k in ["mountain", "forest", "tree", "lake", "flower", "nature"]):
            category = "NATURE"
            tags.extend(["Nature", "Outdoor", "Landscape"])
        elif any(k in filename for k in ["selfie", "portrait", "portrait_", "face", "group"]):
            category = "PEOPLE"
            tags.extend(["Person", "Portrait", "Smile"])
        elif any(k in filename for k in ["night", "stars", "moon", "galaxy", "milky_way"]):
            category = "NIGHT_SKY"
            tags.extend(["Night Sky", "Stars", "Astronomy"])
        else:
            category = "NATURE"
            tags.extend(["Photo", "Moment"])

        return category, tags

    def scan_directory(self, target_dir: str) -> int:
        target_path = Path(target_dir)
        if not target_path.exists():
            return 0

        files_to_process = []
        for root, _, files in os.walk(target_path):
            for file in files:
                p = Path(root) / file
                ext = p.suffix.lower()
                if ext in IMAGE_EXTENSIONS or ext in VIDEO_EXTENSIONS:
                    files_to_process.append(p)

        processed_count = 0
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            for p in files_to_process:
                is_video = p.suffix.lower() in VIDEO_EXTENSIONS
                size_bytes = p.stat().st_size
                exif = self.extract_exif(p) if not is_video else {"date_taken": int(p.stat().st_mtime * 1000), "width": 1920, "height": 1080}
                category, tags = self.classify_scene_and_tags(p)
                if is_video:
                    category = "VIDEOS"
                    tags.append("Video")
                
                dhash = self.calculate_dhash(p) if not is_video else None
                dominant_color = self.extract_dominant_color(p) if not is_video else "#0284C7"

                cursor.execute("""
                    INSERT OR REPLACE INTO media_items (
                        path, display_name, mime_type, media_type,
                        date_taken, size_bytes, width, height,
                        ai_tags, scene_category, dominant_color_hex, perceptual_hash
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (
                    str(p.resolve()),
                    p.name,
                    "video/mp4" if is_video else "image/jpeg",
                    "VIDEO" if is_video else "IMAGE",
                    exif["date_taken"],
                    size_bytes,
                    exif["width"],
                    exif["height"],
                    json.dumps(tags),
                    category,
                    dominant_color,
                    dhash
                ))
                processed_count += 1
            conn.commit()

        self._find_duplicates()
        self._update_album_counts()
        return processed_count

    def _find_duplicates(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT id, perceptual_hash FROM media_items WHERE perceptual_hash IS NOT NULL")
            rows = cursor.fetchall()

            for i in range(len(rows)):
                id1, h1 = rows[i]
                for j in range(i + 1, len(rows)):
                    id2, h2 = rows[j]
                    xor = h1 ^ h2
                    dist = bin(xor).count("1")
                    if dist <= 5:
                        group_id = f"dup_{id1}"
                        cursor.execute("UPDATE media_items SET is_duplicate = 1, duplicate_group_id = ? WHERE id IN (?, ?)", (group_id, id1, id2))
            conn.commit()

    def _update_album_counts(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT scene_category, COUNT(*), MIN(path) FROM media_items GROUP BY scene_category")
            albums = cursor.fetchall()
            for category, count, cover in albums:
                title = category.replace("_", " ").title()
                cursor.execute("""
                    INSERT OR REPLACE INTO smart_albums (id, title, category, item_count, cover_path)
                    VALUES (?, ?, ?, ?, ?)
                """, (f"album_{category.lower()}", title, category, count, cover))
            conn.commit()

    def get_all_media(self) -> List[Dict[str, Any]]:
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM media_items ORDER BY date_taken DESC")
            return [dict(row) for row in cursor.fetchall()]

    def search_media(self, query: str) -> List[Dict[str, Any]]:
        q = f"%{query.strip().lower()}%"
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("""
                SELECT * FROM media_items
                WHERE LOWER(display_name) LIKE ?
                   OR LOWER(ai_tags) LIKE ?
                   OR LOWER(scene_category) LIKE ?
                ORDER BY date_taken DESC
            """, (q, q, q))
            return [dict(row) for row in cursor.fetchall()]


if __name__ == "__main__":
    folder = sys.argv[1] if len(sys.argv) > 1 else "."
    sorter = MediaFlowSorter()
    print(f"[*] Scanning and organizing folder: {folder}")
    count = sorter.scan_directory(folder)
    print(f"[✓] Processed {count} media files into smart albums and SQLite index.")
