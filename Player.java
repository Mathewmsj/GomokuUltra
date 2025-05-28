public class Player {
    public int id; // 0:黑, 1:白
    public String name;
    public int strategyPoints = 0;
    public boolean isMyTurn = false;
 
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }
} 