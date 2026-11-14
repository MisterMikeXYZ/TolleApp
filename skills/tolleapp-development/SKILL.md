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
   - `<Game>GameEntity.kt` (game metadata, timestamp, isFinished, etc.)
   - `<Game>RoundEntity.kt` (round index, scores mapping)
   - `<Game>GamePlayerEntity.kt` (player associations)
2. DAO in `<Game>Dao.kt`:
   - `@Dao` with CRUD methods, queries for active/paused games and rounds.
3. Mappers in `mappers/`:
   - `GameMappers.kt` and `RoundMappers.kt` converting between Room entities and domain models.
4. Repository Implementation in `<Game>RepositoryImpl.kt`:
   - Implements the domain repository interface.

### Step 4: Register in Database (`data/AppDatabase.kt`)
1. Add entity classes to `@Database(entities = [..., MyGameEntity::class, MyGameRoundEntity::class], version = <N+1>)`.
2. Add abstract DAO method: `abstract fun myGameDao(): MyGameDao`.
3. Note: If version was bumped, run `./gradlew compileDebugKotlin` to export the new schema JSON.

### Step 5: Implement Presentation Layer (`games/<game>/presentation/`)
1. **`<Game>Action.kt`**: Sealed interface for user intents:
   ```kotlin
   sealed interface MyGameAction {
       data object EndRound : MyGameAction
       data object UndoLastRound : MyGameAction
       data object EndGame : MyGameAction
       data class InputScore(val playerId: String, val score: Int) : MyGameAction
   }
   ```
2. **`<Game>State.kt`**: Immutable state data class:
   ```kotlin
   data class MyGameState(
       val allPlayers: List<Player> = emptyList(),
       val presets: List<GamePresetWithPlayers> = emptyList(),
       val pausedGames: List<PausedGame> = emptyList(),
       val selectedPlayers: List<Player?> = listOf(null, null),
       val selectedPlayerIds: List<String?> = listOf(null, null),
       val rounds: List<MyGameRoundData> = emptyList(),
       val currentGameId: String = "",
       val isGameEnded: Boolean = false,
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
   - Injects repository, playerRepo, presetRepo.
   - Combines flows into `val state: StateFlow<MyGameState>`.
   - Implements `fun onStartAction(action: StartAction)` for the shared start screen.
   - Implements `fun onAction(action: MyGameAction)` for in-game actions.
4. **`<Game>GameScreen.kt`**:
   - Uses `DevidedScreen` or `CustomTopBar`.
   - Uses `Table` for round display.
   - Uses or extends keyboards in `games/util/keyboards/`.
   - Handles `OnHomeDialog` when exiting active game.

### Step 6: Register in DI (`di/Module.kt`)
```kotlin
single { get<AppDatabase>().myGameDao() }
single<MyGameRepository> { MyGameRepositoryImpl(get(), get()) }
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
   ```kotlin
   navigation<Route.MyGameNav>(startDestination = Route.MyGame.Start) {
       composable<Route.MyGame.Start> { ... }
       composable<Route.MyGame.Game> { ... }
       composable<Route.MyGame.End> { ... }
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

---

## 3. Key Conventions & Best Practices

1. **Material 3 Dividers**: Never use `Divider()`. Always use `HorizontalDivider()` or `VerticalDivider()`.
2. **Text Fields**: Use `BetterOutlinedTextField` from `games/util/BetterOutlinedTextField.kt` for player inputs.
3. **Player Limits**: When configuring a game, pass `minPlayers` and `maxPlayers` to `StartScreen(minPlayers = X, maxPlayers = Y, ...)`.
4. **Sorting Tables**: Use `SortDirectionButton` and `Table` from `games/util/table/`.
5. **Localization**: UI text is German. Use clear German game terminology (e.g. "Geber", "Runde beenden", "Punkte eingeben", "Gewinner").
