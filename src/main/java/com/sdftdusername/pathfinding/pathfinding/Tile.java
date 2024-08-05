package com.sdftdusername.pathfinding.pathfinding;

public class Tile {
    public final int x;
    public final int y;
    public final int z;
    public int score;
    public Tile parent;
    public boolean open;
    public boolean walkThrough;
    public boolean jump;

    public Tile(boolean walkThrough, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        score = 0;
        parent = null;
        open = walkThrough;
        this.walkThrough = walkThrough;
        jump = false;
    }

    public Tile(Tile tile) {
        x = tile.x;
        y = tile.y;
        z = tile.z;
        score = 0;
        parent = null;
        open = true;
        walkThrough = true;
        jump = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tile) {
            return x == ((Tile) obj).x && y == ((Tile) obj).y;
        }
        return false;
    }
}
