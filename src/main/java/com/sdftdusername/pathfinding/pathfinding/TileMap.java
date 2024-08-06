package com.sdftdusername.pathfinding.pathfinding;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Chunk;

public class TileMap {
    public Tile[][][] tiles;

    public TileMap(Chunk chunk) {
        tiles = new Tile[16][16][16];

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockState blockState = chunk.getBlockState(x, y, z);
                    Tile tile = new Tile(blockState.walkThrough, x + chunk.blockX, y + chunk.blockY, z + chunk.blockZ);
                    tile.inFluid = blockState.getBlock().getStringId().equals(Block.WATER.getStringId());
                    tiles[x][y][z] = tile;
                }
            }
        }
    }
}
