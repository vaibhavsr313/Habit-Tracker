# 🟡 HabitTracker

A clean, minimal **habit tracking Android app** built with **Kotlin**, following **MVVM architecture** and using **Room Database** for fully offline, on-device storage. Track your daily habits, visualize your consistency with a **365-day heatmap**, and maintain streaks — all without needing an internet connection.

---

## 📱 Screenshots

> _Add your screenshots here after building the app_

---

## ✨ Features

- ✅ **Add habits** with a custom name and color
- 📅 **Weekly day strip** (Mon–Sun) with progress rings showing daily completion percentage
- 🔥 **Streak tracking** — automatically calculates your current consecutive day streak
- 📊 **365-day heatmap** per habit — visualize your entire year at a glance, scrollable horizontally like GitHub's contribution graph
- 🎯 **Mark habits done/undone** per day — tap any day in the week strip to log past or future entries
- 🗑️ **Delete habits** with confirmation dialog — removes all history cleanly
- 📆 **Dynamic date header** — shows "Today, 1st Jul" for today, full day name for other days
- 💾 **Fully offline** — all data stored locally on device using Room (SQLite)
- ⚡ **Reactive UI** — powered by Kotlin Flow, UI updates instantly with zero manual refresh code

---

## 🏗️ Architecture

This project follows **MVVM (Model-View-ViewModel)** architecture as recommended by Google's official Android guidelines.

```
📁 com.example.habittracker
│
├── 📁 data
│   ├── 📁 local
│   │   ├── Habit.kt               → Room Entity (habits table)
│   │   ├── HabitCompletion.kt     → Room Entity (completions table)
│   │   ├── HabitDao.kt            → DAO (all database queries)
│   │   └── AppDatabase.kt         → Room Database singleton
│   │
│   └── 📁 repository
│       └── HabitRepository.kt     → Single source of truth for all data
│
├── 📁 ui
│   ├── 📁 home
│   │   ├── HomeActivity.kt        → View (observes ViewModel, updates UI)
│   │   ├── HomeViewModel.kt       → ViewModel (holds state, processes logic)
│   │   ├── HabitAdapter.kt        → RecyclerView Adapter with DiffUtil
│   │   ├── HeatmapView.kt         → Custom View (365-day dot grid)
│   │   └── DayProgressView.kt     → Custom View (day circle with progress ring)
│   │
│   └── 📁 addhabit
│       ├── AddHabitActivity.kt    → View (add new habit screen)
│       └── AddHabitViewModel.kt   → ViewModel (handles save logic)
│
└── 📁 utils
    └── DateUtils.kt               → Date formatting, week/year helpers
```

### Data Flow

```
User taps toggle
      ↓
HomeActivity (View) — forwards action
      ↓
HomeViewModel — calls repository, no UI code
      ↓
HabitRepository — a single place for all data rules
      ↓
HabitDao — executes SQL via Room
      ↓
Room updates the SQLite file on the device
      ↓
Flow emits new data automatically
      ↓
ViewModel's StateFlow updates
      ↓
Activity collects → Adapter rebinds → UI updates
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM |
| Local Database | Room (SQLite) |
| Async / Threading | Kotlin Coroutines |
| Reactive Data | Kotlin Flow + StateFlow |
| UI Components | RecyclerView, CardView, Custom Views |
| Lifecycle | ViewModel, LiveData, repeatOnLifecycle |
| Build System | Gradle (KTS) |

---

## 🗄️ Database Schema

### `habits` table
| Column | Type | Description |
|---|---|---|
| id | Int (PK, autoGenerate) | Unique habit ID |
| name | String | Habit name e.g. "Go to Gym" |
| colorHex | String | Hex color e.g. "#FFD700" |
| iconName | String | Icon identifier |

### `habit_completions` table
| Column | Type | Description |
|---|---|---|
| habitId | Int (PK) | References habits.id |
| date | String (PK) | Date string "yyyy-MM-dd" |
| isDone | Boolean | Whether habit was completed |

> `habitId + date` form a **composite primary key** — guarantees one record per habit per day. Toggling done/undone uses `INSERT OR REPLACE` so no duplicates are ever created.

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- Kotlin 1.9+

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/vaibhavsr313/HabitTracker.git
cd HabitTracker
```

2. **Open in Android Studio**
   - File → Open → select the cloned folder

3. **Let Gradle sync** — dependencies will download automatically

4. **Run the app**
   - Connect a physical device or start an emulator
   - Press ▶ Run

---

## 📦 Dependencies

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// ViewModel + LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// RecyclerView + CardView
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
```

---

## 🧠 Key Concepts Used

**MVVM Architecture** — View only shows data and forwards user actions. ViewModel holds state and business logic. Model (Room + Repository) handles all data operations. Each layer is independent and testable.

**Room with Composite Primary Keys** — `HabitCompletion` uses `(habitId, date)` as a composite key ensuring one completion record per habit per day with automatic upsert behavior.

**Kotlin Flow + combine()** — `uiState` is built by combining two Flows (habits list + today's completions) using `combine()`. Whenever either source changes, the UI rebuilds automatically with zero manual refresh calls.

**Custom Views** — `HeatmapView` draws a 365-day colored dot grid on a Canvas. `DayProgressView` draws a circular progress ring using `drawArc()`. Both are fully reactive — set a property, they redraw themselves.

**Repository Pattern** — Single source of truth between ViewModel and data sources. Business rules like "delete completions before deleting a habit" live here, not scattered across ViewModels.

**RecyclerView with DiffUtil** — `ListAdapter` + `DiffCallback` ensures only changed habit cards are redrawn, not the entire list — smooth scrolling even with heatmaps inside each card.

---

## 🚧 Planned Features

- [ ] Habit categories / grouping
- [ ] Daily reminder notifications
- [ ] Statistics screen (completion rate graphs)
- [ ] Jetpack Compose UI rewrite
- [ ] Cloud backup support
- [ ] Widget for home screen

---

## 📄 License

```
MIT License

Copyright (c) 2025 Vaibhav

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and restriction on persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

> Built as a portfolio project to demonstrate Android development with MVVM, Room, Kotlin Coroutines, and custom View drawing. 🚀
