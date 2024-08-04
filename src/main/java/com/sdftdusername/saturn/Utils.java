package com.sdftdusername.saturn;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.saturn.pathfinding.Tile;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class Utils {
    public static BlockState getBlockStateAtPosition(Zone zone, Vector3 position, Vector3 offset) {
        Vector3 blockPosition = new Vector3(position);
        blockPosition.add(offset);

        blockPosition.x = MathUtils.floor(blockPosition.x);
        blockPosition.y = MathUtils.floor(blockPosition.y);
        blockPosition.z = MathUtils.floor(blockPosition.z);

        Chunk chunk = zone.getChunkAtPosition(blockPosition);
        if (chunk != null) {
            int blockX = Math.floorMod((int)blockPosition.x, 16);
            int blockY = Math.floorMod((int)blockPosition.y, 16);
            int blockZ = Math.floorMod((int)blockPosition.z, 16);

            return chunk.getBlockState(blockX, blockY, blockZ);
        }

        return null;
    }

    public static Tile[][] mapFromChunk(Chunk chunk, int localY) {
        Tile[][] map = new Tile[16][16];

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockState blockState = chunk.getBlockState(x, localY, z);
                map[x][z] = new Tile(blockState.walkThrough, x, z);
            }
        }

        return map;
    }
}
