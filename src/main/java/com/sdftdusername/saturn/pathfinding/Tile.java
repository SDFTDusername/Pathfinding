package com.sdftdusername.saturn.pathfinding;

public class Tile {
    public final int x;
    public final int y;
    public int score;
    public Tile parent;
    public boolean open;
    public boolean walkThrough;

    public Tile(boolean walkThrough, int x, int y) {
        this.x = x;
        this.y = y;
        score = 0;
        parent = null;
        open = walkThrough;
        this.walkThrough = walkThrough;
    }

    public Tile(Tile tile) {
        x = tile.x;
        y = tile.y;
        score = 0;
        parent = null;
        open = true;
        walkThrough = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tile) {
            return x == ((Tile) obj).x && y == ((Tile) obj).y;
        }
        return false;
    }
}
