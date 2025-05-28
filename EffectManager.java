public class EffectManager {
    private GamePanel panel;

    public EffectManager(GamePanel panel) {
        this.panel = panel;
    }

    // 每回合调用，处理障碍棋、冻结棋等效果
    public void processTurn() {
        for (int i = 0; i < panel.BOARD_SIZE; i++) {
            for (int j = 0; j < panel.BOARD_SIZE; j++) {
                Cell cell = panel.board[i][j];
                // 障碍棋
                if (cell.piece instanceof ObstaclePiece) {
                    ObstaclePiece op = (ObstaclePiece) cell.piece;
                    op.turnsLeft--;
                    if (op.turnsLeft < 0) {
                        cell.piece = new EmptyPiece();
                    }
                }
                // 冻结棋
                if (cell.piece instanceof FreezePiece) {
                    FreezePiece fp = (FreezePiece) cell.piece;
                    fp.freezeTurns--;
                    if (fp.freezeTurns <= 0) {
                        cell.piece = new EmptyPiece();
                        // 解除冻结范围
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int nx = i + dx, ny = j + dy;
                                if (panel.inBoard(nx, ny)) {
                                    panel.board[nx][ny].piece.isFrozenArea = false;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 