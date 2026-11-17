# TolleApp — Agent Guidelines & Repository Architecture

Welcome to the **TolleApp** codebase. This document serves as the primary technical reference and architectural guide for AI coding agents to work autonomously, accurately, and efficiently in this repository.

---

## 1. Project Overview & Tech Stack

**TolleApp** is a native Android companion and scorekeeping application for popular tabletop, card, and board games (Skyjo, Wizard, Schwimmen, Dart, Rommé, Flip7, Randomizer). It includes persistent player management, game presets, pause/resume game support, and aggregated game statistics.

- **Language & JVM**: Kotlin 2.4.x (`2.4.21`, JVM target 11)
- **Android Config**: `minSdk = 31`, `compileSdk = 37`, `targetSdk = 37`, `namespace = "de.michael.tolleapp"`, `buildConfig = true`, locked to portrait orientation in `AndroidManifest.xml`
- **UI Framework**: Jetpack Compose with Material 3 (Compose BOM `2026.09.00`) + `material-icons-extended`
- **Dependency Injection**: Koin 4.2.x (`koin-core`, `koin-compose`, `koin-compose-viewmodel`)
- **Database & Persistence**: Room 2.8.x with `sqlite-bundled` (all persistence including Settings uses Room; `datastore-preferences` is present in `libs.versions.toml` / `build.gradle.kts` but currently unused)
- **Navigation**: JetBrains Navigation Compose 2.10.x (`org.jetbrains.androidx.navigation:navigation-compose`) with type-safe `@Serializable` routes
- **Serialization & Time**: `kotlinx-serialization-json`, `kotlinx-datetime`
- **Build System**: Gradle 9.9.1 with Android Gradle Plugin (AGP) 9.3.3, KSP 2.3.x

---

## 2. Build & Verification Commands

Always verify changes using the Gradle wrapper:

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

> **Important**:
> - Run `./gradlew compileDebugKotlin` after modifying Room entities or DAOs to verify KSP code generation and Room schema export.
> - Only placeholder template tests currently exist (`ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`), so add new unit tests under `app/src/test/java/de/michael/tolleapp/` when introducing non-trivial domain or ViewModel logic.

---

## 3. Repository Layout & Package Structure

Root package: `app/src/main/java/de/michael/tolleapp`

```
de.michael.tolleapp/
├── App.kt                      # Application class initializing Koin
├── MainActivity.kt             # Activity hosting AppTheme & Compose NavGraph
├── NavGraph.kt                 # Navigation graph, screen transitions & sharedViewModel helper
├── Route.kt                    # Type-safe @Serializable navigation routes
│
├── data/                       # Room database configuration
│   └── AppDatabase.kt          # RoomDatabase definition (entities, DAOs, version 28)
├── di/
│   └── Module.kt               # Koin DI module (AppDatabase, DAOs, Repos, ViewModels)
├── ui/theme/                   # Material 3 Color, Theme (AppTheme) & Typography
│
├── main/                       # Home / Main menu
│   ├── MainScreen.kt           # Game selection grid and settings/easter-egg top bar
│   ├── MainState.kt            # Main menu state
│   ├── MainViewModel.kt        # Main menu ViewModel
│   ├── components/GameCard.kt  # Grid card component for MainScreen
│   └── util/GameNavigationData.kt # List of tiles displayed on MainScreen
│
├── statistics/                 # Aggregated game & player statistics
│   ├── StatScreen.kt           # Statistics UI with drawer game selector
│   ├── StatState.kt            # Aggregated stats UI state
│   ├── StatViewModel.kt        # Stats loading & reset logic
│   ├── gameStats/              # Game-specific stats models (Skyjo, Schwimmen, Dart, Flip7)
│   └── screens/                # Game-specific stats tables & DartCompareSection
│
├── settings/                   # App settings & player cleanup
│   ├── data/                   # Settings entity, DAO & SettingsRepository (dark mode)
│   └── presentation/           # SettingsScreen, PlayerDeleteScreen, TestScreen
│
└── games/                      # Feature modules for each game
    ├── skyjo/                  # Skyjo card game (clean 3-layer MVI)
    ├── wizard/                 # Wizard card game (MVI; note WizardRepository is in data/ and Impl in domain/)
    ├── romme/                  # Rommé card game (clean 3-layer MVI)
    ├── flip7/                  # Flip 7 card game (clean 3-layer MVI + stats)
    ├── schwimmen/              # Legacy structure: data/{game,stats}/ + custom Start/Game screens
    ├── dart/                   # Legacy structure: data/ + custom Start/Game screens + DartKeyboard
    ├── randomizer/             # Utility module: presentation/ only (no DB persistence)
    │
    └── util/                   # Reusable game building blocks
        ├── startScreen/        # Shared StartScreen, StartState, StartAction
        ├── endScreen/          # Shared EndScreen & EndScreenPreview
        ├── keyboards/          # Custom in-app keyboards & KeyboardSwitcher
        ├── table/              # Reusable Table, TableStrokeOptions, SortDirectionButton, StringMappers
        ├── player/             # Global Player entity, DAO & PlayerRepository
        ├── presets/            # Player group GamePreset entities, DAO & GamePresetRepository
        ├── BetterOutlinedTextField.kt
        ├── CustomTopBar.kt     # Standard app top bar
        ├── DevidedScreen.kt    # NOTE filename typo ("Devided"): defines DividedScreen composable
        ├── GameType.kt         # Enum of all games
        ├── OnHomeDialog.kt     # Save/discard confirmation dialog when exiting active games
        ├── PausedGame.kt       # Base open class for resumable game entities
        └── PlayerDisplayRow.kt # Reusable player row (name, dealer highlight, round & total score)
```

---

## 4. Game Architecture & Pattern (MVI / Clean Architecture)

Newer games (`skyjo`, `romme`, `flip7`) follow a three-layer modular structure (`wizard` follows the same MVI pattern, though its `WizardRepository` interface sits in `data/` and `WizardRepositoryImpl` in `domain/`; `schwimmen`, `dart`, and `randomizer` use older bespoke layouts):

### 1. `domain/` (Pure Kotlin)
- Domain entities: e.g., `Flip7Game.kt`, `Flip7RoundData.kt`.
- Repository interface: e.g., `interface Flip7Repository`.

### 2. `data/` (Room & Persistence)
- Room entities in `entities/`: `<Game>GameEntity.kt` (extends `PausedGame(id, createdAt)`), `<Game>PlayerEntity.kt` (foreign keys to game & `Player` with `CASCADE` + `indices`), `<Game>RoundEntity.kt`.
- DAO: `<Game>Dao.kt` (plus optional `<Game>StatisticsDao` in the same file).
- Repository implementation: `<Game>RepositoryImpl.kt`.
- Mappers in `mappers/`: `GameMappers.kt`, `RoundMappers.kt` (maps between Room entities and domain models, encoding/decoding `Map` fields via `kotlinx.serialization.json.Json`).

### 3. `presentation/` (MVI Jetpack Compose)
- **`<Game>Action.kt`**: Sealed interface representing all user intents (e.g. `NavigateToMainMenu`, `DeleteGame`, `EndGame`, `PlayAgain`, `EndRound`, `InputScore`, `UndoLastRound`, `OnSortDirectionChange`, `SetLastKeyboardPage`).
- **`<Game>State.kt`**: Immutable data class holding the full UI state.
  - Must provide extension: `fun <Game>State.toStartState(): StartState` to bridge state to the shared `StartScreen` (`StartState` is an `open class`; `RommeState` subclasses it directly, while newer games use `toStartState()`).
- **`<Game>ViewModel.kt`**:
  - Injects Repositories via Koin (`gameRepo`, `playerRepo`, `presetRepo`).
  - Combines `allPlayers`, `presets`, `pausedGames`, `_selectedPlayerIds`, and internal `_state` via `combine(...)`:
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
        initialValue = Flip7State()
    )
    ```
  - Handles start screen interactions via `fun onStartAction(action: StartAction)`:
    - `StartAction.CreatePlayer(name, index)`: creates player in `PlayerRepository` and selects at `index`.
    - `StartAction.SelectPlayer(index, playerId)`: sets player at seat `index` and auto-appends a trailing `null` seat up to `maxPlayers`.
    - `StartAction.UnselectPlayer(index)`: removes player seat at `index` (maintaining a trailing `null` seat up to `maxPlayers`).
    - `StartAction.ResetSelectedPlayers`: resets seat selection to initial `null` slots.
    - `StartAction.CreatePreset(presetName, playerIds)`: **must call `presetRepo.createPreset(GameType.<GAME>, ...)`** (passing the `GameType` enum — the `String` overload in `GamePresetRepository` has a commented-out no-op body).
    - `StartAction.SelectPreset(presetId)`: loads preset players (enforce `maxPlayers` guard if applicable, as in `WizardViewModel`).
    - `StartAction.DeletePreset(presetId)`: deletes preset in `GamePresetRepository`.
    - `StartAction.StartGame`: creates a new game session and persists players/initial round.
    - `StartAction.ResumeGame(gameId)`: loads an in-progress game from `pausedGames`.
    - `StartAction.DeleteGame(gameId)`: deletes a paused game.
    - *(Note: `StartAction.NavigateToMainMenu` and `StartAction.NavigateToGame` are intercepted in `NavGraph.kt` before calling `viewModel.onStartAction(action)`.)*
  - For async actions like `UndoLastRound` invoked from `EndScreen`, `SkyjoViewModel` and `Flip7ViewModel` accept `fun onAction(action: <Game>Action, onFinished: (() -> Unit)? = null)` so `NavGraph` pops back only after DB undo completes.
- **`<Game>GameScreen.kt`**: Main gameplay Compose screen using `Scaffold` + `CustomTopBar`, `DividedScreen` (in `DevidedScreen.kt`), `PlayerDisplayRow`, `Table`, `OnHomeDialog`, `BackHandler`, and `AnimatedVisibility` + `KeyboardSwitcher`.
- **End-of-game screen**: Active games do not use custom `<Game>EndScreen.kt` files (`SkyjoEndScreen.kt` is commented-out legacy code); instead, `NavGraph.kt` renders the shared `de.michael.tolleapp.games.util.endScreen.EndScreen`.

---

## 5. Shared Game Building Blocks (`games/util`)

Before creating custom UI components, always check and reuse existing utilities in `games/util`:

1. **`StartScreen` (`games/util/startScreen/StartScreen.kt`)**:
   Standard screen for searchable player selection, auto-expanding seats, presets dropdown, creating new players, optional `settingsItem` slot, and resuming/deleting paused games. Enforces `minPlayers` and `maxPlayers` constraints (and disables presets larger than `maxPlayers`).

2. **`EndScreen` (`games/util/endScreen/EndScreen.kt`)**:
   Standard game over screen displaying an optional winner trophy banner (`showWinner = true`, supporting ties via `ranks`), a `Table` of ranked players, round-by-round scores, and optional `sortedTotalValues` (`∑` row), plus top-bar Home & Undo buttons and bottom `"Zum Hauptmenü"` / `"Nochmal spielen"` buttons.

3. **`DividedScreen` (`games/util/DevidedScreen.kt`)**:
   *(Note the filename spelling `DevidedScreen.kt` vs composable name `DividedScreen`.)* User-draggable vertical split screen with a drag-handle `HorizontalDivider`, splitting `topPart` (e.g., player list + round controls) and `bottomPart` (e.g., round history `Table`).

4. **`Table` (`games/util/table/Table.kt`)**:
   Horizontally and vertically scrollable `SubcomposeLayout` table for round-based games. Supports `weights`, `frozenStartColumns`, `pinTotalRow`, `TableStrokes` / `TableStrokeOptions`, string helpers (`toTableHeader`, `toTableRowCell`, `toTableTotalCell` in `StringMappers.kt`), and `SortDirectionButton` / `getSortDirectionButtonComposableList` (`SortDirectionButton.kt`).

5. **Keyboards (`games/util/keyboards/`)**:
   - `NumberKeyboard`: Numeric keypad with optional minus (`minusAllowed`) or double-submit (`withDoubleSubmit`).
   - `CustomSingleCardKeyboard`: Configurable multi-card selector (`lowestNumber..highestNumber`, `higherNumbers`, `numberOfRows`, `maxNumbers`, `ExtraButtonType`).
   - `SkyjoNeleKeyboard`: Specialized Skyjo card-value selector (`-2..12`, up to 12 cards).
   - `RommeKeyboard`: Quick card point values (`2..10`) with `"Submit"` and `"Submit x2"`.
   - `KeyboardSwitcher`: `HorizontalPager` switching between a `List<Keyboard>` (`Keyboard` enum in `util/Keyboard.kt`: `NUMBER`, `NUMBER_WITH_MINUS`, `NUMBER_WITH_2X`, `FLIP7`, `SKYJO`, `DART`, `ROMME`).

6. **`PlayerDisplayRow` (`games/util/PlayerDisplayRow.kt`)**:
   Standard row showing player name (highlighted in `primary` when `isDealer`), current round score, and total score using `BetterOutlinedTextField`.

7. **`CustomTopBar` (`games/util/CustomTopBar.kt`)**:
   Standardized `TopAppBar` with rounded bottom corners, title, `navigationIcon`, and `actions`.

8. **`OnHomeDialog` (`games/util/OnHomeDialog.kt`)**:
   Alert dialog prompting the user to save (pause) or discard the active game when navigating back to the main menu.

---

## 6. Database (Room) Guidelines & Migrations

- Database definition is in `app/src/main/java/de/michael/tolleapp/data/AppDatabase.kt` (currently `version = 28`, `exportSchema = true`).
- Active Room schemas are exported to `app/schemas/de.michael.tolleapp.data.AppDatabase/`. *(Other `Skyjo*Database` folders under `app/schemas/` are obsolete legacy artifacts.)*
- `AppDatabase` is instantiated in `di/Module.kt` with `fallbackToDestructiveMigration(false)`.
- **Data Modeling Conventions**:
  - `*GameEntity` extends `PausedGame(id, createdAt)` for paused game listing.
  - `*PlayerEntity` and `*RoundEntity` declare `ForeignKey` constraints (`onDelete = ForeignKey.CASCADE` for `gameId`/`playerId`, `SET_NULL` for `dealerId`) and `indices` on foreign key columns.
  - Complex round maps (e.g. `scores: Map<String, Int>`) are stored as JSON `String` columns and converted in `mappers/` via `kotlinx.serialization.json.Json` rather than Room `@TypeConverter`s.
- **Rules when altering database schema**:
  1. Add any new entity class to the `entities` array in `@Database(...)` in `AppDatabase.kt`.
  2. Add the abstract DAO getter to `AppDatabase`.
  3. Bump `version = <N + 1>` when adding or modifying columns/tables.
  4. Run `./gradlew compileDebugKotlin` to export the new schema JSON under `app/schemas/de.michael.tolleapp.data.AppDatabase/`.
  5. If queries return extra columns from joins, use `@ColumnInfo` or annotate DAO methods with `@RewriteQueriesToDropUnusedColumns`.

---

## 7. Dependency Injection (Koin) Standards

Central registry is `appModule` in `app/src/main/java/de/michael/tolleapp/di/Module.kt`:

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

In Compose navigation screens:
- **Multi-screen game flows (`Start` -> `Game` -> `End`)**: Retrieve the ViewModel via `it.sharedViewModel<MyGameViewModel>(navController)` inside `NavGraph.kt` so the ViewModel is scoped to the parent `navigation<Route.MyGameNav>` back-stack entry and preserves state across screens.
- **Single/top-level screens**: Use `koinViewModel<...>()` (or pass from `NavGraph`).

---

## 8. Navigation & Statistics Guidelines

Navigation is defined in `Route.kt` and `NavGraph.kt` using JetBrains Navigation Compose with KotlinX Serialization:

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
   Add nested `navigation<Route.MyGameNav>(startDestination = Route.MyGame.Start) { ... }` using `composableStartScreen<...>` and `composableGameScreen<...>` for standard transitions, `it.sharedViewModel<MyGameViewModel>(navController)` for shared state, and `navController.popToRoute(Route.Before.StartScreen)` to return home.
3. **Register on Home Screen**:
   - Add enum value to `de.michael.tolleapp.games.util.GameType`.
   - Add `GameNavigationData` entry to `gameNavigationDataList` in `de.michael.tolleapp.main.util.GameNavigationData.kt`.
4. **Integrate Statistics (if applicable)**:
   - Add `<Game>StatisticsDao` (in `<Game>Dao.kt`, `AppDatabase.kt`, `di/Module.kt`).
   - Add stats model in `statistics/gameStats/` and table composable in `statistics/screens/`.
   - Wire into `StatState.kt`, `StatViewModel.kt`, and `StatScreen.kt` (`GameSelector`, `gameLabel`, and `when (state.selectedGame)`).

---

## 9. Jetpack Compose & Code Style Conventions

- **Material 3**: Use Material 3 APIs.
  - **Notice**: Never use deprecated `Divider(...)`. Always use `HorizontalDivider(...)` or `VerticalDivider(...)`. (A few legacy occurrences remain in `StatScreen.kt` and `DartStatsTable.kt`; do not replicate them.)
- **UI Localization**: User-facing strings are in German (either hardcoded in German for game screens, e.g., `"Spieler auswählen"`, `"Runde beenden"`, `"Punkte"`, `"Gesamt"`, or via `res/values-de/strings.xml` paired with `res/values/strings.xml` for shared strings like `OnHomeDialog`).
- **State Immutability**: All states must be immutable data classes with default argument values.
- **Coroutines & Flow**: Use `viewModelScope` for repository writes and state updates. Collect states in Compose using `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose` (used in `NavGraph.kt`; some older top-level screens still use `collectAsState()`).
