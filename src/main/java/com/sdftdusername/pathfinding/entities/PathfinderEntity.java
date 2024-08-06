package com.sdftdusername.pathfinding.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.pathfinding.PathfindingMod;
import com.sdftdusername.pathfinding.Vector3i;
import com.sdftdusername.pathfinding.commands.CommandFollow;
import com.sdftdusername.pathfinding.commands.CommandStart;
import com.sdftdusername.pathfinding.commands.CommandStop;
import com.sdftdusername.pathfinding.commands.CommandStopFollow;
import com.sdftdusername.pathfinding.mixins.EntityModelInstanceGetBoneMap;
import com.sdftdusername.pathfinding.pathfinding.Pathfinding;
import com.sdftdusername.pathfinding.pathfinding.Tile;
import com.sdftdusername.pathfinding.pathfinding.Waypoint;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.world.Zone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathfinderEntity extends Entity {
    public static final String ENTITY_TYPE_ID = "base:entity_pathfinder";
    public static final float SPEED = 3f;

    public String currentAnimation = "";
    public Object bodyBone = null;

    public Thread pathfindingThread;

    public List<Waypoint> waypoints = new ArrayList<>();
    public boolean busy = false;

    public boolean isFollowing = false;
    public Vector3 targetPosition;
    public double updateFollowTime = 0;

    @CRBSerialized
    public float targetRotationY = 0;

    @CRBSerialized
    public float bodyRotationY = 0;

    @CRBSerialized
    public float headRotationX = 0;

    @CRBSerialized
    public float headRotationY = 0;

    public void playAnimation(String name) {
        if (currentAnimation.equals(name))
            return;

        if (model == null) {
            PathfindingMod.LOGGER.error("model is null, can't play animation");
            return;
        }

        model.setCurrentAnimation(this, "animation.pathfinder." + name);
        currentAnimation = name;
    }

    public void sendMessage(Zone zone, String message) {
        for (int i = 0; i < zone.players.size; ++i) {
            Player player = zone.players.get(i);
            Chat.MAIN_CHAT.sendMessage(zone.getWorld(), player, null, message);
        }
    }

    public void move(float multiply) {
        double yaw = Math.toRadians(targetRotationY);
        Vector3 forward = new Vector3(
                (float)Math.sin(yaw),
                0,
                (float)Math.cos(yaw)
        );

        float force = SPEED * multiply;
        this.onceVelocity.add(forward.scl(force));
    }

    public void stopMoving() {
        onceVelocity.setZero();
    }

    public PathfinderEntity() {
        super(ENTITY_TYPE_ID);
        canDespawn = false;

        localBoundingBox.min.set(-0.25f, 0, -0.25f);
        localBoundingBox.max.set(0.25f,  1.9f, 0.25f);
        localBoundingBox.update();
        getBoundingBox(this.globalBoundingBox);

        viewPositionOffset = new Vector3(0, 1.75f, 0);

        Threads.runOnMainThread(
                () -> this.model = GameSingletons.entityModelLoader
                        .load(this, "model_pathfinder.json", "pathfinder.animation.json", "animation.pathfinder.idle", "pathfinder.png")
        );

        currentAnimation = "idle";
    }

    public void setBodyRotation(Vector3 rotation) {
        if (bodyBone != null) {
            try {
                Class myClass = bodyBone.getClass();
                Field rotationField = myClass.getField("rotation");
                rotationField.set(bodyBone, rotation);
            } catch (Exception e) {
                PathfindingMod.LOGGER.error(e.getMessage());
            }
        }
    }

    public void wrapRotation() {
        while (headRotationX > 180)
            headRotationX -= 360;
        while (headRotationX < -180)
            headRotationX += 360;

        while (headRotationY > 180)
            headRotationY -= 360;
        while (headRotationY < -180)
            headRotationY += 360;

        while (bodyRotationY > 180)
            bodyRotationY -= 360;
        while (bodyRotationY < -180)
            bodyRotationY += 360;
    }

    public float shortestAngleDifference(float a, float b) {
        float diff = (b - a) % 360;
        if (diff > 180)
            diff -= 360;
        else if (diff < -180)
            diff += 360;
        return diff;
    }

    public float lerpRotation(float a, float b, float t) {
        float diff = shortestAngleDifference(a, b);
        return (a + diff * t + 360) % 360;
    }

    public void startPathfinding(Zone zone, Vector3 goal, boolean follow) {
        if (!follow)
            sendMessage(zone, "Computing route...");

        long start = System.currentTimeMillis();
        Pathfinding pathfinding = new Pathfinding();
        List<Tile> route = pathfinding.getRoute(zone, new Vector3i(position), new Vector3i(goal));
        long end = System.currentTimeMillis();

        if (Thread.currentThread().isInterrupted()) {
            if (!follow)
                sendMessage(zone, "Pathfinding has stopped");
        } else {
            if (route.isEmpty()) {
                if (!follow)
                    sendMessage(zone, "Route is empty");

                return;
            }

            if (!follow) {
                long duration = end - start;
                sendMessage(zone, "Found a route! Took " + duration + "ms (" + (duration / 1000f) + "s)");
            }

            if (follow || CommandStart.moveToWaypoints)
                waypoints.clear();

            for (int i = 1; i < route.size(); ++i) {
                Tile tile = route.get(route.size() - i - 1);
                Vector3 worldPosition = new Vector3(
                        tile.x + 0.5f,
                        tile.y,
                        tile.z + 0.5f
                );

                if (follow || CommandStart.moveToWaypoints)
                    waypoints.add(new Waypoint(worldPosition, tile.jump));

                if (!follow && CommandStart.spawnWaypointItems) {
                    Block block = Block.STONE_BASALT;
                    if (i == 1)
                        block = Block.GRASS;
                    else if (i == route.size() - 1)
                        block = Block.SAND;
                    else if (tile.jump)
                        block = Block.CRATE_WOOD;

                    ItemStack itemStack = new ItemStack(block.getDefaultBlockState().getItem(), 1);

                    ItemEntity itemEntity = new ItemEntity(itemStack);
                    itemEntity.setPosition(worldPosition);
                    itemEntity.minPickupAge = 5.0F;
                    zone.addEntity(itemEntity);
                }
            }
        }
    }

    public void updateFollow(Zone zone) {
        if (pathfindingThread != null && pathfindingThread.isAlive())
            pathfindingThread.interrupt();

        pathfindingThread = new Thread(() -> {
            startPathfinding(zone, targetPosition, true);
        });

        pathfindingThread.start();
    }

    public void stopFollow(Zone zone) {
        pathfindingThread.interrupt();
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        if (bodyBone == null) {
            EntityModelInstance entityModelInstance = ((EntityModel) model).getModelInstance(this);
            HashMap boneMap = ((EntityModelInstanceGetBoneMap) entityModelInstance).getBoneMap();
            bodyBone = boneMap.get("body");
        }

        headRotationY = 0;
        headRotationX = 90;

        if (CommandStop.stop) {
            if (busy) {
                sendMessage(zone, "Stopping pathfinding");
                pathfindingThread.interrupt();
            } else if (!waypoints.isEmpty()) {
                sendMessage(zone, "Stopping pathfinding");
                waypoints.clear();
            } else {
                sendMessage(zone, "Nothing to stop");
            }

            CommandStop.stop = false;
        }

        if (CommandStart.positionInQueue) {
            CommandStart.positionInQueue = false;
            CommandStart.busy = true;
            busy = true;

            pathfindingThread = new Thread(() -> {
                startPathfinding(zone, CommandStart.queuePosition, false);
                CommandStart.busy = false;
                busy = false;
            });

            sendMessage(zone, "Started pathfinding");
            pathfindingThread.start();
        }

        if (CommandStopFollow.stop) {
            CommandFollow.follow = false;
            CommandStopFollow.stop = false;
            sendMessage(zone, "Stopped following");
            stopFollow(zone);
        }

        if (CommandFollow.follow) {
            if (!isFollowing) {
                updateFollowTime = 1;
                isFollowing = true;

                targetPosition = new Vector3(CommandFollow.target.getPosition());
                updateFollow(zone);

                sendMessage(zone, "Started following");
            } else {
                updateFollowTime -= deltaTime;
                boolean updateTarget = false;

                Vector3 newTargetPosition = CommandFollow.target.getPosition();
                if (targetPosition.dst(newTargetPosition) > 1f)
                    updateTarget = true;

                if (updateFollowTime <= 0 && targetPosition.equals(newTargetPosition))
                    updateTarget = true;

                if (updateTarget) {
                    updateFollowTime = 1;

                    targetPosition = new Vector3(newTargetPosition);
                    updateFollow(zone);
                }
            }
        }

        if (!waypoints.isEmpty() && (!busy || isFollowing)) {
            Waypoint waypoint = waypoints.get(0);

            Vector3 currentPosition = waypoint.position;
            targetRotationY = -lookAt(position.x, position.z, currentPosition.x, currentPosition.z) + 90;

            Vector3 nextPosition = waypoints.get((waypoints.size() > 1) ? 1 : 0).position;
            float nextTargetRotationY = -lookAt(position.x, position.z, nextPosition.x, nextPosition.z) + 90;
            bodyRotationY = lerpRotation(bodyRotationY, nextTargetRotationY, (float)deltaTime * 5f);

            if (waypoint.jump && isOnGround) {
                isOnGround = false;
                velocity.add(0, 10, 0);
            }

            Vector2 checkPosition2D = new Vector2(position.x, position.z);
            Vector2 currentPosition2D = new Vector2(currentPosition.x, currentPosition.z);
            float distance2D = checkPosition2D.dst(currentPosition2D);

            if (distance2D > 0.1f)
                move(1);
            else
                stopMoving();
            playAnimation("walk");

            Vector3 checkPosition = new Vector3(position);
            float distance = checkPosition.dst(currentPosition);

            if (distance < 0.25f) {
                waypoints.remove(0);
                if (!isFollowing && waypoints.isEmpty())
                    sendMessage(zone, "Done!");
            }
        } else {
            stopMoving();
            playAnimation("idle");
        }

        wrapRotation();

        setBodyRotation(new Vector3(0, bodyRotationY, 0));
        viewDirection = convertToDirectionVector(headRotationX, headRotationY);

        super.update(zone, deltaTime);
    }

    public static float lookAt(float x1, float y1, float x2, float y2) {
        // Calculate the difference vector
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Compute the angle using atan2
        double angleRadians = Math.atan2(dy, dx);

        // Convert the angle to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        return (float)angleDegrees;
    }

    public Vector3 convertToDirectionVector(float yawDegrees, float pitchDegrees) {
        double yawRadians = Math.toRadians(yawDegrees);
        double pitchRadians = Math.toRadians(pitchDegrees);

        double x = Math.cos(pitchRadians) * Math.cos(yawRadians);
        double y = Math.sin(pitchRadians);
        double z = Math.cos(pitchRadians) * Math.sin(yawRadians);

        // Normalize the vector
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        return new Vector3((float)(x / magnitude), (float)(y / magnitude), (float)(z / magnitude));
    }
}
