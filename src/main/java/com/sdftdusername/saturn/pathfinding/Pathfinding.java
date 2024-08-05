package com.sdftdusername.saturn.pathfinding;

import com.sdftdusername.saturn.SaturnMod;
import com.sdftdusername.saturn.Vector3i;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.*;

public class Pathfinding {
    public static final int MAX_CHUNK_SEARCH_WIDTH = 32;
    public static final int MAX_CHUNK_SEARCH_HEIGHT = 32;
    public static final int MAX_CHUNK_SEARCH_LENGTH = 32;

    private Tile currentTile;
    private Vector3i endPosition;

    private Map<Vector3i, TileMap> tileMaps = new HashMap<>();
    private Vector3i originTileMapPos = new Vector3i();

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

                SaturnMod.LOGGER.info("Generated new TileMap at ({}, {}, {})", tileMapPos.x, tileMapPos.y, tileMapPos.z);

                return map;
            }
        }

        return null;
    }

    public Vector3i tileMapPosFromBlockPos(Vector3i blockPos) {
        return new Vector3i(
                Math.floorDiv(blockPos.x, 16),
                Math.floorDiv(blockPos.y, 16),
                Math.floorDiv(blockPos.z, 16)
        );
    }

    public Tile getTile(Zone zone, Vector3i position) {
        Vector3i tileMapPos = tileMapPosFromBlockPos(position);
        TileMap tileMap = getTileMap(zone, tileMapPos);

        if (tileMap == null)
            return null;

        Vector3i localPos = new Vector3i(
                Math.floorMod(position.x, 16),
                Math.floorMod(position.y, 16),
                Math.floorMod(position.z, 16)
        );

        //SaturnMod.LOGGER.info("tile map pos: {}, {}, {}", tileMapPos.x, tileMapPos.y, tileMapPos.z);
        //SaturnMod.LOGGER.info("position: {}, {}, {}", position.x, position.y, position.z);
        //SaturnMod.LOGGER.info("local pos: {}, {}, {}", localPos.x, localPos.y, localPos.z);

        return tileMap.tiles[localPos.x][localPos.y][localPos.z];
    }

    public List<Tile> getRoute(Zone zone, Vector3i startPosition, Vector3i endPosition) {
        this.endPosition = endPosition;

        tileMaps.clear();
        originTileMapPos = tileMapPosFromBlockPos(startPosition);

        PriorityQueue<Tile> queue = new PriorityQueue<>(tileScoreComparator);
        queue.add(getTile(zone, startPosition));

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

            if (currentTile.x == endPosition.x && currentTile.y == endPosition.y && currentTile.z == endPosition.z) {
                // at the end, return path
                routeAvailable = true;
                break;
            }

            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x+=2) {
                int nextX = currentX + x;
                if (validTile(zone, nextX, currentY, currentZ)) {
                    Tile tile = getTile(zone, new Vector3i(nextX, currentY, currentZ));
                    int score = getScoreOfTile(tile, currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    tile.score = score;
                    tile.jump = false;
                    queue.add(tile);
                    tile.parent = currentTile;
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
                        if (validTile(zone, nextX, nextY, nextZ)) {
                            Tile tile = getTile(zone, new Vector3i(nextX, nextY, nextZ));
                            int score = getScoreOfTile(tile, currentScore);
                            if (score < smallestScore) {
                                smallestScore = score;
                            }
                            tile.score = score;
                            tile.jump = y == 1;
                            queue.add(tile);
                            tile.parent = currentTile;
                        }
                    }
                }
            }

            for (int z = -1; z <= 1; z+=2) {
                int nextZ = currentZ + z;
                if (validTile(zone, currentX, currentY, nextZ)) {
                    Tile tile = getTile(zone, new Vector3i(currentX, currentY, nextZ));
                    int score = getScoreOfTile(tile, currentScore);
                    if (score < smallestScore) {
                        smallestScore = score;
                    }
                    tile.score = score;
                    tile.jump = false;
                    queue.add(tile);
                    tile.parent = currentTile;
                }
            }
        }

        // get List of tiles using current tile
        // returns reverse list btw
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
        return Math.abs(endPosition.x - currentTile.x) + Math.abs(endPosition.y - currentTile.y) + Math.abs(endPosition.z - currentTile.z);
    }

    private int getScoreOfTile(Tile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);

        int extraMovementCost = 0;
        if (tile.walkThrough)
            extraMovementCost = 1000;

        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost;
    }

    private boolean validTile(Zone zone, int nextX, int nextY, int nextZ) {
        Tile nextTile = getTile(zone, new Vector3i(nextX, nextY, nextZ));
        Tile bottomTile = getTile(zone, new Vector3i(nextX, nextY - 1, nextZ));
        Tile topTile = getTile(zone, new Vector3i(nextX, nextY + 1, nextZ));

        return bottomTile != null && !bottomTile.walkThrough &&
                topTile != null && topTile.walkThrough &&
                nextTile != null && nextTile.open && nextTile.walkThrough;
    }
}
