package com.sdftdusername.pathfinding;

import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public class Vector3i {
    public int x;
    public int y;
    public int z;

    public static final Vector3i ZERO = new Vector3i();
    public static final Vector3i ONE = new Vector3i(1);

    public static final Vector3i RIGHT = new Vector3i(1, 0, 0);
    public static final Vector3i UP = new Vector3i(0, 1, 0);
    public static final Vector3i FORWARD = new Vector3i(0, 0, 1);

    private final int hashCode;

    public Vector3i() {
        x = 0;
        y = 0;
        z = 0;
        hashCode = Objects.hash(x, y, z);
    }

    public Vector3i(int x) {
        this.x = x;
        y = x;
        z = x;
        hashCode = Objects.hash(x, y, z);
    }

    public Vector3i(int x, int y) {
        this.x = x;
        this.y = y;
        z = 0;
        hashCode = Objects.hash(x, y, z);
    }

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        hashCode = Objects.hash(x, y, z);
    }

    public Vector3i(Vector3 vec3) {
        x = (int)Math.floor(vec3.x);
        y = (int)Math.floor(vec3.y);
        z = (int)Math.floor(vec3.z);
        hashCode = Objects.hash(x, y, z);
    }

    public Vector3 toVector3() {
        return new Vector3(x, y, z);
    }

    public Vector3i add(Vector3i other) {
        return new Vector3i(x + other.x, y + other.y, z + other.z);
    }

    public Vector3i subtract(Vector3i other) {
        return new Vector3i(x - other.x, y - other.y, z - other.z);
    }

    public Vector3i multiply(Vector3i other) {
        return new Vector3i(x * other.x, y * other.y, z * other.z);
    }

    public Vector3i divide(Vector3i other) {
        return new Vector3i(x / other.x, y / other.y, z / other.z);
    }

    public Vector3i multiply(int other) {
        return new Vector3i(x * other, y * other, z * other);
    }

    public Vector3i divide(int other) {
        return new Vector3i(x / other, y / other, z / other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Vector3i)
            return ((Vector3i)other).x == x && ((Vector3i)other).y == y && ((Vector3i)other).z == z;
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
