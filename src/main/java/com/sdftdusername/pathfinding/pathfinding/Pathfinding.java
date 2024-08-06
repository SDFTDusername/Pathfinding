package com.sdftdusername.pathfinding.pathfinding;

import com.sdftdusername.pathfinding.Vector3i;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.*;

public class Pathfinding {
    public static final int MAX_CHUNK_SEARCH_WIDTH  = 32;
    public static final int MAX_CHUNK_SEARCH_HEIGHT = 32;
    public static final int MAX_CHUNK_SEARCH_LENGTH = 32;

    private Tile currentTile;
    private Vector3i endPosition;

    private final Map<Vector3i, TileMap> tileMaps = new HashMap<>();
    private Vector3i originTileMapPos;

    private final TileScoreComparator tileScoreComparator = new TileScoreComparator();

    public TileMap getTileMap(Zone zone, Vector3i tileMapPos) {
        if (tileMaps.containsKey(tileMapPos))
            return tileMaps.get(tileMapPos);

        Vector3i relativeTileMapPos = tileMapPos.subtract(originTileMapPos);

        if (
            relativeTileMapPos.x >= Math.floor(-MAX_CHUNK_SEARCH_WIDTH / 2f) &&
            relativeTileMapPos.x <= Math.ceil(MAX_CHUNK_SEARCH_WIDTH / 2f) &&
            relativeTileMapPos.y >= Math.floor(-MAX_CHUNK_SEARCH_HEIGHT / 2f) &&
            relativeTileMapPos.y <= Math.ceil(MAX_CHUNK_SEARCH_HEIGHT / 2f) &&
            relativeTileMapPos.z >= Math.floor(-MAX_CHUNK_SEARCH_LENGTH / 2f) &&
            relativeTileMapPos.z <= Math.ceil(MAX_CHUNK_SEARCH_LENGTH / 2f)) {

            Chunk chunk = zone.getChunkAtChunkCoords(tileMapPos.x, tileMapPos.y, tileMapPos.z);
            if (chunk != null) {
                TileMap map = new TileMap(chunk);
                tileMaps.put(tileMapPos, map);

                return map;
            }
        }

        return null;
    }

    public Vector3i tileMapPosFromBlockPos(int x, int y, int z) {
        return new Vector3i(
                Math.floorDiv(x, 16),
                Math.floorDiv(y, 16),
                Math.floorDiv(z, 16)
        );
    }

    public Tile getTile(Zone zone, int x, int y, int z) {
        Vector3i tileMapPos = tileMapPosFromBlockPos(x, y, z);
        TileMap tileMap = getTileMap(zone, tileMapPos);

        if (tileMap == null)
            return null;

        int localX = Math.floorMod(x, 16);
        int localY = Math.floorMod(y, 16);
        int localZ = Math.floorMod(z, 16);

        return tileMap.tiles[localX][localY][localZ];
    }

    public List<Tile> getRoute(Zone zone, Vector3i startPosition, Vector3i endPosition) {
        this.endPosition = endPosition;

        tileMaps.clear();
        originTileMapPos = tileMapPosFromBlockPos(startPosition.x, startPosition.y, startPosition.z);

        PriorityQueue<Tile> queue = new PriorityQueue<>(tileScoreComparator);
        queue.add(getTile(zone, startPosition.x, startPosition.y, startPosition.z));

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

            if (currentX == endPosition.x && currentY == endPosition.y && currentZ == endPosition.z) {
                // at the end, return path
                routeAvailable = true;
                break;
            }

            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x+=2) {
                int nextX = currentX + x;

                Tile tile = getTile(zone, nextX, currentY, currentZ);
                if (validTile(zone, tile)) {
                    tile.jump = false;
                    tile.fall = false;

                    int score = getScoreOfTile(tile, currentScore);
                    if (score < smallestScore)
                        smallestScore = score;

                    tile.score = score;
                    tile.parent = currentTile;
                    queue.add(tile);
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

                        Tile tile = getTile(zone, nextX, nextY, nextZ);
                        if (validTile(zone, tile)) {
                            Tile ceilingTile = null;
                            
                            if (y == 1)
                                ceilingTile = getTile(zone, currentX, currentY + 2, currentZ);
                            else if (y == -1)
                                ceilingTile = getTile(zone, nextX, currentY + 1, nextZ);
                            
                            if (ceilingTile != null && ceilingTile.walkThrough) {
                                tile.jump = y == 1;
                                tile.fall = y == -1;

                                int score = getScoreOfTile(tile, currentScore);
                                if (score < smallestScore)
                                    smallestScore = score;

                                tile.score = score;
                                tile.parent = currentTile;
                                queue.add(tile);
                            }
                        }
                    }
                }
            }

            for (int z = -1; z <= 1; z+=2) {
                int nextZ = currentZ + z;

                Tile tile = getTile(zone, currentX, currentY, nextZ);
                if (validTile(zone, tile)) {
                    tile.jump = false;
                    tile.fall = false;

                    int score = getScoreOfTile(tile, currentScore);
                    if (score < smallestScore)
                        smallestScore = score;

                    tile.score = score;
                    tile.parent = currentTile;
                    queue.add(tile);
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                return new ArrayList<>();
            }
        }

        if (routeAvailable) return getPath(currentTile);
        return new ArrayList<>();
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
        return  Math.abs(endPosition.x - currentTile.x) +
                Math.abs(endPosition.y - currentTile.y) +
                Math.abs(endPosition.z - currentTile.z);
    }

    private int getScoreOfTile(Tile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);

        int extraMovementCost = 1;
        if (tile.fall)    extraMovementCost += 5;
        if (tile.inFluid) extraMovementCost += 10;
        if (tile.jump)    extraMovementCost += 20;

        return guessScoreLeft + currentScore + extraMovementCost;
    }

    private boolean validTile(Zone zone, Tile tile) {
        if (tile == null)
            return false;

        Tile topTile    = getTile(zone, tile.x, tile.y + 1, tile.z);
        Tile bottomTile = getTile(zone, tile.x, tile.y - 1, tile.z);

        return  topTile    != null &&  topTile.walkThrough &&
                bottomTile != null && !bottomTile.walkThrough &&
                                       tile.walkThrough && tile.open;
    }
}
