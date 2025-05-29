# GomokuUltra

A feature-rich Gomoku (Five-in-a-Row) game with strategy points and multiple special pieces.

## Game Overview
- **Normal Piece**: Place to gain 1 strategy point. Black and White alternate turns. Five consecutive pieces of the same color (not interrupted by any special piece) win the game.
- **Strategy Points**: Used to activate special pieces.

## Special Pieces & Rules
- **Obstacle Piece** (2 pts, 5 per player): Place on an empty cell to block it for 4 turns. Cannot be placed on frozen areas or occupied cells.
- **Freeze Piece** (5 pts, 3 per player): Place anywhere. After 3 turns, activates and freezes a 3x3 area for 5 turns. Frozen areas cannot be used for victory, and no pieces can be placed there.
- **Bomb Piece** (5 pts, 3 per player): Place anywhere. After 3 turns, explodes and clears a 3x3 area. If a Freeze Piece is in the area, it is removed and the area is unfrozen.
- **Remove Piece** (3 pts, 3 per player): Attempt to remove any piece (including special pieces and your own). First use has 25% success rate, each subsequent use increases by 25% (max 100%). If used on a pending Bomb/Freeze, also uses probability. If used on a Bomb/Freeze already on the board and fails, 25% chance to trigger its effect immediately.
- **Override Piece** (15 pts, 3 per player): Place your normal piece anywhere (even on special pieces or frozen areas), overriding the original.

## Additional Rules
- Each special piece has a limited number of uses per player per game (see above).
- Pending (delayed) Bomb/Freeze can be removed by Remove Piece, also with probability.
- Only consecutive Normal Pieces of the same color count for victory. Special pieces do not count.
- The game ends immediately when a player forms five consecutive normal pieces of their color.
- White starts with 1 strategy point.

## How to Run
```bash
# Compile
javac -d out src/*.java
# Run (assuming the main class is GomokuGame)
java -cp out GomokuGame
```

## Requirements
- JDK 8 or above
- Java Swing (Standard Library)

## Project Structure Example
```
GomokuUltra/
├── src/                # Source code (.java files)
├── out/                # Compiled output (.class files, not uploaded)
├── dist/               # Release directory (jar files)
├── assets/             # Resources (images, sounds, etc., if any)
├── .gitignore
├── README.md
├── LICENSE
```

## Author
- [Your Name](https://github.com/YourGitHubUsername) 