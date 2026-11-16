"""
MediaFlow AI — Core Python Neural & Media Intelligence Engine
Handles all business logic, duplicate clustering, perceptual hashing,
smart categorization, semantic search ranking, and storage analytics.
"""

import io
import os
import json
import math
import re
from typing import List, Dict, Any, Tuple, Optional
from PIL import Image

# ---------------------------------------------------------------------------
# 1. PERCEPTUAL HASHING (dHash & pHash)
# ---------------------------------------------------------------------------

def calculate_dhash(image_bytes: bytes, hash_size: int = 8) -> int:
    """
    Computes a 64-bit difference hash (dHash) from image bytes.
    Ultra-fast and resistant to compression, gamma, and resolution scaling.
    """
    try:
        with Image.open(io.BytesIO(image_bytes)) as img:
            img = img.convert("L").resize((hash_size + 1, hash_size), Image.Resampling.BILINEAR)
            pixels = list(img.getdata())
            diff = []
            for row in range(hash_size):
                for col in range(hash_size):
                    left = pixels[row * (hash_size + 1) + col]
                    right = pixels[row * (hash_size + 1) + col + 1]
                    diff.append(left > right)
            
            hash_val = 0
            for idx, val in enumerate(diff):
                if val:
                    hash_val |= 1 << idx
            return hash_val
    except Exception:
        return 0


def calculate_hamming_distance(hash1: int, hash2: int) -> int:
    """Calculates bitwise Hamming distance between two 64-bit perceptual hashes."""
    xor = hash1 ^ hash2
    distance = 0
    while xor:
        distance += xor & 1
        xor >>= 1
    return distance


# ---------------------------------------------------------------------------
# 2. COLOR PALETTE & DOMINANT TONE EXTRACTION
# ---------------------------------------------------------------------------

def extract_dominant_color_hex(image_bytes: bytes) -> str:
    """Extracts the dominant aesthetic color from an image for UI accent matching."""
    try:
        with Image.open(io.BytesIO(image_bytes)) as img:
            img = img.convert("RGB").resize((16, 16), Image.Resampling.BILINEAR)
            colors = img.getcolors(256)
            if not colors:
                return "#6750A4"
            colors.sort(key=lambda c: c[0], reverse=True)
            r, g, b = colors[0][1]
            return f"#{r:02X}{g:02X}{b:02X}"
    except Exception:
        return "#6750A4"


# ---------------------------------------------------------------------------
# 3. SCENE & SMART ALBUM CLASSIFICATION
# ---------------------------------------------------------------------------

def classify_media_scene(
    filename: str,
    tags: List[str],
    ocr_text: Optional[str] = None,
    face_count: int = 0
) -> Tuple[str, List[str]]:
    """
    Classifies media into a Smart Album category and generates enriched AI tags.
    Smart Albums:
      - PEOPLE: Portraits, selfies, group photos, smiles
      - TRAVEL: Beaches, resorts, monuments, vacations
      - NATURE: Mountains, lakes, forests, peaks, skies
      - CARS: Automobiles, supercars, vehicles, bikes
      - FOOD: Meals, dishes, coffee, restaurants, desserts
      - PETS: Dogs, cats, puppies, kittens
      - ANIMALS: Wildlife, birds, safari
      - DOCUMENTS: Scanned text, passports, bills, contracts, receipts
      - SCREENSHOTS: UI captures, code snippets, chat screenshots
      - NIGHT_SKY: Astronomy, milky way, stars
      - VIDEOS: Video clips
    """
    lower_name = filename.lower()
    lower_tags = [t.lower() for t in tags]
    text = (ocr_text or "").lower()

    enriched_tags = list(tags)
    category = "GENERAL"

    # Prioritize face presence
    if face_count > 0:
        enriched_tags.append(f"{face_count} Person{'s' if face_count > 1 else ''}")
        if face_count == 1 and ("selfie" in lower_name or "portrait" in lower_name):
            category = "PEOPLE"
            enriched_tags.append("Selfie")
        elif face_count >= 2:
            category = "PEOPLE"
            enriched_tags.append("Group")

    # Document & OCR signals
    if any(k in lower_name for k in ["doc_", "receipt", "invoice", "passport", "contract", "license", "bill"]) \
       or "passport" in text or "invoice" in text or "total:" in text or "license" in text:
        category = "DOCUMENTS"
        enriched_tags.extend(["Document", "Text"])

    # Screenshot signals
    elif "screenshot" in lower_name or "screen_shot" in lower_name or "@composable" in text or "http" in text:
        category = "SCREENSHOTS"
        enriched_tags.extend(["Screenshot", "Software UI"])

    # Pets signals
    elif any(k in lower_tags for k in ["dog", "cat", "pet", "puppy", "kitten"]) or any(k in lower_name for k in ["dog", "cat", "puppy"]):
        category = "PETS"
        enriched_tags.append("Pets")

    # Cars signals
    elif any(k in lower_tags for k in ["car", "vehicle", "automobile", "sports car", "porsche", "ferrari", "motorcycle"]) \
         or any(k in lower_name for k in ["car", "porsche", "bmw", "ferrari"]):
        category = "CARS"
        enriched_tags.append("Vehicle")

    # Food signals
    elif any(k in lower_tags for k in ["food", "meal", "burger", "pizza", "cuisine", "dessert", "coffee", "restaurant"]) \
         or any(k in lower_name for k in ["food", "burger", "dinner", "lunch"]):
        category = "FOOD"
        enriched_tags.append("Food & Dining")

    # Travel & Vacation signals
    elif any(k in lower_tags for k in ["beach", "resort", "hotel", "vacation", "monument", "tropical", "sunset"]) \
         or any(k in lower_name for k in ["beach", "sunset", "travel", "resort", "vacation"]):
        category = "TRAVEL"
        enriched_tags.append("Travel")

    # Nature & Mountains signals
    elif any(k in lower_tags for k in ["mountain", "snow", "peak", "forest", "tree", "nature", "lake", "ocean", "river"]) \
         or any(k in lower_name for k in ["mountain", "forest", "nature", "alps"]):
        category = "NATURE"
        enriched_tags.append("Nature")

    # Night Sky & Astronomy
    elif any(k in lower_tags for k in ["night", "stars", "astronomy", "milky way", "galaxy"]) \
         or any(k in lower_name for k in ["night", "milky_way", "stars"]):
        category = "NIGHT_SKY"
        enriched_tags.append("Night Sky")

    elif category == "GENERAL" and face_count > 0:
        category = "PEOPLE"

    # Deduplicate tags preserving order
    seen = set()
    unique_tags = []
    for t in enriched_tags:
        if t not in seen:
            seen.add(t)
            unique_tags.append(t)

    return category, unique_tags


# ---------------------------------------------------------------------------
# 4. COMPREHENSIVE ON-DEVICE MEDIA ANALYSIS (CALLED FROM KOTLIN)
# ---------------------------------------------------------------------------

def analyze_media_full(
    image_bytes: bytes,
    filename: str,
    raw_tags_json: str,
    ocr_text: Optional[str] = None,
    face_count: int = 0
) -> str:
    """
    Main entry point for Android app processing.
    Executes hashing, dominant color extraction, and smart categorization in Python.
    Returns JSON string with full analysis payload.
    """
    try:
        raw_tags = json.loads(raw_tags_json) if raw_tags_json else []
    except Exception:
        raw_tags = []

    dhash = calculate_dhash(image_bytes)
    color = extract_dominant_color_hex(image_bytes)
    category, final_tags = classify_media_scene(filename, raw_tags, ocr_text, face_count)

    result = {
        "perceptualHash": dhash,
        "dominantColorHex": color,
        "sceneCategory": category,
        "aiTags": final_tags,
        "isProcessed": True
    }
    return json.dumps(result)


# ---------------------------------------------------------------------------
# 5. DUPLICATE CLUSTERING & STORAGE SAVINGS (GRAPH UNION-FIND)
# ---------------------------------------------------------------------------

def cluster_duplicates_and_savings(hashes_json: str, threshold: int = 5) -> str:
    """
    Clusters duplicate photos using a disjoint-set (Union-Find) algorithm.
    Calculates total recoverable storage space in bytes.
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

        clusters: Dict[int, List[Dict[str, Any]]] = {}
        for i in range(n):
            root = find(i)
            clusters.setdefault(root, []).append(items[i])

        # Filter out clusters with only 1 item
        duplicate_groups = []
        total_recoverable_bytes = 0

        for root, group in clusters.items():
            if len(group) > 1:
                # Keep first, others are recoverable
                recoverable_in_group = sum(item.get("sizeBytes", 0) for item in group[1:])
                total_recoverable_bytes += recoverable_in_group
                duplicate_groups.append({
                    "groupId": f"dup_{group[0]['id']}",
                    "itemIds": [item["id"] for item in group],
                    "count": len(group),
                    "recoverableBytes": recoverable_in_group
                })

        return json.dumps({
            "duplicateGroups": duplicate_groups,
            "totalDuplicatesFound": sum(len(g["itemIds"]) for g in duplicate_groups),
            "totalRecoverableBytes": total_recoverable_bytes
        })
    except Exception as e:
        return json.dumps({"duplicateGroups": [], "totalDuplicatesFound": 0, "totalRecoverableBytes": 0})


# ---------------------------------------------------------------------------
# 6. NATURAL LANGUAGE SEMANTIC SEARCH ENGINE
# ---------------------------------------------------------------------------

def semantic_search_rank_python(query: str, items_json: str) -> str:
    """
    Ranks items with natural language keyword scoring.
    Tokenizes input query and weighs matches against filenames, scene categories,
    AI visual tags, and extracted OCR text.
    """
    try:
        items = json.loads(items_json)
        tokens = [t.lower() for t in re.findall(r"\w+", query)]
        if not tokens:
            return json.dumps([item["id"] for item in items])

        scored = []
        for item in items:
            score = 0
            name = item.get("displayName", "").lower()
            scene = item.get("sceneCategory", "").lower()
            tags = [t.lower() for t in item.get("aiTags", [])]
            ocr = (item.get("ocrText") or "").lower()

            for token in tokens:
                # Exact name match gets highest weight
                if token in name:
                    score += 25
                # Smart album category match
                if token in scene:
                    score += 20
                # Visual tag match
                if any(token in t for t in tags):
                    score += 15
                # OCR text match
                if token in ocr:
                    score += 12

            if score > 0:
                scored.append((score, item["id"]))

        scored.sort(key=lambda x: x[0], reverse=True)
        return json.dumps([item_id for _, item_id in scored])
    except Exception:
        return json.dumps([])
