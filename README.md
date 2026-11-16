# MediaFlow AI 📸✨

> **Intelligent, real-time AI-powered media organizer for Android.**  
> Crafted with Jetpack Compose, Material Design 3, Google ML Kit, Room, and modern clean architecture. Inspired by Google Photos, Nothing Gallery, Apple Photos, Samsung Gallery, Arc, and Material You.

---

## 🌟 Overview & Design Philosophy

MediaFlow AI is designed from the ground up to **NOT** look like a traditional dashboard, file manager, or utility app. Instead, it delivers a smooth, tactile consumer experience with:

- **18–24dp Rounded Aesthetics** & Floating Glassmorphic Surfaces
- **Purple to Electric Blue Accent Gradients** (`#7C3AED` ➔ `#2563EB`)
- **60 FPS Hardware-Accelerated Animations** (Spring physics, Hero transitions, Parallax gestures)
- **100% On-Device Neural Processing** (Zero cloud telemetry, complete privacy)
- **Scalable Architecture**: Engineered to effortlessly index and organize **100,000+ photos** and **20,000+ videos**

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MediaFlow AI (Android)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Presentation Layer                                │
│                                                                             │
│  Jetpack Compose UI                                                         │
│  ├── Home (Gallery Grid: 2–4 cols, Sticky Date Headers, Selection Mode)    │
│  ├── Collections (Smart Albums: People, Travel, Nature, Documents, etc.)    │
│  ├── Search (Instant Natural Language Semantic Search)                      │
│  ├── Media Viewer (Fullscreen Immersive, Pinch-to-Zoom, EXIF & AI Sheet)    │
│  ├── Activity (Background AI Task Stream & History)                         │
│  ├── Settings (Themes, On-Device AI Controls, Duplicate Storage Analyzer)   │
│  ├── Bottom Navigation (Floating Glassmorphic Pill with Spring Indicators)  │
│  └── Material 3 Components (GlassmorphicSurface, RealtimeBanner)            │
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
│ Room Database         │  │ Images               │  │ WorkManager Sync       │
│ Media Metadata        │  │ Videos               │  │ MediaObserverService   │
│ AI Tags & Hashes      │  │ Albums               │  │ ContentObserver        │
│ Search Index          │  │ Thumbnails           │  │ NotificationManager    │
└───────────────────────┘  └──────────────────────┘  └────────────────────────┘
                     │                     │                     │
                     └──────────────┬──────┴──────────────┬──────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AI Processing Layer                            │
│                                                                             │
│  • Image Classifier (Google ML Kit ImageLabeling)                           │
│  • OCR Engine (Google ML Kit TextRecognition)                               │
│  • Face Detection & Clustering (Google ML Kit FaceDetection)                │
│  • Perceptual dHash Duplicate Detector (64-bit difference hash)             │
│  • Dominant Color & Palette Extraction                                      │
│  • Automatic Smart Album Routing                                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📱 Core Features & User Journey

### 1. Home Screen (Gallery)
- **Responsive 2–4 Column Grid**: Toggle column density dynamically with instantaneous layout relayout.
- **Sticky Date Headers**: Elegant groupings like *"Today"*, *"Yesterday"*, and formatted calendar dates.
- **Real-Time Floating Capsule Banner**: An animated floating banner appears at the top whenever new items are ingested (*"5 new photos organized"* with slide, scale, fade, and glowing status dot).
- **Multi-Selection Mode**: Long press any image to enter selection mode with soft border glow, checkmarks, item counter, batch share, and delete.

### 2. Collections (Smart Albums)
- Real-time automatic categorizations:
  - 👥 **People & Pets** (Faces, portraits, puppies, kittens)
  - ✈️ **Travel & Places** (Beaches, resorts, cityscapes, monuments)
  - 🏔️ **Nature & Mountains** (Peaks, lakes, foliage, flora)
  - 🏎️ **Cars & Vehicles** (Supercars, automobiles, bikes)
  - 🍔 **Food & Dining** (Dishes, cuisines, desserts)
  - 📄 **Documents & Receipts** (OCR-indexed text files, passports, bills)
  - 📱 **Screenshots** (App captures, code snippets)
  - 🎬 **Videos & Clips** (4K and HD recordings with playback badges)
- Each album card features cover art, total item count, and animated touch scale.

### 3. AI Semantic Search
- Natural language query parser:
  - *"Dog"*, *"Sunset"*, *"Red Car"*, *"Beach"*, *"Birthday"*, *"Passport"*, *"Mountain"*, *"Laptop"*, *"Blue Shirt"*, *"Night Sky"*
- Live debounced search against image labels, scene categories, filenames, and OCR extracted texts.

### 4. Fullscreen Immersive Media Viewer
- Full edge-to-edge black canvas with controls that fade out on single tap.
- **Pinch to zoom** (up to 4x) and **Double tap zoom**.
- **Swipe down to dismiss** gesture.
- **HorizontalPager** for buttery smooth swiping across your entire library.
- Bottom action bar: **Share**, **Edit**, **Favorite**, **Delete**, and **Info**.
- **Metadata Bottom Sheet**: Displays camera EXIF (aperture, ISO, lens, resolution, file size), date, detected AI visual labels, and extracted OCR text.

### 5. Activity Screen
- Real-time background feed monitoring autonomous AI actions:
  - ✔ *12 photos sorted* (Categorized into Nature & Sunset)
  - ✔ *Duplicate removed* (Saved 14.2 MB of storage)
  - ✔ *Screenshot detected* (Auto-routed to Screenshots album)
  - ✔ *OCR completed* (Indexed Passport and Receipt text)
  - ✔ *Face grouping finished* (Clustered distinct individuals)

### 6. Settings & Storage Management
- Theme selection (Light, Dark, System Auto).
- Individual AI toggles (Real-time Sorting, Duplicate Detection, OCR, Face Clustering).
- Storage cleanup tool with instant duplicate analysis and space recovery.
- 100% On-Device Neural Processing privacy guarantee.

---

## 🐍 Python Core Engine & Real-Time Observer

MediaFlow AI includes a dual-engine architecture where **Python** powers high-speed image processing, neural hashing, real-time filesystem observation, and API serving:

1. **Embedded On-Device Python (`app/src/main/python/mediaflow_engine.py`)**:
   - Packaged with the Android APK via Chaquopy.
   - Computes 64-bit difference hash (`dHash`), dominant tone extraction, natural language query ranking, and duplicate graph clustering directly in Python on Android.
   - [PythonBridge.kt](file:///e:/PROJECTS/images-sort/app/src/main/java/com/mediaflow/ai/ai/PythonBridge.kt) connects Kotlin directly with `mediaflow_engine.py`.

2. **Standalone Python AI Media Sorter & Real-Time Watcher (`python_engine/`)**:
   - `python python_engine/run.py scan <folder>`: Indexes, categorizes, extracts EXIF, and detects duplicates for any media directory into SQLite.
   - `python python_engine/run.py watch <folder>`: Live filesystem observer that auto-organizes incoming photos and videos in real time.
   - `python python_engine/run.py serve`: High-performance FastAPI & WebSocket server for querying sorted media, semantic search, and streaming live `"5 new photos organized"` notifications.

---

## 🛠️ Tech Stack & Dependencies

| Component | Library / Framework | Version |
|---|---|---|
| **Language** | Kotlin | `2.0.21` |
| **UI Toolkit** | Jetpack Compose (BOM) | `2024.11.00` |
| **Design System** | Material Design 3 | Latest |
| **Dependency Injection** | Dagger Hilt | `2.52` |
| **Navigation** | Navigation Compose | `2.8.4` |
| **Database** | Room + KSP | `2.6.1` |
| **Image Loading** | Coil Compose + Video Decoder | `2.7.0` |
| **Background Tasks** | WorkManager (Kotlin KTX) | `2.10.0` |
| **Video Playback** | AndroidX Media3 / ExoPlayer | `1.4.1` |
| **On-Device Vision** | Google ML Kit Image Labeling | `17.0.9` |
| **On-Device OCR** | Google ML Kit Text Recognition | `16.0.1` |
| **On-Device Faces** | Google ML Kit Face Detection | `16.1.7` |

---

## 🚀 Building & Running

1. Open the project in **Android Studio Ladybug (2024.2+)** or newer.
2. Ensure you have **JDK 17** configured.
3. Sync Gradle:
   ```bash
   ./gradlew build
   ```
4. Run on an Android device or emulator running **Android 8.0 (API 26) through Android 15 (API 35)**.
5. On launch, MediaFlow AI requests standard media permissions (`READ_MEDIA_IMAGES` and `READ_MEDIA_VIDEO` on Android 13+). If running on an emulator without photos, the app automatically pre-populates a rich demo gallery so you can test all features instantly!
