# Gomoku Ultra

A beautiful and feature-rich Gomoku (Five-in-a-Row) game in Java, with special pieces and strategy points!

## Features
- Classic Gomoku gameplay (15x15 board, Black vs White)
- Strategy points system: gain points by placing normal pieces, spend them to use special pieces
- Special pieces:
  - **Obstacle Piece**: Block a cell for 4 turns
  - **Freeze Piece**: Freeze a 3x3 area for 5 turns, enemy pieces in the area cannot form a line, both sides cannot place pieces there
  - **Annihilate Piece**: Remove an enemy normal or obstacle piece
  - **Bomb Piece**: Instantly destroy all pieces in a 3x3 area (unfreezes freeze pieces)
  - **Override Piece**: Place your normal piece anywhere (even on enemy, obstacle, or freeze area), overriding the original
- Beautiful UI with clear buttons and effects
- Bomb special effect (screen flashes red)
- Game rules popup at start
- English interface

## How to Run
1. Make sure you have Java 8 or above installed.
2. In the project directory, run:
   ```sh
   java -jar GomokuUltra.jar
   ```

## Game Rules (Summary)
1. **Normal Piece (Black/White)**: Gain 1 strategy point per move.
2. **Obstacle Piece**: Costs 2 points, place on an empty cell, lasts 4 turns, you can continue to move after placing.
3. **Freeze Piece**: Costs 5 points, place anywhere, lasts 5 turns, freezes a 3x3 area. Enemy pieces in the area cannot form a line, and both sides cannot place pieces in the area.
4. **Annihilate Piece**: Costs 2 points, remove an enemy normal or obstacle piece.
5. **Bomb Piece**: Costs 5 points, place anywhere, instantly destroys all pieces in a 3x3 area (if a freeze piece is in the area, it is removed and the area is unfrozen), you can continue to move after placing.
6. **Override Piece**: Costs 15 points, place your normal piece anywhere (including freeze area, obstacles, or enemy pieces), overrides the original.
7. White starts with 1 strategy point.
8. No moves allowed after someone wins.

## Author
- Powered by Java & Cursor AI 