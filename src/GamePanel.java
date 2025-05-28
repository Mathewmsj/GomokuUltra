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
    public boolean waitingForAnnihilate = false;
    public int freezeWaitPlayer = -1;
    public int bombWaitPlayer = -1;
    public int freezeWaitX = -1, freezeWaitY = -1;
    public int bombWaitX = -1, bombWaitY = -1;
    public String message = "";
    private boolean gameOver = false;
    private boolean waitingForOverride = false;
    private boolean showBombEffect = false;

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

        JButton obstacleBtn = new JButton("Obstacle Piece (2 pts)");
        obstacleBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        obstacleBtn.addActionListener(e -> {
            if (players[currentPlayer].strategyPoints >= 2) {
                message = "Click an empty cell to place an obstacle piece";
                waitingForAnnihilate = false;
                waitingForFreeze = false;
                waitingForBomb = false;
                waitingForObstacle = true;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        add(obstacleBtn);
        btnY += btnHeight + btnGap;

        JButton freezeBtn = new JButton("Freeze Piece (5 pts)");
        freezeBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        freezeBtn.addActionListener(e -> {
            if (players[currentPlayer].strategyPoints >= 5) {
                message = "Click anywhere to place a freeze piece";
                waitingForFreeze = true;
                freezeWaitPlayer = currentPlayer;
                waitingForAnnihilate = false;
                waitingForBomb = false;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        add(freezeBtn);
        btnY += btnHeight + btnGap;

        JButton annihilateBtn = new JButton("Annihilate Piece (2 pts)");
        annihilateBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        annihilateBtn.addActionListener(e -> {
            if (players[currentPlayer].strategyPoints >= 2) {
                message = "Click an enemy piece or obstacle to annihilate";
                waitingForAnnihilate = true;
                waitingForFreeze = false;
                waitingForBomb = false;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        add(annihilateBtn);
        btnY += btnHeight + btnGap;

        JButton bombBtn = new JButton("Bomb Piece (5 pts)");
        bombBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        bombBtn.addActionListener(e -> {
            if (players[currentPlayer].strategyPoints >= 5) {
                message = "Click anywhere to place a bomb piece";
                waitingForBomb = true;
                bombWaitPlayer = currentPlayer;
                waitingForFreeze = false;
                waitingForAnnihilate = false;
                waitingForObstacle = false;
                waitingForOverride = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        add(bombBtn);
        btnY += btnHeight + btnGap;

        JButton overrideBtn = new JButton("Override Piece (15 pts)");
        overrideBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        overrideBtn.addActionListener(e -> {
            if (players[currentPlayer].strategyPoints >= 15) {
                message = "Click anywhere to override with your piece";
                waitingForOverride = true;
                waitingForAnnihilate = false;
                waitingForFreeze = false;
                waitingForBomb = false;
                waitingForObstacle = false;
            } else {
                message = "Not enough strategy points!";
            }
            repaint();
        });
        add(overrideBtn);
        btnY += btnHeight + btnGap;

        JButton restartBtn = new JButton("Restart");
        restartBtn.setBounds(btnX, btnY, btnWidth, btnHeight);
        restartBtn.addActionListener(e -> {
            resetGame();
            repaint();
        });
        add(restartBtn);
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
                player.strategyPoints -= 2;
                message = "Obstacle placed! You can continue to move.";
                waitingForObstacle = false;
            } else {
                message = "Can only place on empty cell!";
            }
            repaint();
            return;
        }
        if (waitingForFreeze) {
            cell.piece = new FreezePiece(currentPlayer, 5);
            // Mark freeze area
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx, ny = y + dy;
                    if (inBoard(nx, ny)) {
                        board[nx][ny].piece.isFrozenArea = true;
                    }
                }
            }
            player.strategyPoints -= 5;
            message = "Freeze piece placed!";
            waitingForFreeze = false;
            nextTurn();
            repaint();
            return;
        }
        if (waitingForAnnihilate) {
            if ((cell.piece instanceof NormalPiece || cell.piece instanceof ObstaclePiece) && cell.piece.owner != currentPlayer) {
                cell.piece = new EmptyPiece();
                player.strategyPoints -= 2;
                message = "Annihilate successful!";
                waitingForAnnihilate = false;
                nextTurn();
            } else {
                message = "Can only remove enemy piece or obstacle!";
            }
            repaint();
            return;
        }
        if (waitingForBomb) {
            cell.piece = new BombPiece(currentPlayer);
            player.strategyPoints -= 5;
            message = "Bomb placed! You can continue to move.";
            waitingForBomb = false;
            bombWaitX = x;
            bombWaitY = y;
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
    }

    public boolean inBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean checkWin(int x, int y) {
        ChessPiece t = board[x][y].piece;
        if (t instanceof NormalPiece) return false;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int cnt = 1;
            for (int k = 1; k < 5; k++) {
                int nx = x + d[0]*k, ny = y + d[1]*k;
                if (!inBoard(nx, ny)) break;
                if (board[nx][ny].piece instanceof NormalPiece && !board[nx][ny].piece.isFrozenArea) cnt++;
                else break;
            }
            for (int k = 1; k < 5; k++) {
                int nx = x - d[0]*k, ny = y - d[1]*k;
                if (!inBoard(nx, ny)) break;
                if (board[nx][ny].piece instanceof NormalPiece && !board[nx][ny].piece.isFrozenArea) cnt++;
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
        waitingForAnnihilate = false;
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
    }

    // 展示游戏规则方法
    private void showGameRules() {
        String rules = "Gomoku Ultra Rules and Special Pieces:\n" +
                "1. Normal Piece (Black/White): Gain 1 strategy point per move.\n" +
                "2. Obstacle Piece: Costs 2 points, place on an empty cell, lasts 4 turns in total, you can continue to move after placing.\n" +
                "3. Freeze Piece: Costs 5 points, place anywhere, lasts 5 turns, freezes a 3x3 area. Enemy pieces in the area cannot form a line, and both sides cannot place pieces in the area.\n" +
                "4. Annihilate Piece: Costs 2 points, remove an enemy normal or obstacle piece.\n" +
                "5. Bomb Piece: Costs 5 points, place anywhere, instantly destroys all pieces in a 3x3 area (if a freeze piece is in the area, it is removed and the area is unfrozen), you can continue to move after placing.\n" +
                "6. Override Piece: Costs 15 points, place your normal piece anywhere (including freeze area, obstacles, or enemy pieces), overrides the original.\n" +
                "7. White starts with 1 strategy point.\n" +
                "8. No moves allowed after someone wins.";
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
} 