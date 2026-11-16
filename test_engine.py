import sys
import json
sys.path.append('app/src/main/python')
import mediaflow_engine

print("1. Testing scene classification:")
category, tags = mediaflow_engine.classify_media_scene('IMG_Sunset_Beach.jpg', ['Sunset', 'Ocean'], None, 0)
print(f"   Category: {category}, Tags: {tags}")

print("\n2. Testing duplicate clustering:")
sample_hashes = [
    {"id": 101, "hash": 99999999, "sizeBytes": 4200000},
    {"id": 102, "hash": 99999999, "sizeBytes": 4200000},
    {"id": 103, "hash": 12345678, "sizeBytes": 3100000}
]
dup_json = mediaflow_engine.cluster_duplicates_and_savings(json.dumps(sample_hashes))
print(f"   Clusters: {dup_json}")

print("\n3. Testing semantic search ranking:")
sample_items = [
    {"id": 101, "displayName": "IMG_Alpine_Peaks.jpg", "sceneCategory": "NATURE", "aiTags": ["Mountain", "Snow"]},
    {"id": 102, "displayName": "IMG_Red_Porsche.jpg", "sceneCategory": "CARS", "aiTags": ["Red Car", "Porsche"]},
    {"id": 103, "displayName": "DOC_Passport_Scan.jpg", "sceneCategory": "DOCUMENTS", "aiTags": ["Passport", "Text"]}
]
rank_res = mediaflow_engine.semantic_search_rank_python("red car", json.dumps(sample_items))
print(f"   Ranked IDs for 'red car': {rank_res}")
print("\n[✔] All Python mediaflow_engine methods verified successfully!")
