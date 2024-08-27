package io.github.sdftdusername.pathfinding.pathfinding;

import com.badlogic.gdx.math.Vector3;

public class Waypoint {
    public Vector3 position;
    public boolean jump;

    public Waypoint(Vector3 position, boolean jump) {
        this.position = position;
        this.jump = jump;
    }
}
