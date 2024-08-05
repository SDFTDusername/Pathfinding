package com.sdftdusername.pathfinding.pathfinding;

import java.util.Comparator;

public class TileScoreComparator implements Comparator<Tile> {
    @Override
    public int compare(Tile o1, Tile o2) {
        return o1.score - o2.score;
    }
}
