"""
MediaFlow AI — Embedded Python Neural & Image Sorting Engine
Compatible with Chaquopy for on-device Android execution and standalone Python environments.
"""

import io
import os
import json
import math
from typing import List, Dict, Any, Tuple, Optional
from PIL import Image

def calculate_dhash(image_bytes: bytes, hash_size: int = 8) -> int:
    """
    Computes a 64-bit difference hash (dHash) from raw image bytes.
    Resistant to scaling, compression, and subtle color variance.
    """
    try:
        with Image.open(io.BytesIO(image_bytes)) as img:
            # Convert to grayscale and resize to (hash_size + 1, hash_size)
            img = img.convert("L").resize((hash_size + 1, hash_size), Image.Resampling.BILINEAR)
            pixels = list(img.getdata())
            
            diff = []
            for row in range(hash_size):
                for col in range(hash_size):
                    left_pixel = pixels[row * (hash_size + 1) + col]
                    right_pixel = pixels[row * (hash_size + 1) + col + 1]
                    diff.append(left_pixel > right_pixel)
            
            decimal_val = 0
            for idx, val in enumerate(diff):
                if val:
                    decimal_val |= 1 << idx
            return decimal_val
    except Exception as e:
        return 0


def calculate_hamming_distance(hash1: int, hash2: int) -> int:
    """Computes bitwise Hamming distance between two 64-bit hashes."""
    xor = hash1 ^ hash2
    distance = 0
    while xor:
        distance += xor & 1
        xor >>= 1
    return distance


def extract_dominant_color_hex(image_bytes: bytes) -> str:
    """Extracts dominant color hex from an image."""
    try:
        with Image.open(io.BytesIO(image_bytes)) as img:
            img = img.convert("RGB").resize((24, 24), Image.Resampling.BILINEAR)
            colors = img.getcolors(24 * 24)
            if not colors:
                return "#6750A4"
            # Sort by count descending
            colors.sort(key=lambda c: c[0], reverse=True)
            dominant = colors[0][1]
            return f"#{dominant[0]:02X}{dominant[1]:02X}{dominant[2]:02X}"
    except Exception:
        return "#6750A4"


def infer_scene_category(tags: List[str], text_content: Optional[str] = None) -> str:
    """Classifies media into smart album categories based on visual tags & OCR text."""
    lower_tags = [t.lower() for t in tags]
    combined_text = (text_content or "").lower()
    
    if any(k in lower_tags for k in ["dog", "cat", "pet", "puppy", "kitten"]):
        return "PETS"
    if any(k in lower_tags for k in ["animal", "wildlife", "bird", "lion", "tiger"]):
        return "ANIMALS"
    if any(k in lower_tags for k in ["car", "vehicle", "automobile", "porsche", "ferrari", "supercar", "motorcycle"]):
        return "CARS"
    if any(k in lower_tags for k in ["food", "meal", "burger", "pizza", "coffee", "restaurant", "dessert", "cuisine"]):
        return "FOOD"
    if any(k in lower_tags for k in ["mountain", "lake", "forest", "tree", "river", "nature", "snow", "flower"]):
        return "NATURE"
    if any(k in lower_tags for k in ["beach", "resort", "hotel", "travel", "vacation", "monument", "tropical", "sunset"]):
        return "TRAVEL"
    if any(k in lower_tags for k in ["document", "paper", "receipt", "passport", "contract", "letter"]) or "passport" in combined_text or "invoice" in combined_text or "total:" in combined_text:
        return "DOCUMENTS"
    if any(k in lower_tags for k in ["screenshot", "multimedia", "software", "ui"]) or "@composable" in combined_text or "http" in combined_text:
        return "SCREENSHOTS"
    if any(k in lower_tags for k in ["person", "portrait", "selfie", "girl", "boy", "smile", "crowd", "people"]):
        return "PEOPLE"
    if any(k in lower_tags for k in ["night", "stars", "astronomy", "milky way", "galaxy"]):
        return "NIGHT_SKY"
        
    return "GENERAL"


def semantic_search_rank(query: str, items_json: str) -> str:
    """
    Ranks media items based on natural language query match against tags, filename, OCR text and scene.
    Returns JSON string of ranked IDs.
    """
    try:
        items = json.loads(items_json)
        q_tokens = query.lower().strip().split()
        if not q_tokens:
            return json.dumps([item["id"] for item in items])
        
        scored_items = []
        for item in items:
            score = 0
            name = item.get("displayName", "").lower()
            scene = item.get("sceneCategory", "").lower()
            tags = [t.lower() for t in item.get("aiTags", [])]
            ocr = (item.get("ocrText") or "").lower()
            
            for token in q_tokens:
                if token in name:
                    score += 15
                if token in scene:
                    score += 12
                if any(token in t for t in tags):
                    score += 10
                if token in ocr:
                    score += 8
                    
            if score > 0:
                scored_items.append((score, item["id"]))
                
        scored_items.sort(key=lambda x: x[0], reverse=True)
        return json.dumps([item_id for _, item_id in scored_items])
    except Exception as e:
        return json.dumps([])


def cluster_duplicates(hashes_json: str, threshold: int = 5) -> str:
    """
    Clusters media items with Hamming distance <= threshold.
    Input: JSON string of list of objects with {"id": long, "hash": long}
    Returns: JSON string mapping groupId -> list of media IDs.
    """
    try:
        items = json.loads(hashes_json)
        n = len(items)
        parent = list(range(n))
        
        def find(i):
            if parent[i] == i:
                return i
            parent[i] = find(parent[i])
            return parent[i]
            
        def union(i, j):
            root_i = find(i)
            root_j = find(j)
            if root_i != root_j:
                parent[root_i] = root_j

        for i in range(n):
            h1 = items[i].get("hash")
            if h1 is None:
                continue
            for j in range(i + 1, n):
                h2 = items[j].get("hash")
                if h2 is None:
                    continue
                if calculate_hamming_distance(h1, h2) <= threshold:
                    union(i, j)
                    
        groups: Dict[int, List[int]] = {}
        for i in range(n):
            root = find(i)
            groups.setdefault(root, []).append(items[i]["id"])
            
        # Only keep groups with > 1 item
        duplicate_clusters = {f"group_{k}": v for k, v in groups.items() if len(v) > 1}
        return json.dumps(duplicate_clusters)
    except Exception:
        return json.dumps({})
