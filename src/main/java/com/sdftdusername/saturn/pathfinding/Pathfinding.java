package com.sdftdusername.saturn.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Pathfinding {
    private Tile[][] tiles;
    private Tile currentTile;
    private int[] endPosition;
    private int maxWidth;
    private int maxHeight;

    private final TileScoreComparator tileScoreComparator = new TileScoreComparator();

    public List<Tile> getRoute(Tile[][] tiles, int[] startPosition, int[] endPosition) {
        this.tiles = tiles;
        this.maxWidth = tiles.length;
        this.maxHeight = tiles[0].length;
        this.endPosition = endPosition;

        resetAllTiles();

        PriorityQueue<Tile> queue = new PriorityQueue<>(tileScoreComparator);
        queue.add(tiles[startPosition[0]][startPosition[1]]);

        boolean routeAvailable = false;

        while (!queue.isEmpty()) {

            do {
                if (queue.isEmpty()) break;
                currentTile = queue.remove();
            } while (!currentTile.open);

            currentTile.open = false;

            int currentX = currentTile.x;
            int currentY = currentTile.y;
            int currentScore = currentTile.score;

            if (currentTile.x == endPosition[0] && currentTile.y == endPosition[1]) {
                // at the end, return path
                routeAvailable = true;
                break;
            }

            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x+=2) {
                int nextX = currentX + x;
                // currentY is now nextY
                if (validTile(nextX, currentY)) {
                    int score = getScoreOfTile(tiles[nextX][currentY], currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[nextX][currentY];
                    thisTile.score = score;
                    queue.add(thisTile);
                    thisTile.parent = currentTile;
                }
            }

            for (int y = -1; y <= 1; y+=2) {
                // currentX is now nextX
                int nextY = currentY + y;
                if (validTile(currentX, nextY)) {
                    int score = getScoreOfTile(tiles[currentX][nextY], currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[currentX][nextY];
                    thisTile.score = score;
                    queue.add(thisTile);
                    thisTile.parent = currentTile;
                }
            }
        }

        // get List of tiles using current tile
        // returns reverse list btw
        if (routeAvailable) return getPath(currentTile);
        return new ArrayList<>();
    }

    private void resetAllTiles() {
        for (Tile[] tile : tiles) {
            for (int col = 0; col < tiles[0].length; ++col) {
                tile[col].open = true;
                tile[col].parent = null;
                tile[col].score = 0;
            }
        }
    }

    private List<Tile> getPath(Tile currentTile) {
        List<Tile> path = new ArrayList<>();

        while (currentTile != null) {
            path.add(currentTile);
            currentTile = currentTile.parent;
        }

        return path;
    }

    private int distanceScoreAway(Tile currentTile) {
        return Math.abs(endPosition[0] - currentTile.y) + Math.abs(endPosition[1] - currentTile.x);
    }

    private int getScoreOfTile(Tile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);

        int extraMovementCost = 0;
        if (tile.walkThrough)
            extraMovementCost = 1000;

        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost;
    }

    private boolean validTile(int nextX, int nextY) {
        if (nextX >= 0 && nextX < maxWidth)
            if (nextY >= 0 && nextY < maxHeight)
                return tiles[nextX][nextY].open && tiles[nextX][nextY].walkThrough;

        return false;
    }
}
