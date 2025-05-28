public abstract class ChessPiece {
    public int owner; // 0:黑, 1:白, -1:无
    public abstract String getType();
    public boolean isFrozenArea = false;
}

class EmptyPiece extends ChessPiece {
    public EmptyPiece() { this.owner = -1; }
    public String getType() { return "EMPTY"; }
}

class NormalPiece extends ChessPiece {
    public NormalPiece(int owner) { this.owner = owner; }
    public String getType() { return owner == 0 ? "BLACK" : "WHITE"; }
}

class ObstaclePiece extends ChessPiece {
    public int turnsLeft;
    public ObstaclePiece(int owner, int turns) {
        this.owner = owner;
        this.turnsLeft = turns;
    }
    public String getType() { return "OBSTACLE"; }
}

class FreezePiece extends ChessPiece {
    public int freezeTurns;
    public FreezePiece(int owner, int turns) {
        this.owner = owner;
        this.freezeTurns = turns;
    }
    public String getType() { return "FREEZE"; }
}

class BombPiece extends ChessPiece {
    public BombPiece(int owner) { this.owner = owner; }
    public String getType() { return "BOMB"; }
} 