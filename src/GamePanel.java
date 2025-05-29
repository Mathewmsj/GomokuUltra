import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements MouseListener {
    public final int BOARD_SIZE = 15;
    public final int CELL_SIZE = 40;
    public final int OFFSET = 50;
    public Cell[][] board = new Cell[BOARD_SIZE][BOARD_SIZE];
    public Player[] players = new Player[2];
    public int currentPlayer = 0;
    public EffectManager effectManager;
    public boolean waitingForFreeze = false;
    public boolean waitingForBomb = false;
    public boolean waitingForRemove = false;
    public int freezeWaitPlayer = -1;
    public int bombWaitPlayer = -1;
    public int freezeWaitX = -1, freezeWaitY = -1;
    public int bombWaitX = -1, bombWaitY = -1;
    public String message = "";
    private boolean gameOver = false;
    private boolean waitingForOverride = false;
    private boolean showBombEffect = false;
    private int pendingBombX = -1, pendingBombY = -1, bombDelay = 0;
    private int pendingFreezeX = -1, pendingFreezeY = -1, freezeDelay = 0;
    private JButton freezeBtn, bombBtn, removeBtn, obstacleBtn, overrideBtn;
    private JLabel freezeStatusLabel, bombStatusLabel, removeStatusLabel, obstacleStatusLabel, overrideStatusLabel;

    public GamePanel() {
        setPreferredSize(new Dimension(870, 680));
        setBackground(new Color(245, 222, 179));
        addMouseListener(this);
        // 展示游戏规则
        showGameRules();
        // 初始化棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
        // 初始化玩家
        players[0] = new Player(0, "Black");
        players[1] = new Player(1, "White");
        players[0].isMyTurn = true;
        players[1].strategyPoints = 1; // White starts with 1 strategy point
        // 效果管理器
        effectManager = new EffectManager(this);
        // UI按钮
        addButtons();
    }

    private void addButtons() {
        setLayout(null);
        int btnWidth = 170;
        int btnHeight = 35;
        int btnX = 650;
        int btnY = 100;
        int btnGap = 18;

        obstacleBtn = new JButton("Obstacle Piece (2 pts)");
        obstacleBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        add(obstacleBtn);
        obstacleStatusLabel = new JLabel();
        obstacleStatusLabel.setBounds(btnX + btnWidth + 10, btnY, 30, btnHeight);
        add(obstacleStatusLabel);
        btnY += btnHeight + btnGap;

        freezeBtn = new JButton("Freeze Piece (5 pts)");
        freezeBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        add(freezeBtn);
        freezeStatusLabel = new JLabel();
        freezeStatusLabel.setBounds(btnX + btnWidth + 10, btnY, 30, btnHeight);
        add(freezeStatusLabel);
        btnY += btnHeight + btnGap;

        removeBtn = new JButton("Remove Piece (3 pts)");
        removeBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        add(removeBtn);
        removeStatusLabel = new JLabel();
        removeStatusLabel.setBounds(btnX + btnWidth + 10, btnY, 30, btnHeight);
        add(removeStatusLabel);
        btnY += btnHeight + btnGap;

        bombBtn = new JButton("Bomb Piece (5 pts)");
        bombBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        add(bombBtn);
        bombStatusLabel = new JLabel();
        bombStatusLabel.setBounds(btnX + btnWidth + 10, btnY, 30, btnHeight);
        add(bombStatusLabel);
        btnY += btnHeight + btnGap;

        overrideBtn = new JButton("Override Piece (15 pts)");
        overrideBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        add(overrideBtn);
        overrideStatusLabel = new JLabel();
        overrideStatusLabel.setBounds(btnX + btnWidth + 10, btnY, 30, btnHeight);
        add(overrideStatusLabel);
        btnY += btnHeight + btnGap;

        JButton restartBtn = new JButton("Restart");
        restartBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        restartBtn.addActionListener(e -> {
            resetGame();
            repaint();
        });
        add(restartBtn);

        // Obstacle Piece
        obstacleBtn.addActionListener(e -> {
            if (players[currentPlayer].remainObstacle <= 0) {
                message = "No Obstacle Piece left!";
            } else if (players[currentPlayer].strategyPoints >= 2) {
                message = "Click an empty cell to place an obstacle piece";
                waitingForRemove = false;
                waitingForFreeze = false;
                waitingForBomb = false;
                waitingForObstacle = true;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        // Freeze Piece
        freezeBtn.addActionListener(e -> {
            if (players[currentPlayer].remainFreeze <= 0) {
                message = "No Freeze Piece left!";
            } else if (players[currentPlayer].strategyPoints >= 5) {
                message = "Click anywhere to place a freeze piece";
                waitingForFreeze = true;
                freezeWaitPlayer = currentPlayer;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        // Remove Piece
        removeBtn.addActionListener(e -> {
            if (players[currentPlayer].remainRemove <= 0) {
                message = "No Remove Piece left!";
            } else if (players[currentPlayer].strategyPoints >= 3) {
                message = "Click an enemy piece or obstacle to remove";
                waitingForRemove = true;
                waitingForFreeze = false;
                waitingForBomb = false;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        // Bomb Piece
        bombBtn.addActionListener(e -> {
            if (players[currentPlayer].remainBomb <= 0) {
                message = "No Bomb Piece left!";
            } else if (players[currentPlayer].strategyPoints >= 5) {
                message = "Click anywhere to place a bomb piece";
                waitingForBomb = true;
                bombWaitPlayer = currentPlayer;
                waitingForFreeze = false;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
    }

    private boolean waitingForObstacle = false;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 棋盘
        g.setColor(new Color(184, 134, 11));
        for (int i = 0; i < BOARD_SIZE; i++) {
            g.drawLine(OFFSET, OFFSET + i * CELL_SIZE, OFFSET + (BOARD_SIZE - 1) * CELL_SIZE, OFFSET + i * CELL_SIZE);
            g.drawLine(OFFSET + i * CELL_SIZE, OFFSET, OFFSET + i * CELL_SIZE, OFFSET + (BOARD_SIZE - 1) * CELL_SIZE);
        }
        // 星位
        int[] star = {3, 7, 11};
        for (int x : star) for (int y : star) {
            g.fillOval(OFFSET + x * CELL_SIZE - 4, OFFSET + y * CELL_SIZE - 4, 8, 8);
        }
        // 棋子
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];
                int cx = OFFSET + i * CELL_SIZE;
                int cy = OFFSET + j * CELL_SIZE;
                ChessPiece piece = cell.piece;
                if (piece instanceof NormalPiece) {
                    drawPiece(g, cx, cy, piece.owner == 0 ? Color.BLACK : Color.WHITE, false, piece.isFrozenArea);
                } else if (piece instanceof ObstaclePiece) {
                    drawPiece(g, cx, cy, Color.GRAY, true, false);
                } else if (piece instanceof FreezePiece) {
                    drawPiece(g, cx, cy, new Color(0, 191, 255), true, false);
                    // 冻结范围
                    g.setColor(new Color(135, 206, 250, 80));
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = i + dx, ny = j + dy;
                            if (inBoard(nx, ny)) {
                                g.fillRect(OFFSET + nx * CELL_SIZE - CELL_SIZE / 2 + 1, OFFSET + ny * CELL_SIZE - CELL_SIZE / 2 + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                            }
                        }
                    }
                } else if (piece instanceof BombPiece) {
                    drawPiece(g, cx, cy, Color.RED, true, false);
                }
            }
        }
        // 爆炸特效
        if (showBombEffect) {
            g.setColor(new Color(255, 0, 0, 120));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // 预览即将生效的冰冻棋和炸弹棋
        if (pendingFreezeX != -1 && pendingFreezeY != -1 && freezeDelay > 0) {
            int cx = OFFSET + pendingFreezeX * CELL_SIZE;
            int cy = OFFSET + pendingFreezeY * CELL_SIZE;
            g.setColor(new Color(0, 191, 255, 120)); // 半透明蓝色
            g.fillOval(cx - CELL_SIZE / 2, cy - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
            g.setColor(new Color(0, 191, 255, 200));
            g.drawOval(cx - CELL_SIZE / 2, cy - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
        }
        if (pendingBombX != -1 && pendingBombY != -1 && bombDelay > 0) {
            int cx = OFFSET + pendingBombX * CELL_SIZE;
            int cy = OFFSET + pendingBombY * CELL_SIZE;
            g.setColor(new Color(255, 0, 0, 120)); // 半透明红色
            g.fillOval(cx - CELL_SIZE / 2, cy - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
            g.setColor(new Color(255, 0, 0, 200));
            g.drawOval(cx - CELL_SIZE / 2, cy - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
        }
        // UI
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Current Turn: " + (currentPlayer == 0 ? "Black" : "White"), 640, 50);
        g.drawString("Black Strategy Points: " + players[0].strategyPoints, 630, 450);
        g.drawString("White Strategy Points: " + players[1].strategyPoints, 630, 480);
        g.setColor(Color.BLUE);
        // 自动多行居中显示消息
        int msgY = 520;
        int lineHeight = 25;
        int msgX = 620;
        int maxLineLen = 30; // 每行最大字符数
        String[] msgLines = message.split("\\n");
        FontMetrics fm = g.getFontMetrics();
        int infoPanelWidth = 240; // 870-630
        for (String line : msgLines) {
            while (line.length() > maxLineLen) {
                int breakPos = line.lastIndexOf(' ', maxLineLen);
                if (breakPos == -1) breakPos = maxLineLen;
                String sub = line.substring(0, breakPos);
                int strWidth = fm.stringWidth(sub);
                int drawX = msgX + (infoPanelWidth - strWidth) / 2;
                g.drawString(sub, drawX, msgY);
                line = line.substring(breakPos).trim();
                msgY += lineHeight;
            }
            int strWidth = fm.stringWidth(line);
            int drawX = msgX + (infoPanelWidth - strWidth) / 2;
            g.drawString(line, drawX, msgY);
            msgY += lineHeight;
        }
    }

    private void drawPiece(Graphics g, int x, int y, Color color, boolean special, boolean frozen) {
        Graphics2D g2 = (Graphics2D) g;
        if (special) {
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setStroke(new BasicStroke(1));
        }
        if (frozen) {
            g2.setColor(new Color(100, 100, 255, 120));
            g2.fillOval(x - CELL_SIZE / 2, y - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
        }
        g2.setColor(color);
        g2.fillOval(x - CELL_SIZE / 2, y - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(x - CELL_SIZE / 2, y - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameOver) return;
        int mx = e.getX(), my = e.getY();
        int x = (mx - OFFSET + CELL_SIZE / 2) / CELL_SIZE;
        int y = (my - OFFSET + CELL_SIZE / 2) / CELL_SIZE;
        if (!inBoard(x, y)) return;
        Cell cell = board[x][y];
        Player player = players[currentPlayer];
        Player enemy = players[1 - currentPlayer];
        // "Override Piece" priority
        if (waitingForOverride) {
            cell.piece = new NormalPiece(currentPlayer);
            player.strategyPoints -= 15;
            message = "Override placed successfully!";
            waitingForOverride = false;
            if (checkWin(x, y)) {
                message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                gameOver = true;
                repaint();
                return;
            }
            nextTurn();
            repaint();
            return;
        }
        // Special piece priority
        if (waitingForObstacle) {
            if (cell.piece instanceof EmptyPiece && !cell.piece.isFrozenArea) {
                cell.piece = new ObstaclePiece(currentPlayer, 3);
                players[currentPlayer].strategyPoints -= 2;
                players[currentPlayer].remainObstacle--;
                message = "Obstacle placed! You can continue to move.";
                waitingForObstacle = false;
                if (checkWin(x, y)) {
                    message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                    gameOver = true;
                    repaint();
                    return;
                }
            } else {
                message = "Can only place on empty cell!";
            }
            repaint();
            return;
        }
        if (waitingForFreeze) {
            pendingFreezeX = x;
            pendingFreezeY = y;
            freezeDelay = 3;
            players[currentPlayer].strategyPoints -= 5;
            players[currentPlayer].remainFreeze--;
            message = "Freeze Piece will activate in 3 turns!";
            waitingForFreeze = false;
            if (checkWin(x, y)) {
                message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                gameOver = true;
                repaint();
                return;
            }
            nextTurn();
            repaint();
            return;
        }
        if (waitingForRemove) {
            // 允许移除延迟中的预览炸弹棋
            if (pendingBombX == x && pendingBombY == y && bombDelay > 0) {
                int rand = (int)(Math.random() * 100) + 1;
                boolean success = rand <= player.removeSuccessRate;
                player.strategyPoints -= 3;
                player.remainRemove--;
                if (success) {
                    pendingBombX = pendingBombY = -1;
                    bombDelay = 0;
                    message = "Pending Bomb Piece removed! (" + player.removeSuccessRate + "% chance)";
                } else {
                    message = "Remove failed! (" + player.removeSuccessRate + "% chance)";
                }
                player.removeSuccessRate = Math.min(100, player.removeSuccessRate + 25);
                waitingForRemove = false;
                repaint();
                return;
            }
            // 允许移除延迟中的预览冰冻棋
            if (pendingFreezeX == x && pendingFreezeY == y && freezeDelay > 0) {
                int rand = (int)(Math.random() * 100) + 1;
                boolean success = rand <= player.removeSuccessRate;
                player.strategyPoints -= 3;
                player.remainRemove--;
                if (success) {
                    pendingFreezeX = pendingFreezeY = -1;
                    freezeDelay = 0;
                    message = "Pending Freeze Piece removed! (" + player.removeSuccessRate + "% chance)";
                } else {
                    message = "Remove failed! (" + player.removeSuccessRate + "% chance)";
                }
                player.removeSuccessRate = Math.min(100, player.removeSuccessRate + 25);
                waitingForRemove = false;
                repaint();
                return;
            }
            boolean isBombOrFreeze = (cell.piece instanceof BombPiece) || (cell.piece instanceof FreezePiece);
            int rand = (int)(Math.random() * 100) + 1;
            boolean success = rand <= player.removeSuccessRate;
            boolean triggerEffect = false;
            if (isBombOrFreeze && !success) {
                int triggerRand = (int)(Math.random() * 100) + 1;
                triggerEffect = triggerRand <= 25;
            }
            if (cell.piece instanceof EmptyPiece) {
                message = "Cannot remove an empty cell!";
            } else if (success) {
                cell.piece = new EmptyPiece();
                player.strategyPoints -= 3;
                player.remainRemove--;
                message = "Remove successful! (" + player.removeSuccessRate + "% chance)";
                player.removeSuccessRate = Math.min(100, player.removeSuccessRate + 25);
                waitingForRemove = false;
                if (checkWin(x, y)) {
                    message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                    gameOver = true;
                    repaint();
                    return;
                }
                nextTurn();
            } else if (isBombOrFreeze && triggerEffect) {
                if (cell.piece instanceof BombPiece) {
                    int bx = x, by = y;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = bx + dx, ny = by + dy;
                            if (inBoard(nx, ny)) {
                                Cell c = board[nx][ny];
                                if (c.piece instanceof FreezePiece) {
                                    c.piece = new EmptyPiece();
                                    c.piece.isFrozenArea = false;
                                    for (int ddx = -1; ddx <= 1; ddx++) {
                                        for (int ddy = -1; ddy <= 1; ddy++) {
                                            int nnx = nx + ddx, nny = ny + ddy;
                                            if (inBoard(nnx, nny)) {
                                                board[nnx][nny].piece.isFrozenArea = false;
                                            }
                                        }
                                    }
                                } else {
                                    c.piece = new EmptyPiece();
                                }
                            }
                        }
                    }
                    triggerBombEffect();
                } else if (cell.piece instanceof FreezePiece) {
                    int fx = x, fy = y;
                    board[fx][fy].piece = new FreezePiece(currentPlayer, 5);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = fx + dx, ny = fy + dy;
                            if (inBoard(nx, ny)) {
                                board[nx][ny].piece.isFrozenArea = true;
                            }
                        }
                    }
                    message = "Freeze Piece activated early!";
                }
                player.strategyPoints -= 3;
                player.remainRemove--;
                player.removeSuccessRate = Math.min(100, player.removeSuccessRate + 25);
                waitingForRemove = false;
                if (checkWin(x, y)) {
                    message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                    gameOver = true;
                    repaint();
                    return;
                }
                nextTurn();
            } else {
                player.strategyPoints -= 3;
                player.remainRemove--;
                message = "Remove failed! (" + player.removeSuccessRate + "% chance)";
                player.removeSuccessRate = Math.min(100, player.removeSuccessRate + 25);
                waitingForRemove = false;
                nextTurn();
            }
            repaint();
            return;
        }
        if (waitingForBomb) {
            pendingBombX = x;
            pendingBombY = y;
            bombDelay = 3;
            players[currentPlayer].strategyPoints -= 5;
            players[currentPlayer].remainBomb--;
            message = "Bomb Piece will activate in 3 turns!";
            waitingForBomb = false;
            if (checkWin(x, y)) {
                message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                gameOver = true;
                repaint();
                return;
            }
            nextTurn();
            repaint();
            return;
        }
        // Normal move
        if (cell.piece instanceof EmptyPiece && !cell.piece.isFrozenArea) {
            cell.piece = new NormalPiece(currentPlayer);
            player.strategyPoints++;
            message = "";
            if (checkWin(x, y)) {
                message = (currentPlayer == 0 ? "Black" : "White") + " wins!";
                gameOver = true;
                repaint();
                return;
            }
            nextTurn();
        } else {
            message = "Cannot place here!";
        }
        repaint();
    }

    private void nextTurn() {
        boolean bombTriggered = false;
        // 爆炸棋生效
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];
                if (cell.piece instanceof BombPiece) {
                    bombTriggered = true;
                    // 炸掉3x3
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = i + dx, ny = j + dy;
                            if (inBoard(nx, ny)) {
                                Cell c = board[nx][ny];
                                if (c.piece instanceof FreezePiece) {
                                    // 解冻，保留棋子
                                    c.piece = new EmptyPiece();
                                    c.piece.isFrozenArea = false;
                                    for (int ddx = -1; ddx <= 1; ddx++) {
                                        for (int ddy = -1; ddy <= 1; ddy++) {
                                            int nnx = nx + ddx, nny = ny + ddy;
                                            if (inBoard(nnx, nny)) {
                                                board[nnx][nny].piece.isFrozenArea = false;
                                            }
                                        }
                                    }
                                } else {
                                    c.piece = new EmptyPiece();
                                }
                            }
                        }
                    }
                    cell.piece = new EmptyPiece();
                }
            }
        }
        if (bombTriggered) {
            triggerBombEffect();
        }
        // 效果管理
        effectManager.processTurn();
        // 回合切换
        currentPlayer = 1 - currentPlayer;
        if (bombDelay > 0) bombDelay--;
        if (bombDelay == 0 && pendingBombX != -1 && pendingBombY != -1) {
            // 执行炸弹效果
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = pendingBombX + dx, ny = pendingBombY + dy;
                    if (inBoard(nx, ny)) {
                        Cell c = board[nx][ny];
                        if (c.piece instanceof FreezePiece) {
                            c.piece = new EmptyPiece();
                            c.piece.isFrozenArea = false;
                            for (int ddx = -1; ddx <= 1; ddx++) {
                                for (int ddy = -1; ddy <= 1; ddy++) {
                                    int nnx = nx + ddx, nny = ny + ddy;
                                    if (inBoard(nnx, nny)) {
                                        board[nnx][nny].piece.isFrozenArea = false;
                                    }
                                }
                            }
                        } else {
                            c.piece = new EmptyPiece();
                        }
                    }
                }
            }
            triggerBombEffect();
            pendingBombX = pendingBombY = -1;
        }
        if (freezeDelay > 0) freezeDelay--;
        if (freezeDelay == 0 && pendingFreezeX != -1 && pendingFreezeY != -1) {
            // 执行冰冻效果
            board[pendingFreezeX][pendingFreezeY].piece = new FreezePiece(currentPlayer, 5);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = pendingFreezeX + dx, ny = pendingFreezeY + dy;
                    if (inBoard(nx, ny)) {
                        board[nx][ny].piece.isFrozenArea = true;
                    }
                }
            }
            message = "Freeze Piece activated!";
            pendingFreezeX = pendingFreezeY = -1;
        }
        refreshButtonStatus();
    }

    public boolean inBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean checkWin(int x, int y) {
        ChessPiece t = board[x][y].piece;
        if (!(t instanceof NormalPiece)) return false;
        int owner = t.owner;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int cnt = 1;
            for (int k = 1; k < 5; k++) {
                int nx = x + d[0]*k, ny = y + d[1]*k;
                if (!inBoard(nx, ny)) break;
                ChessPiece p = board[nx][ny].piece;
                if (p instanceof NormalPiece && !p.isFrozenArea && p.owner == owner) cnt++;
                else break;
            }
            for (int k = 1; k < 5; k++) {
                int nx = x - d[0]*k, ny = y - d[1]*k;
                if (!inBoard(nx, ny)) break;
                ChessPiece p = board[nx][ny].piece;
                if (p instanceof NormalPiece && !p.isFrozenArea && p.owner == owner) cnt++;
                else break;
            }
            if (cnt >= 5) return true;
        }
        return false;
    }

    // 其余 MouseListener 方法
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // 添加重置游戏方法
    private void resetGame() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
        players[0].strategyPoints = 0;
        players[1].strategyPoints = 1; // White starts with 1 strategy point
        currentPlayer = 0;
        players[0].isMyTurn = true;
        players[1].isMyTurn = false;
        waitingForFreeze = false;
        waitingForBomb = false;
        waitingForRemove = false;
        waitingForObstacle = false;
        waitingForOverride = false;
        freezeWaitPlayer = -1;
        bombWaitPlayer = -1;
        freezeWaitX = -1;
        freezeWaitY = -1;
        bombWaitX = -1;
        bombWaitY = -1;
        message = "";
        gameOver = false;
        players[0].usedBomb = false;
        players[0].usedFreeze = false;
        players[0].usedRemove = false;
        players[1].usedBomb = false;
        players[1].usedFreeze = false;
        players[1].usedRemove = false;
        pendingBombX = pendingBombY = -1;
        bombDelay = 0;
        pendingFreezeX = pendingFreezeY = -1;
        freezeDelay = 0;
        players[0].remainObstacle = 5;
        players[0].remainFreeze = 3;
        players[0].remainBomb = 3;
        players[0].remainRemove = 3;
        players[0].remainOverride = 3;
        players[0].removeSuccessRate = 25;
        players[1].remainObstacle = 5;
        players[1].remainFreeze = 3;
        players[1].remainBomb = 3;
        players[1].remainRemove = 3;
        players[1].remainOverride = 3;
        players[1].removeSuccessRate = 25;
        refreshButtonStatus();
    }

    // 展示游戏规则方法
    private void showGameRules() {
        String rules = "Gomoku Ultra Rules and Special Pieces:\n" +
                "1. Normal Piece: Place to gain 1 strategy point. Five consecutive normal pieces of the same color win.\n" +
                "2. Obstacle Piece (2 pts, 5 per player): Place on an empty cell to block it for 4 turns. Cannot be placed on frozen or occupied cells.\n" +
                "3. Freeze Piece (5 pts, 3 per player): Place anywhere. After 3 turns, activates and freezes a 3x3 area for 5 turns. Frozen areas cannot be used for victory, and no pieces can be placed there.\n" +
                "4. Bomb Piece (5 pts, 3 per player): Place anywhere. After 3 turns, explodes and clears a 3x3 area. If a Freeze Piece is in the area, it is removed and the area is unfrozen.\n" +
                "5. Remove Piece (3 pts, 3 per player): Attempt to remove any piece (including special pieces and your own). First use has 25% success rate, each subsequent use increases by 25% (max 100%). If used on a pending Bomb/Freeze, also uses probability. If used on a Bomb/Freeze already on the board and fails, 25% chance to trigger its effect immediately.\n" +
                "6. Override Piece (15 pts, 3 per player): Place your normal piece anywhere (even on special pieces or frozen areas), overriding the original.\n" +
                "7. Each special piece has a limited number of uses per player per game.\n" +
                "8. Pending (delayed) Bomb/Freeze can be removed by Remove Piece, also with probability.\n" +
                "9. Only consecutive Normal Pieces of the same color count for victory. Special pieces do not count.\n" +
                "10. The game ends immediately when a player forms five consecutive normal pieces of their color.\n" +
                "11. White starts with 1 strategy point.";
        JOptionPane.showMessageDialog(this, rules, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private void triggerBombEffect() {
        showBombEffect = true;
        repaint();
        Timer timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showBombEffect = false;
                repaint();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void refreshButtonStatus() {
        obstacleStatusLabel.setText(String.valueOf(players[currentPlayer].remainObstacle));
        freezeStatusLabel.setText(String.valueOf(players[currentPlayer].remainFreeze));
        bombStatusLabel.setText(String.valueOf(players[currentPlayer].remainBomb));
        removeStatusLabel.setText(String.valueOf(players[currentPlayer].remainRemove));
        overrideStatusLabel.setText(String.valueOf(players[currentPlayer].remainOverride));
    }
} 