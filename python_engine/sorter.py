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

            cursor.execute("SELECT COUNT(*) FROM media_items")
            if cursor.fetchone()[0] == 0:
                self._populate_demo_media(cursor)
                conn.commit()

    def _populate_demo_media(self, cursor):
        now = int(datetime.now().timestamp() * 1000)
        day = 86400000
        samples = [
            ("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80", "IMG_Tropical_Sunset.jpg", "image/jpeg", "IMAGE", now, 4250000, 3840, 2160, 1, 0, None, json.dumps(["Sunset", "Beach", "Ocean", "Golden Hour", "Tropical"]), "TRAVEL", "#F59E0B", 987654321),
            ("https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=1200&q=80", "IMG_Golden_Retriever.jpg", "image/jpeg", "IMAGE", now, 3120000, 4032, 3024, 1, 0, None, json.dumps(["Dog", "Golden Retriever", "Pet", "Puppy", "Cute"]), "PETS", "#D97706", 123456789),
            ("https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80", "IMG_Porsche_911.jpg", "image/jpeg", "IMAGE", now - day, 5800000, 3840, 2560, 0, 0, None, json.dumps(["Red Car", "Porsche", "Supercar", "Speed"]), "CARS", "#DC2626", 555555555),
            ("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80", "IMG_Portrait_Sarah.jpg", "image/jpeg", "IMAGE", now - day, 6100000, 3000, 4000, 1, 0, None, json.dumps(["Portrait", "Smile", "Person", "Girl", "Blue Shirt"]), "PEOPLE", "#8B5CF6", 888888888),
            ("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80", "IMG_Alpine_Peaks.jpg", "image/jpeg", "IMAGE", now - (day * 2), 7400000, 4000, 2250, 0, 0, None, json.dumps(["Mountain", "Snow", "Peak", "Sky", "Nature"]), "NATURE", "#3B82F6", 333333333),
            ("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80", "IMG_Gourmet_Burger.jpg", "image/jpeg", "IMAGE", now - (day * 2), 3900000, 3024, 3024, 0, 0, None, json.dumps(["Food", "Burger", "Cuisine", "Delicious"]), "FOOD", "#EA580C", 444444444),
            ("https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1200&q=80", "IMG_Milky_Way_Night.jpg", "image/jpeg", "IMAGE", now - (day * 3), 8200000, 4032, 3024, 1, 0, None, json.dumps(["Night Sky", "Stars", "Milky Way", "Astronomy"]), "NIGHT_SKY", "#1E1B4B", 222222222),
            ("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=1200&q=80", "DOC_Passport_Scan.jpg", "image/jpeg", "IMAGE", now - (day * 4), 2100000, 2480, 3508, 0, 0, None, json.dumps(["Document", "Passport", "Contract", "Text"]), "DOCUMENTS", "#64748B", 111111111),
            ("https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80", "Screenshot_Code_Snippet.png", "image/png", "IMAGE", now - (day * 5), 1400000, 1080, 2400, 0, 0, None, json.dumps(["Screenshot", "Technology", "Code", "Android"]), "SCREENSHOTS", "#0F172A", 777777777)
        ]
        for s in samples:
            cursor.execute("""
                INSERT OR REPLACE INTO media_items (
                    path, display_name, mime_type, media_type, date_taken, size_bytes,
                    width, height, is_favorite, is_duplicate, duplicate_group_id,
                    ai_tags, scene_category, dominant_color_hex, perceptual_hash
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, s)

        albums = [
            ("album_people", "People & Pets", "PEOPLE", 142, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"),
            ("album_travel", "Travel & Places", "TRAVEL", 89, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80"),
            ("album_nature", "Nature & Mountains", "NATURE", 215, "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=600&q=80"),
            ("album_cars", "Cars & Vehicles", "CARS", 48, "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80"),
            ("album_food", "Food & Dining", "FOOD", 63, "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=600&q=80"),
            ("album_documents", "Documents & Receipts", "DOCUMENTS", 37, "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80"),
            ("album_screenshots", "Screenshots", "SCREENSHOTS", 184, "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80"),
            ("album_videos", "Videos & Clips", "VIDEOS", 52, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80")
        ]
        for a in albums:
            cursor.execute("""
                INSERT OR REPLACE INTO smart_albums (id, title, category, item_count, cover_path)
                VALUES (?, ?, ?, ?, ?)
            """, a)

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
        Infers scene category and tags using visual pixel analysis (skin/face detection,
        aspect ratios, color balance) and metadata heuristics.
        """
        filename = img_path.name.lower()

        # 1. Filename explicit overrides
        if any(k in filename for k in ["screenshot", "screen_shot", "capture"]):
            return "SCREENSHOTS", ["Screenshot", "UI"]
        if any(k in filename for k in ["receipt", "invoice", "bill", "doc", "passport", "license"]):
            return "DOCUMENTS", ["Document", "Receipt"]
        if any(k in filename for k in ["pet", "dog", "cat", "puppy", "kitten"]):
            return "PETS", ["Pet", "Animal"]
        if any(k in filename for k in ["car", "auto", "bike", "vehicle", "porsche", "bmw"]):
            return "CARS", ["Vehicle", "Things"]
        if any(k in filename for k in ["food", "meal", "dinner", "burger", "pizza", "dessert"]):
            return "FOOD", ["Food", "Dining"]

        # 2. Pixel / Vision inspection using PIL
        try:
            with Image.open(img_path) as img:
                w, h = img.size
                aspect = h / max(1, w)
                
                # Check for mobile screenshots (tall aspect ratio like 19.5:9, 20:9)
                if aspect > 1.85:
                    return "SCREENSHOTS", ["Screenshot", "Mobile Capture"]

                # Downsample for ultra-fast pixel heuristics
                thumb = img.convert("RGB").resize((80, 80))
                pixels = list(thumb.getdata())
                total = len(pixels)

                skin_pixels = 0
                green_pixels = 0
                sky_pixels = 0
                food_pixels = 0
                gray_pixels = 0

                for r, g, b in pixels:
                    # Skin tone heuristic (universal human skin color rule)
                    if r > 60 and g > 30 and b > 15 and r > g and g >= b and (r - g) >= 8 and (r - b) >= 12:
                        skin_pixels += 1
                    
                    if g > 75 and g > r * 1.15 and g > b * 1.15:
                        green_pixels += 1
                    
                    if b > 110 and b > r * 1.2 and b > g * 1.05:
                        sky_pixels += 1

                    if r > 140 and g > 60 and b < 100:
                        food_pixels += 1

                    if abs(r - g) < 15 and abs(g - b) < 15:
                        gray_pixels += 1

                skin_ratio = skin_pixels / total
                green_ratio = green_pixels / total
                sky_ratio = sky_pixels / total
                food_ratio = food_pixels / total
                gray_ratio = gray_pixels / total

                # Human Face / Portrait / Selfie Detection
                if skin_ratio > 0.10:
                    if skin_ratio > 0.20 or aspect > 1.1:
                        return "PEOPLE", ["Selfie", "Portrait", "Face"]
                    else:
                        return "FAMILY", ["Family & Friends", "People"]

                if gray_ratio > 0.60:
                    return "DOCUMENTS", ["Document", "Text"]

                if food_ratio > 0.20:
                    return "FOOD", ["Food", "Meal"]

                if sky_ratio > 0.15 and sky_ratio > green_ratio:
                    return "TRAVEL", ["Travel", "Sky", "Outdoor"]

                if green_ratio > 0.15:
                    return "NATURE", ["Nature", "Greenery", "Outdoor"]

                # Aspect ratio portrait default to People/Family
                if aspect > 1.2:
                    return "PEOPLE", ["Portrait", "Person"]

        except Exception:
            pass

        return "TRAVEL", ["Travel", "Moment"]

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

    def organize_and_export(self, source_dir: str, dest_dir: Optional[str] = None, mode: str = "copy") -> Dict[str, Any]:
        """
        Physically sorts and organizes files from source_dir into categorized subdirectories in dest_dir.
        Categories match the 11 smart folders:
        Selfies, Events & weddings, Travel, Screenshots, Receipts & docs,
        Duplicates, Family & friends, Food, Nature & views, Animals & pets, Things & shopping.
        """
        source_path = Path(source_dir).resolve()
        if not source_path.exists():
            return {"status": "error", "message": f"Source directory does not exist: {source_dir}"}

        if not dest_dir:
            dest_path = source_path / "MediaFlow_Sorted"
        else:
            dest_path = Path(dest_dir).resolve()

        dest_path.mkdir(parents=True, exist_ok=True)

        # First scan & classify
        self.scan_directory(str(source_path))

        category_folder_map = {
            "PEOPLE": "Selfies_and_Portraits",
            "EVENTS": "Events_and_Weddings",
            "TRAVEL": "Travel",
            "SCREENSHOTS": "Screenshots",
            "DOCUMENTS": "Receipts_and_Docs",
            "DUPLICATES": "Duplicates",
            "FAMILY": "Family_and_Friends",
            "FOOD": "Food",
            "NATURE": "Nature_and_Views",
            "PETS": "Animals_and_Pets",
            "CARS": "Things_and_Shopping",
            "GENERAL": "General_Photos",
            "VIDEOS": "Videos"
        }

        # Ensure all folders exist
        for folder_name in category_folder_map.values():
            (dest_path / folder_name).mkdir(exist_ok=True)

        sorted_stats = {folder: 0 for folder in category_folder_map.values()}
        total_organized = 0

        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("SELECT path, display_name, scene_category, is_duplicate FROM media_items WHERE path LIKE ?", (f"{str(source_path)}%",))
            rows = cursor.fetchall()

            for row in rows:
                src_file = Path(row["path"])
                if not src_file.exists():
                    continue

                if row["is_duplicate"] == 1:
                    target_folder_name = "Duplicates"
                else:
                    cat = row["scene_category"] or "GENERAL"
                    target_folder_name = category_folder_map.get(cat, "General_Photos")

                target_dest = dest_path / target_folder_name / src_file.name

                # Prevent overwriting identically named files
                if target_dest.exists() and target_dest != src_file:
                    stem = src_file.stem
                    suffix = src_file.suffix
                    target_dest = dest_path / target_folder_name / f"{stem}_{int(datetime.now().timestamp())}{suffix}"

                try:
                    if mode == "move":
                        shutil.move(str(src_file), str(target_dest))
                    else:
                        shutil.copy2(str(src_file), str(target_dest))
                    sorted_stats[target_folder_name] += 1
                    total_organized += 1
                except Exception as e:
                    print(f"Error organizing {src_file}: {e}")

        return {
            "status": "success",
            "source": str(source_path),
            "destination": str(dest_path),
            "total_organized": total_organized,
            "mode": mode,
            "breakdown": sorted_stats
        }

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
