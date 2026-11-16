# MediaFlow AI — Comprehensive Implementation Plan

A phased development, refinement, and delivery plan for **MediaFlow AI** — a premium Android on-device media organizer built with **Jetpack Compose, Material Design 3, Google ML Kit, Room, and MVVM Clean Architecture**.

---

## 🐍 Python-First Architecture (Zero Trade-off Integration)

MediaFlow AI adopts a **Python-First** execution pipeline without sacrificing 60 FPS Material 3 gesture physics or Google Play compatibility:

1. **Python as the Brain (`mediaflow_engine.py`)**:
   - **Perceptual dHash & pHash**: Calculated directly in Python from raw byte streams.
   - **Smart Album Routing**: Categorizes photos into *People, Travel, Nature, Cars, Food, Pets, Documents, Screenshots, Videos, Night Sky*.
   - **Duplicate Graph Clustering**: Disjoint-set (Union-Find) clustering algorithm running in Python, calculating exact recoverable byte savings.
   - **Semantic Search Ranker**: Python natural language tokenizer and relevance scoring.
   - **Dominant Color Extractor**: PIL-based palette extractor for dynamic theme accents.

2. **Kotlin as the Display Shell**:
   - Exclusively renders the high-performance Jetpack Compose UI (dynamic 2–4 column grid, sticky date headers, Arc top bar, fullscreen viewer with pinch zoom, and animated bottom sheets).
   - [PythonBridge.kt](file:///e:/PROJECTS/images-sort/app/src/main/java/com/mediaflow/ai/ai/PythonBridge.kt) acts as the bridge connecting Kotlin directly to `mediaflow_engine.py`.

3. **Standalone Python Suite (`python_engine/`)**:
   - `python python_engine/run.py scan <dir>`: CLI batch sorter.
   - `python python_engine/run.py watch <dir>`: Live filesystem observer auto-sorting new files on arrival.
   - `python python_engine/run.py serve`: FastAPI REST + WebSocket media server.

---

## 🏛️ System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MediaFlow AI (Android)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Presentation Layer                                │
│                                                                             │
│  Jetpack Compose UI (Material 3 • 18–24dp Shapes • Purple-Blue Gradients)   │
│  ├── Home Gallery (Dynamic 2–4 cols, Sticky Date Headers, Multi-Select)     │
│  ├── Collections (Smart Albums: People, Travel, Nature, Documents, etc.)    │
│  ├── Natural Language Search ("Dog", "Sunset", "Red Car", "Passport")       │
│  ├── Media Viewer (Fullscreen, Pinch/Double-Tap Zoom, Swipe Dismiss)       │
│  ├── Metadata Bottom Sheet (Camera EXIF, AI Labels, Extracted OCR Text)     │
│  ├── Activity (Background AI Tasks: Sorting, OCR, Duplicates, Faces)        │
│  ├── Settings (Theme, Neural Engine Toggles, Storage Space Cleaner)         │
│  └── Floating Banner ("5 new photos organized" with Slide/Scale/Fade)       │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               ViewModel Layer                               │
│                                                                             │
│  GalleryViewModel     SearchViewModel        CollectionsViewModel           │
│  MediaViewerViewModel ActivityViewModel      SettingsViewModel              │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               Domain Layer                                  │
│                                                                             │
│  Business Logic & Use Cases                                                 │
│  • GetMediaUseCase          • SearchMediaUseCase     • GetAlbumsUseCase     │
│  • ToggleFavoriteUseCase    • DeleteMediaUseCase     • SyncMediaStoreUseCase│
│  • RunAiOrganizationUseCase • GetActivityHistoryUseCase                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             Repository Layer                                │
│                                                                             │
│  MediaRepository       SearchRepository       AiRepository                  │
│  SettingsRepository    ActivityRepository                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                     │                     │                     │
                     ▼                     ▼                     ▼
┌───────────────────────┐  ┌──────────────────────┐  ┌────────────────────────┐
│ Local Database        │  │ Android MediaStore   │  │ Background Services     │
│                       │  │                      │  │                        │
│ Room Database         │  │ Images & Videos      │  │ WorkManager Sync       │
│ Media Metadata        │  │ Albums & Buckets     │  │ MediaObserverService   │
│ AI Tags & Hashes      │  │ Hardware Thumbnails  │  │ ContentObserver        │
│ Search Index          │  │ Scoped Storage       │  │ NotificationManager    │
└───────────────────────┘  └──────────────────────┘  └────────────────────────┘
                     │                     │                     │
                     └──────────────┬──────┴──────────────┬──────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AI Neural Pipeline                             │
│                                                                             │
│  • Image Classifier (Google ML Kit ImageLabeling)                           │
│  • OCR Engine (Google ML Kit TextRecognition)                               │
│  • Face Detection & Grouping (Google ML Kit FaceDetection)                  │
│  • Perceptual dHash Duplicate Detector (64-bit difference hash)             │
│  • Dominant Color & Palette Extractor                                       │
│  • 100% On-Device Neural Processing (Zero Cloud Telemetry)                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📅 Phased Implementation Plan

### Phase 1: Architecture, Build & Design System Foundation
* **Objective**: Configure modern Android build tools, dependency injection, and core Material 3 design tokens.
* **Key Tasks**:
  1. Configure Gradle Version Catalog in `gradle/libs.versions.toml` (Kotlin 2.0.21, Compose BOM 2024.11.00, AGP 8.7.2).
  2. Implement Hilt DI bindings for Database and Repositories (`DatabaseModule.kt`, `RepositoryModule.kt`).
  3. Define visual design language:
     - 18–24dp rounded corners (`Shape.kt`).
     - Purple-to-Electric Blue gradient accents (`#7C3AED` to `#2563EB`) in `Color.kt`.
     - Frosted glassmorphism container with subtle gradient border and soft ambient shadows (`GlassmorphicSurface.kt`).
* **Deliverable Files**:
  - `gradle/libs.versions.toml`
  - `app/build.gradle.kts`
  - `app/src/main/java/com/mediaflow/ai/ui/theme/*`
  - `app/src/main/java/com/mediaflow/ai/ui/components/GlassmorphicSurface.kt`

---

### Phase 2: MediaStore Ingestion & Room Persistence
* **Objective**: Interface with Android Scoped Storage to index photos and videos reactively.
* **Key Tasks**:
  1. Configure storage permissions in `AndroidManifest.xml` (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED` on Android 13+, legacy storage on Android 12).
  2. Implement `MediaStoreScanner.kt` to query device storage with content URIs, dimensions, MIME types, duration, and timestamps.
  3. Set up Room Database (`MediaFlowDatabase.kt`) with tables for `media_items`, `smart_albums`, and `activity_tasks`.
  4. Create compound database indexes on `dateTaken`, `sceneCategory`, `isFavorite`, and `perceptualHash` for sub-millisecond lookups.
* **Deliverable Files**:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/mediaflow/ai/data/mediastore/MediaStoreScanner.kt`
  - `app/src/main/java/com/mediaflow/ai/data/local/MediaFlowDatabase.kt`
  - `app/src/main/java/com/mediaflow/ai/data/local/dao/MediaDao.kt`

---

### Phase 3: On-Device AI Vision & Duplicate Detection
* **Objective**: Run fast, battery-efficient, 100% on-device neural processing without sending data to the cloud.
* **Key Tasks**:
  1. **Visual Classification Engine** (`ImageClassifierEngine.kt`): Use Google ML Kit Vision to extract objects and infer scene categories (Nature, Travel, Food, Cars, Pets, Night Sky).
  2. **OCR Engine** (`OcrEngine.kt`): Extract text from documents, receipts, screenshots, and passes.
  3. **Face Detection** (`FaceDetectionEngine.kt`): Detect human faces to cluster portraits, selfies, and group moments.
  4. **Perceptual Duplicate Detection** (`PerceptualHash.kt`): Compute 64-bit difference hash (`dHash`) and Hamming distance ($\le 5$) to flag duplicates and burst photos.
  5. **Downsampled Image Pipeline** (`AiPipeline.kt`): Decode bitmaps with `inSampleSize` to prevent Out-Of-Memory exceptions on 50MP+ camera images.
* **Deliverable Files**:
  - `app/src/main/java/com/mediaflow/ai/ai/AiPipeline.kt`
  - `app/src/main/java/com/mediaflow/ai/ai/ImageClassifierEngine.kt`
  - `app/src/main/java/com/mediaflow/ai/ai/OcrEngine.kt`
  - `app/src/main/java/com/mediaflow/ai/ai/FaceDetectionEngine.kt`
  - `app/src/main/java/com/mediaflow/ai/ai/PerceptualHash.kt`

---

### Phase 4: Jetpack Compose Presentation & Gestures
* **Objective**: Build a responsive consumer UI inspired by Google Photos, Nothing Gallery, and Arc.
* **Key Tasks**:
  1. **Gallery Screen** (`GalleryScreen.kt`):
     - Dynamic 2x, 3x, 4x column layout switcher.
     - Sticky calendar date headers (*"Today"*, *"Yesterday"*, calendar dates).
     - Long-press multi-selection mode with batch actions (Share, Delete).
     - Adaptive thumbnail cards with video badges and animated heart favorites.
  2. **Collapsible Top Bar** (`CollapsibleTopBar.kt`): Arc-inspired pill search bar, notification badge, and profile avatar.
  3. **Real-time Floating Notification Banner** (`RealtimeNotificationBanner.kt`):
     - Displays *"5 new photos organized"* with slide, scale, fade, and glowing status dot.
  4. **Collections Screen** (`CollectionsScreen.kt`):
     - Smart albums (People & Pets, Travel, Nature, Cars, Food, Documents, Screenshots, Videos) with animated touch scale.
  5. **Natural Language Search** (`SearchScreen.kt`):
     - Debounced queries matching visual labels, scene types, OCR text, and filenames.
  6. **Activity Screen** (`ActivityScreen.kt`):
     - Live stream of autonomous tasks (*"12 photos sorted"*, *"Duplicate removed"*, *"OCR completed"*).
  7. **Settings Screen** (`SettingsScreen.kt`):
     - Theme selector, AI toggles, duplicate storage space cleaner, and privacy pledge.
* **Deliverable Files**:
  - `app/src/main/java/com/mediaflow/ai/ui/screens/gallery/*`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/collections/*`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/search/*`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/activity/*`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/settings/*`
  - `app/src/main/java/com/mediaflow/ai/ui/components/*`

---

### Phase 5: Immersive Fullscreen Media Viewer
* **Objective**: Enable fluid, hardware-accelerated photo/video inspection with gestures.
* **Key Tasks**:
  1. Fullscreen black canvas with `HorizontalPager` for smooth swiping across the entire library.
  2. Pinch-to-zoom (up to 4x) and double-tap zoom via gesture transforms.
  3. Swipe-down-to-dismiss gesture returning cleanly to the gallery.
  4. Single-tap to toggle floating top and bottom bars.
  5. **Metadata Bottom Sheet** (`MetadataBottomSheet.kt`):
     - Displays camera EXIF (aperture, ISO, focal length, resolution, size).
     - AI visual tags pill cloud.
     - Selectable and copyable OCR extracted text.
* **Deliverable Files**:
  - `app/src/main/java/com/mediaflow/ai/ui/screens/viewer/MediaViewerScreen.kt`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/viewer/MediaViewerViewModel.kt`
  - `app/src/main/java/com/mediaflow/ai/ui/screens/viewer/MetadataBottomSheet.kt`

---

### Phase 6: Real-time Ingestion & Background Sync
* **Objective**: Automate organization as soon as camera captures or downloads take place.
* **Key Tasks**:
  1. **Foreground MediaObserverService** (`MediaObserverService.kt`):
     - ContentObserver listening on `MediaStore.Images` and `MediaStore.Video`.
     - Automatically triggers AI classification on new media.
  2. **WorkManager Periodic Sync** (`MediaSyncWorker.kt`):
     - Background sync every hour under `BatteryNotLow` constraint.
  3. **Notification Manager** (`MediaNotificationManager.kt`):
     - Dedicated notification channel for *"Media Organized"* updates.
* **Deliverable Files**:
  - `app/src/main/java/com/mediaflow/ai/service/MediaObserverService.kt`
  - `app/src/main/java/com/mediaflow/ai/service/MediaSyncWorker.kt`
  - `app/src/main/java/com/mediaflow/ai/notification/MediaNotificationManager.kt`

---

### Phase 7: Performance Tuning for 100,000+ Items
* **Objective**: Maintain 60 FPS scrolling and low memory footprint on massive libraries.
* **Key Tasks**:
  1. Configure Coil image loader with `VideoFrameDecoder.Factory()` and 25% heap memory cache.
  2. Integrate `Paging 3` (`PagingSource`) for windowed chunk loading (60 items per page).
  3. Limit background neural inference to `Dispatchers.Default.limitedParallelism(2)` to keep CPU thermals cool.
  4. Store perceptual hashes in 64-bit primitive Longs for fast bitwise XOR Hamming distance operations.

---

### Phase 8: Hardening & Google Play Release
* **Objective**: Optimize binary size, verify compatibility, and prepare for distribution.
* **Key Tasks**:
  1. Configure R8 code shrinking and resource minification (`proguard-rules.pro`).
  2. Run automated Unit & Instrumentation tests on Room DAOs and AI pipelines.
  3. Generate signed Android App Bundle (`.aab`).
