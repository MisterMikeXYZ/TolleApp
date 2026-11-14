# TolleApp — Agent Guidelines & Repository Architecture

Welcome to the **TolleApp** codebase. This document serves as the primary technical reference and architectural guide for AI coding agents to work autonomously, accurately, and efficiently in this repository.

---

## 1. Project Overview & Tech Stack

**TolleApp** is a native Android companion and scorekeeping application for popular tabletop, card, and board games (e.g., Skyjo, Wizard, Schwimmen, Dart, Rommé, Flip7, Randomizer). It includes persistent player management, game presets, pause/resume game support, and aggregated game statistics.

- **Language**: Kotlin 2.4.x (JVM target 11)
- **UI Framework**: Jetpack Compose with Material 3 (Compose BOM 2026.09.00)
- **Dependency Injection**: Koin 4.2.x (`koin-android`, `koin-compose`, `koin-compose-viewmodel`)
- **Database & Persistence**: Room 2.8.x with SQLite Bundled, DataStore Preferences
- **Navigation**: JetBrains / AndroidX Navigation Compose 2.10.x with type-safe `@Serializable` routes
- **Serialization**: KotlinX Serialization JSON
- **Build System**: Gradle 9.9.1 with Android Gradle Plugin (AGP) 9.3.3, KSP 2.3.x

---

## 2. Build & Verification Commands

Always verify changes using the Gradle wrapper.

```bash
# Ensure gradlew has executable permissions (if ever reset)
chmod +x ./gradlew

# Compile Kotlin debug sources (fastest validation of types, KSP, and Room schemas)
./gradlew compileDebugKotlin

# Run unit tests
./gradlew testDebugUnitTest

# Run Android lint
./gradlew lintDebug

# Assemble full debug APK
./gradlew assembleDebug
```

> **Important**: When running Gradle commands in persistent shells or subshells, execute `./gradlew compileDebugKotlin` to verify that Room entity changes and KSP code generators compile without errors.

---

## 3. Repository Layout & Package Structure

Root package: `app/src/main/java/de/michael/tolleapp`

```
de.michael.tolleapp/
├── App.kt                      # Application class initializing Koin
├── MainActivity.kt             # Activity hosting the Compose NavGraph
├── NavGraph.kt                 # Jetpack Compose navigation graph & screen transitions
├── Route.kt                    # Type-safe @Serializable navigation routes
│
├── data/                       # Room database configuration
│   └── AppDatabase.kt          # RoomDatabase definition (entities, DAOs, version)
├── di/
│   └── Module.kt               # Koin dependency injection module (DAOs, Repos, ViewModels)
│
├── main/                       # Home / Main menu
│   ├── MainScreen.kt           # Game selection grid and home buttons
│   ├── MainViewModel.kt        # Main menu state
│   └── util/GameNavigationData # List of games displayed on MainScreen
│
├── statistics/                 # Aggregated game & player statistics
│   ├── StatScreen.kt           # Statistics UI
│   ├── StatViewModel.kt        # Stats business logic
│   └── gameStats/              # Game-specific stats calculations & tables
│
├── settings/                   # App settings & player cleanup
│   ├── data/                   # Settings entity & DAO
│   └── presentation/           # Settings screens
│
└── games/                      # Feature modules for each game
    ├── skyjo/                  # Skyjo card game
    ├── wizard/                 # Wizard card game
    ├── schwimmen/              # Schwimmen (31) card game
    ├── dart/                   # Dart scorekeeper (X01 games, throws)
    ├── romme/                  # Rommé card game
    ├── flip7/                  # Flip 7 card game
    ├── randomizer/             # Randomizer utility
    │
    └── util/                   # Reusable game building blocks
        ├── startScreen/        # Standardized player selection & game start screen
        ├── endScreen/          # Standardized winner/podium & end-of-game screen
        ├── keyboards/          # Custom in-app keyboards for fast score entry
        ├── table/              # Reusable score & round tables
        ├── player/             # Global Player model, DAO & Repository
        ├── presets/            # Player group presets, DAO & Repository
        ├── BetterOutlinedTextField.kt
        ├── CustomTopBar.kt     # Standard app top bar
        ├── DevidedScreen.kt    # Responsive split screen (content + controls/keyboard)
        ├── GameType.kt         # Enum of all games
        ├── OnHomeDialog.kt     # Confirmation dialog when exiting active games
        └── PausedGame.kt       # Common model for saved/resumable games
```

---

## 4. Game Architecture & Pattern (MVI / Clean Architecture)

Every game follows a consistent three-layer modular structure:

### 1. `domain/` (Pure Kotlin)
- Domain entities: e.g., `SkyjoGame.kt`, `SkyjoRoundData.kt`.
- Repository interface: e.g., `interface SkyjoRepository`.

### 2. `data/` (Room & Persistence)
- Room entities: `entities/SkyjoGameEntity.kt`, `entities/SkyjoRoundEntity.kt`.
- DAO: `SkyjoDao.kt`.
- Repository implementation: `SkyjoRepositoryImpl.kt`.
- Mappers: `mappers/GameMappers.kt`, `mappers/RoundMappers.kt` (maps between Room entities and domain models).

### 3. `presentation/` (MVI Jetpack Compose)
- **`<Game>Action.kt`**: Sealed interface representing all user intents (e.g. `NavigateToMainMenu`, `EndRound`, `InputScore`, `UndoLastRound`).
- **`<Game>State.kt`**: Immutable data class holding the full UI state.
  - Must provide extension: `fun <Game>State.toStartState(): StartState` to bridge state to the shared `StartScreen`.
- **`<Game>ViewModel.kt`**:
  - Injects Repositories via Koin.
  - Combines `allPlayers`, `presets`, `pausedGames`, and internal `_state` via `combine(...)`:
    ```kotlin
    val state = combine(
        _allPlayers,
        _presets,
        _selectedPlayerIds,
        _pausedGames,
        _state
    ) { players, presets, selectedPlayerIds, pausedGames, state ->
        state.copy(
            allPlayers = players,
            presets = presets,
            pausedGames = pausedGames,
            selectedPlayers = selectedPlayerIds.map { id -> players.find { it.id == id } },
            selectedPlayerIds = selectedPlayerIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SkyjoState()
    )
    ```
  - Handles start screen interactions via `fun onStartAction(action: StartAction)`:
    - `StartAction.CreatePlayer`: creates player in `PlayerRepository` and auto-selects.
    - `StartAction.SelectPlayer`: updates player selection for a given seat index.
    - `StartAction.RemovePlayerRow`: removes player seat.
    - `StartAction.AddPlayerRow`: adds an empty player seat.
    - `StartAction.LoadPreset`: loads preset player list.
    - `StartAction.ResumeGame`: resumes an in-progress game from `pausedGames`.
    - `StartAction.StartNewGame`: starts a new game session.
- **`<Game>GameScreen.kt`**: Main gameplay Compose screen using `DevidedScreen` or `CustomTopBar`, displaying players, rounds table, and custom keyboard.
- **`<Game>EndScreen.kt` / `games.util.endScreen.EndScreen`**: Shows rankings, podium, play again, and undo options.

---

## 5. Shared Game Building Blocks (`games/util`)

Before creating custom UI components, always check and reuse existing utilities in `games/util`:

1. **`StartScreen` (`games/util/startScreen/StartScreen.kt`)**:
   Standard screen for player selection, adding seats, loading presets, creating new players, and resuming paused games. Supports `minPlayers` and `maxPlayers` constraints.

2. **`EndScreen` (`games/util/endScreen/EndScreen.kt`)**:
   Standard game over screen with podium for top 3, full player ranking list, undo button, replay button, and home button.

3. **`DevidedScreen` (`games/util/DevidedScreen.kt`)**:
   Splits the screen vertically into a top content area (e.g. scoreboard table) and a bottom action area (e.g. keyboard), with responsive weight/scrolling.

4. **`Table` (`games/util/table/Table.kt`)**:
   Scrollable tabular layout for round-based games. Supports headers, frozen columns, row strokes, and `SortDirectionButton`.

5. **Keyboards (`games/util/keyboards/`)**:
   - `NumberKeyboard`: Numeric keypad with +/- toggle.
   - `SkyjoNeleKeyboard`: Specialized Skyjo score keypad (-2 to 12).
   - `RommeKeyboard`: Quick card point values for Rommé.
   - `KeyboardSwitcher`: Paged keyboard switching mechanism.

6. **`CustomTopBar` (`games/util/CustomTopBar.kt`)**:
   Standardized `TopAppBar` with title, back button handling, and action icons.

7. **`OnHomeDialog` (`games/util/OnHomeDialog.kt`)**:
   Dialog prompting the user whether to save (pause) or discard the game when navigating back to the main menu.

---

## 6. Database (Room) Guidelines & Migrations

- Database definition is in `app/src/main/java/de/michael/tolleapp/data/AppDatabase.kt`.
- Room schemas are exported to `app/schemas/de.michael.tolleapp.data.AppDatabase/`.
- `AppDatabase` is instantiated in `di/Module.kt` with:
  ```kotlin
  fallbackToDestructiveMigration(false)
  ```
- **Rules when altering database schema**:
  1. Any new table/entity must be added to the `entities` array in `@Database(...)`.
  2. The abstract DAO getter must be added to `AppDatabase`.
  3. Bump `version = <N + 1>` when adding or modifying columns/tables.
  4. Run `./gradlew compileDebugKotlin` to regenerate the schema JSON file under `app/schemas/`.
  5. If queries return extra columns from joins, use `@ColumnInfo` or annotate DAO methods with `@RewriteQueriesToDropUnusedColumns`.

---

## 7. Dependency Injection (Koin) Standards

Central registry is in `app/src/main/java/de/michael/tolleapp/di/Module.kt`:

1. **DAOs**:
   ```kotlin
   single { get<AppDatabase>().myGameDao() }
   ```
2. **Repositories**:
   ```kotlin
   single<MyGameRepository> { MyGameRepositoryImpl(get(), get()) }
   ```
3. **ViewModels**:
   ```kotlin
   viewModel { MyGameViewModel(get(), get(), get()) }
   ```

In Compose screens:
- Retrieve ViewModels using `koinViewModel<MyGameViewModel>()`.

---

## 8. Navigation Guidelines

Navigation is defined in `Route.kt` and `NavGraph.kt` using Jetpack Navigation Compose with KotlinX Serialization:

1. **Define Routes in `Route.kt`**:
   ```kotlin
   @Serializable
   data object MyGameNav : Route

   @Serializable
   sealed interface MyGame : Route {
       @Serializable
       data object Start : MyGame
       @Serializable
       data object Game : MyGame
       @Serializable
       data object End : MyGame
   }
   ```
2. **Wire in `NavGraph.kt`**:
   Add nested `navigation<Route.MyGameNav>(startDestination = Route.MyGame.Start) { ... }`.
   Ensure the ViewModel is scoped appropriately so state persists between `Start` -> `Game` -> `End`.
3. **Register on Home Screen**:
   - Add enum value to `de.michael.tolleapp.games.util.GameType`.
   - Add entry to `gameNavigationDataList` in `de.michael.tolleapp.main.util.GameNavigationData.kt`.

---

## 9. Jetpack Compose & Code Style Conventions

- **Material 3**: Use Material 3 APIs.
  - **Notice**: Never use deprecated `Divider(...)`. Always use `HorizontalDivider(...)` or `VerticalDivider(...)`.
- **UI Localization**: App strings are predominantly in German. When writing user-facing strings, check `res/values-de/strings.xml` and keep UI text consistent in German (e.g., "Spieler auswählen", "Runde beenden", "Punktestand").
- **State Immutability**: All states must be immutable data classes with default argument values.
- **Coroutines & Flow**: Use `viewModelScope` for repository writes and state updates. Collect states in Compose using `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose`.
