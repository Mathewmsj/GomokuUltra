public class Player {
    public int id; // 0:黑, 1:白
    public String name;
    public int strategyPoints = 0;
    public boolean isMyTurn = false;
    public boolean usedBomb = false;
    public boolean usedFreeze = false;
    public boolean usedRemove = false;
    public int remainObstacle = 5;
    public int remainFreeze = 3;
    public int remainBomb = 3;
    public int remainRemove = 3;
    public int remainOverride = 3;
    public int removeSuccessRate = 25; // 百分比
 
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }
} 