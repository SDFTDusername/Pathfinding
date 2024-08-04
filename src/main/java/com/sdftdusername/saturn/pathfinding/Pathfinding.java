package com.sdftdusername.saturn.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Pathfinding {
    private Tile[][][] tiles;
    private Tile currentTile;
    private int[] endPosition;
    private int maxWidth;
    private int maxHeight;
    private int maxLength;

    private final TileScoreComparator tileScoreComparator = new TileScoreComparator();

    public List<Tile> getRoute(Tile[][][] tiles, int[] startPosition, int[] endPosition) {
        this.tiles = tiles;
        this.maxWidth = tiles.length;
        this.maxHeight = tiles[0].length;
        this.maxLength = tiles[0][0].length;
        this.endPosition = endPosition;

        resetAllTiles();

        PriorityQueue<Tile> queue = new PriorityQueue<>(tileScoreComparator);
        queue.add(tiles[startPosition[0]][startPosition[1]][startPosition[2]]);

        boolean routeAvailable = false;

        while (!queue.isEmpty()) {
            do {
                if (queue.isEmpty()) break;
                currentTile = queue.remove();
            } while (!currentTile.open);

            currentTile.open = false;

            int currentX = currentTile.x;
            int currentY = currentTile.y;
            int currentZ = currentTile.z;
            int currentScore = currentTile.score;

            if (currentTile.x == endPosition[0] && currentTile.y == endPosition[1] && currentTile.z == endPosition[2]) {
                // at the end, return path
                routeAvailable = true;
                break;
            }

            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x+=2) {
                int nextX = currentX + x;
                if (validTile(nextX, currentY, currentZ)) {
                    int score = getScoreOfTile(tiles[nextX][currentY][currentZ], currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[nextX][currentY][currentZ];
                    thisTile.score = score;
                    thisTile.jump = false;
                    queue.add(thisTile);
                    thisTile.parent = currentTile;
                }
            }

            for (int y = -1; y <= 1; y+=2) {
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        if (x == 0 && z == 0)
                            continue;
                        if (Math.abs(x) == Math.abs(z))
                            continue;
                        int nextX = currentX + x;
                        int nextY = currentY + y;
                        int nextZ = currentZ + z;
                        if (validTile(nextX, nextY, nextZ)) {
                            int score = getScoreOfTile(tiles[nextX][nextY][nextZ], currentScore);
                            if (score < smallestScore) {
                                smallestScore = score;
                            }
                            Tile thisTile = tiles[nextX][nextY][nextZ];
                            thisTile.score = score;
                            thisTile.jump = y == 1;
                            queue.add(thisTile);
                            thisTile.parent = currentTile;
                        }
                    }
                }
            }

            for (int z = -1; z <= 1; z+=2) {
                int nextZ = currentZ + z;
                if (validTile(currentX, currentY, nextZ)) {
                    int score = getScoreOfTile(tiles[currentX][currentY][nextZ], currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    Tile thisTile = tiles[currentX][currentY][nextZ];
                    thisTile.score = score;
                    thisTile.jump = false;
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
        for (Tile[][] tiles2 : tiles) {
            for (Tile[] tile : tiles2) {
                for (int col = 0; col < tiles[0].length; ++col) {
                    tile[col].open = true;
                    tile[col].jump = false;
                    tile[col].parent = null;
                    tile[col].score = 0;
                }
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
        return Math.abs(endPosition[0] - currentTile.x) + Math.abs(endPosition[1] - currentTile.y) + Math.abs(endPosition[2] - currentTile.z);
    }

    private int getScoreOfTile(Tile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);

        int extraMovementCost = 0;
        if (tile.walkThrough)
            extraMovementCost = 1000;

        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost;
    }

    private boolean validTile(int nextX, int nextY, int nextZ) {
        if (nextX >= 0 && nextX < maxWidth)
            if (nextY >= 0 && nextY < maxHeight)
                if (nextZ >= 0 && nextZ < maxLength) {
                    Tile nextTile = tiles[nextX][nextY][nextZ];
                    boolean bottom = true;
                    int bottomY = nextY - 1;
                    if (bottomY >= 0 && bottomY < maxHeight) {
                        Tile bottomTile = tiles[nextX][bottomY][nextZ];
                        if (bottomTile.walkThrough)
                            bottom = false;
                    }
                    return bottom && nextTile.open && nextTile.walkThrough;
                }

        return false;
    }
}
