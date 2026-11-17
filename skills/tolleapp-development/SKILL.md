---
name: tolleapp-development
description: Guidelines, checklists, and templates for developing, debugging, and extending games in the TolleApp Android codebase (Jetpack Compose, Room, Koin, MVI architecture).
---

# TolleApp Development Skill

This skill provides step-by-step instructions, templates, and patterns for developing features, fixing bugs, and creating new tabletop/card games in TolleApp.

---

## 1. Quick Verification & Build Workflow

Always test your changes using the Gradle wrapper:

```bash
# Verify compilation and KSP/Room schema generation
./gradlew compileDebugKotlin

# Run unit tests
./gradlew testDebugUnitTest
```

If `./gradlew` fails with `Permission denied`, run `chmod +x ./gradlew`.

---

## 2. Checklist for Adding a New Game

When adding a new game (e.g. `cabo` or `doppelkopf`), complete the following steps in order:

### Step 1: Add GameType Enum
In `app/src/main/java/de/michael/tolleapp/games/util/GameType.kt`:
```kotlin
enum class GameType {
    // ... existing games
    MY_NEW_GAME
}
```

### Step 2: Create Domain Models (`games/<game>/domain/`)
1. `<Game>Game.kt`: Domain model representing game state, players, status.
2. `<Game>RoundData.kt`: Domain model representing a single round's scores/tricks.
3. `<Game>Repository.kt`: Repository interface exposing flows and suspend functions.

### Step 3: Create Persistence Layer (`games/<game>/data/`)
1. Room Entities in `entities/`:
   - `<Game>GameEntity.kt`: must extend `PausedGame(id, createdAt)` so paused game queries map cleanly (`id`, `createdAt`, `endedAt`, `isFinished`, `dealerId`, `winnerId`, etc.).
   - `<Game>PlayerEntity.kt`: junction entity (`primaryKeys = ["gameId", "playerId"]`) with `ForeignKey` `CASCADE` on both `<Game>GameEntity` and `Player`, plus `indices = [Index("gameId"), Index("playerId")]` and `index: Int` for seat order.
   - `<Game>RoundEntity.kt`: (`primaryKeys = ["gameId", "roundNumber"]`) with `ForeignKey` `CASCADE` on `<Game>GameEntity` (and `SET_NULL` on `Player` for `dealerId`), plus `indices`. Store round score maps as JSON `String`.
2. DAO in `<Game>Dao.kt`:
   - `@Dao` with CRUD methods, `getPausedGames(): Flow<List<<Game>GameEntity>>`, round upserts/deletes, and player/dealer/winner updates.
   - Optional `<Game>StatisticsDao` in the same file if the game tracks statistics.
3. Mappers in `mappers/`:
   - `GameMappers.kt` and `RoundMappers.kt` converting between Room entities and domain models. Serialize/deserialize `Map` fields using `kotlinx.serialization.json.Json` (`Json.encodeToString` / `Json.decodeFromString`).
4. Repository Implementation in `<Game>RepositoryImpl.kt`:
   - Implements the domain repository interface (wrap DB operations in `Result<T>` as in `Flip7RepositoryImpl` / `SkyjoRepositoryImpl`).

### Step 4: Register in Database (`data/AppDatabase.kt`)
1. Add all new entity classes (`MyGameEntity::class`, `MyGamePlayerEntity::class`, `MyGameRoundEntity::class`) to `@Database(entities = [...], version = <N+1>)`.
2. Add abstract DAO getter(s): `abstract fun myGameDao(): MyGameDao` (and optional `myGameStatisticsDao()`).
3. Run `./gradlew compileDebugKotlin` to export the new schema JSON to `app/schemas/de.michael.tolleapp.data.AppDatabase/<N+1>.json`.

### Step 5: Implement Presentation Layer (`games/<game>/presentation/`)
1. **`<Game>Action.kt`**: Sealed interface for user intents:
   ```kotlin
   sealed interface MyGameAction {
       data object NavigateToMainMenu : MyGameAction
       data class DeleteGame(val gameId: String) : MyGameAction
       data object EndGame : MyGameAction
       data class PlayAgain(val players: List<Player>) : MyGameAction

       data object AdvanceDealer : MyGameAction
       data object EndRound : MyGameAction
       data object UndoLastRound : MyGameAction
       data class InputScore(val playerId: String, val newScore: Int) : MyGameAction

       data class SetLastKeyboardPage(val page: Int) : MyGameAction
       data class OnSortDirectionChange(val newDirection: SortDirection) : MyGameAction
   }
   ```
2. **`<Game>State.kt`**: Immutable state data class with `toStartState()`:
   ```kotlin
   data class MyGameState(
       val allPlayers: List<Player> = emptyList(),
       val presets: List<GamePresetWithPlayers> = emptyList(),
       val pausedGames: List<PausedGame> = emptyList(),
       val selectedPlayers: List<Player?> = listOf(null, null),
       val selectedPlayerIds: List<String?> = listOf(null, null),
       val gameId: String = "",
       val currentDealerId: String? = null,
       val rounds: List<MyGameRoundData> = emptyList(),
       val totalPoints: Map<String, Int> = emptyMap(),
       val visibleRoundRows: Int = 5,
       val isFinished: Boolean = false,
       val lastKeyboardPage: Int = 0,
       val sortDirection: SortDirection = SortDirection.ASCENDING,
   )

   fun MyGameState.toStartState(): StartState {
       return StartState(
           gameType = GameType.MY_NEW_GAME,
           pausedGames = this.pausedGames,
           presets = this.presets,
           allPlayers = this.allPlayers,
           selectedPlayers = this.selectedPlayers,
       )
   }
   ```
3. **`<Game>ViewModel.kt`**:
   - Injects `gameRepo`, `playerRepo`, `presetRepo`.
   - Combines `_allPlayers`, `_presets`, `_selectedPlayerIds`, `_pausedGames`, `_state` into `val state: StateFlow<MyGameState>` using `WhileSubscribed(5_000)`.
   - Implements `fun onStartAction(action: StartAction)` handling `CreatePlayer`, `SelectPlayer`, `UnselectPlayer`, `ResetSelectedPlayers`, `CreatePreset`, `SelectPreset`, `DeletePreset`, `StartGame`, `ResumeGame`, `DeleteGame`.
     - **Important**: Always call `presetRepo.createPreset(GameType.MY_NEW_GAME, action.presetName, action.playerIds)` with the `GameType` enum — the `String` overload in `GamePresetRepository` is a commented-out no-op.
   - Implements `fun onAction(action: MyGameAction, onFinished: (() -> Unit)? = null)` (use `onFinished` for async actions like `UndoLastRound` invoked from `EndScreen`).
4. **`<Game>GameScreen.kt`**:
   - Uses `Scaffold` + `CustomTopBar` (home button opens `OnHomeDialog`, actions include Undo).
   - Handles `BackHandler` (close in-app keyboard first if expanded, otherwise open `OnHomeDialog`).
   - Uses `DividedScreen` (defined in `games/util/DevidedScreen.kt`) with `PlayerDisplayRow` + round controls in `topPart` and `Table` + `getSortDirectionButtonComposableList` in `bottomPart`.
   - Uses `AnimatedVisibility` + `KeyboardSwitcher` (or custom keyboard in `games/util/keyboards/`) for score entry.

### Step 6: Register in DI (`di/Module.kt`)
```kotlin
single { get<AppDatabase>().myGameDao() }
single<MyGameRepository> { MyGameRepositoryImpl(get(), get()) } // match constructor params
viewModel { MyGameViewModel(get(), get(), get()) }
```

### Step 7: Define Navigation (`Route.kt` & `NavGraph.kt`)
1. In `Route.kt`:
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
2. In `NavGraph.kt`:
   Use `composableStartScreen`, `composableGameScreen`, `it.sharedViewModel<MyGameViewModel>(navController)`, `collectAsStateWithLifecycle()`, `navController.popToRoute(Route.Before.StartScreen)`, and shared `EndScreen`:
   ```kotlin
   navigation<Route.MyGameNav>(startDestination = Route.MyGame.Start) {
       composableStartScreen<Route.MyGame.Start> {
           val viewModel = it.sharedViewModel<MyGameViewModel>(navController)
           val state by viewModel.state.collectAsStateWithLifecycle()
           StartScreen(
               minPlayers = 2,
               maxPlayers = 8,
               state = state.toStartState(),
               onAction = { action ->
                   when (action) {
                       StartAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                       StartAction.NavigateToGame -> navController.navigate(Route.MyGame.Game)
                       else -> viewModel.onStartAction(action)
                   }
               }
           )
       }
       composableGameScreen<Route.MyGame.Game> {
           val viewModel = it.sharedViewModel<MyGameViewModel>(navController)
           val state by viewModel.state.collectAsStateWithLifecycle()
           MyGameGameScreen(
               state = state,
               onAction = { action ->
                   when (action) {
                       MyGameAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                       MyGameAction.EndGame -> navController.navigate(Route.MyGame.End)
                       else -> viewModel.onAction(action)
                   }
               }
           )
       }
       composableGameScreen<Route.MyGame.End> {
           val viewModel = it.sharedViewModel<MyGameViewModel>(navController)
           val state by viewModel.state.collectAsStateWithLifecycle()
           // Sort players & render shared EndScreen(...)
       }
   }
   ```

### Step 8: Expose in Main Menu
In `app/src/main/java/de/michael/tolleapp/main/util/GameNavigationData.kt`:
```kotlin
GameNavigationData(
    name = "My New Game",
    gameRoute = Route.MyGameNav
)
```

### Step 9: Integrate Statistics (Optional)
If the new game records player statistics:
1. Add `<Game>StatisticsDao` in `<Game>Dao.kt`, `AppDatabase.kt`, and `di/Module.kt`.
2. Add `<Game>Stats` data class in `statistics/gameStats/` and `<Game>StatsTable` composable in `statistics/screens/`.
3. Wire into `StatState.kt`, `StatViewModel.kt`, and `StatScreen.kt` (`GameSelector`, `gameLabel`, and `when (state.selectedGame)`).

---

## 3. Key Conventions & Best Practices

1. **Material 3 Dividers**: Never use `Divider()`. Always use `HorizontalDivider()` or `VerticalDivider()`.
2. **Filename Gotcha**: `DividedScreen` is defined in `games/util/DevidedScreen.kt` (note spelling).
3. **Text Fields**: Use `BetterOutlinedTextField` (`games/util/BetterOutlinedTextField.kt`) and `PlayerDisplayRow` (`games/util/PlayerDisplayRow.kt`) for gameplay player score items.
4. **Player Limits**: Pass `minPlayers` and `maxPlayers` to `StartScreen(minPlayers = X, maxPlayers = Y, ...)` and guard `StartAction.SelectPreset` in the ViewModel if `maxPlayers` is bounded.
5. **Tables & Sorting**: Use `Table`, `getSortDirectionButtonComposableList` (`SortDirectionButton.kt`), and `toTableHeader` / `toTableRowCell` / `toTableTotalCell` (`StringMappers.kt`) from `games/util/table/`.
6. **Localization**: UI text is German. Keep game strings consistent in German (e.g., `"Geber"`, `"Runde beenden"`, `"Punkte"`, `"Gesamt"`, `"Zum Hauptmenü"`, `"Nochmal spielen"`), and update both `res/values/strings.xml` and `res/values-de/strings.xml` when adding string resources.
7. **Verification**: Always run `./gradlew compileDebugKotlin` and `./gradlew testDebugUnitTest` (plus `./gradlew lintDebug` for UI/manifest/resource changes) before finishing.
