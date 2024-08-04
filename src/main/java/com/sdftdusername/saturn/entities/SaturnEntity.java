package com.sdftdusername.saturn.entities;

import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.saturn.SaturnMod;
import com.sdftdusername.saturn.Utils;
import com.sdftdusername.saturn.commands.CommandStart;
import com.sdftdusername.saturn.mixins.EntityGetSightRange;
import com.sdftdusername.saturn.mixins.EntityModelInstanceGetBoneMap;
import com.sdftdusername.saturn.pathfinding.Pathfinding;
import com.sdftdusername.saturn.pathfinding.Tile;
import com.sdftdusername.saturn.pathfinding.Waypoint;
import de.pottgames.tuningfork.SoundBuffer;
import finalforeach.cosmicreach.GameAssetLoader;
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
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SaturnEntity extends Entity {
    public static final String ENTITY_TYPE_ID = "base:saturn_guy";
    public static final float SPEED = 3f;

    public static SoundBuffer[] yearnSounds = new SoundBuffer[8];
    public static SoundBuffer[] thankSounds = new SoundBuffer[8];

    public static String[] yearnMessages = new String[8];
    public static String[] thankMessages = new String[8];

    public String currentAnimation = "";

    public double nextTalk = 0;
    public boolean isTalking = false;

    public double talkOver = 0;
    public double playTalk = 0;
    public int talkOverState = 0;

    public Object bodyBone = null;

    public List<Waypoint> waypoints = new ArrayList<>();
    public boolean busy = false;

    @CRBSerialized
    public float targetRotationY = 0;

    @CRBSerialized
    public float bodyRotationY = 0;

    @CRBSerialized
    public float headRotationX = 0;

    @CRBSerialized
    public float headRotationY = 0;

    @CRBSerialized
    public boolean gotLog = false;

    public void playAnimation(String name) {
        if (currentAnimation.equals(name))
            return;

        if (model == null) {
            SaturnMod.LOGGER.error("model is null, can't play animation");
            return;
        }

        model.setCurrentAnimation(this, "animation.saturn." + name);
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

    public SaturnEntity() {
        super("base:entity_saturn");
        canDespawn = false;

        localBoundingBox.min.set(-0.25f, 0, -0.25f);
        localBoundingBox.max.set(0.25f,  2, 0.25f);
        localBoundingBox.update();
        getBoundingBox(this.globalBoundingBox);

        viewPositionOffset = new Vector3(0, 1.75f, 0);

        Threads.runOnMainThread(
                () -> this.model = GameSingletons.entityModelLoader
                        .load(this, "model_saturn.json", "saturn.animation.json", "animation.saturn.idle", "saturn.png")
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
                SaturnMod.LOGGER.error(e.getMessage());
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

    public void startPathfinding(Zone zone, Vector3 goal) {
        Vector3 startBlock = new Vector3(position);
        startBlock.x = (float)Math.floor(startBlock.x);
        startBlock.y = (float)Math.floor(startBlock.y);
        startBlock.z = (float)Math.floor(startBlock.z);

        Vector3 goalBlock = new Vector3(goal);
        goalBlock.x = (float)Math.floor(goalBlock.x);
        goalBlock.y = (float)Math.floor(goalBlock.y);
        goalBlock.z = (float)Math.floor(goalBlock.z);

        Chunk startChunk = zone.getChunkAtPosition(startBlock);
        Chunk goalChunk = zone.getChunkAtPosition(goalBlock);

        boolean sameChunk = startChunk.chunkX == goalChunk.chunkX &&
                startChunk.chunkY == goalChunk.chunkY &&
                startChunk.chunkZ == goalChunk.chunkZ;

        if (!sameChunk) {
            sendMessage(zone, "Start and goal position isn't in the same chunk");
            return;
        }

        Vector3 localStartBlock = new Vector3(startBlock);
        localStartBlock.x = Math.floorMod((int)localStartBlock.x, 16);
        localStartBlock.y = Math.floorMod((int)localStartBlock.y, 16);
        localStartBlock.z = Math.floorMod((int)localStartBlock.z, 16);

        Vector3 localGoalBlock = new Vector3(goalBlock);
        localGoalBlock.x = Math.floorMod((int)localGoalBlock.x, 16);
        localGoalBlock.y = Math.floorMod((int)localGoalBlock.y, 16);
        localGoalBlock.z = Math.floorMod((int)localGoalBlock.z, 16);

        Tile[][][] map = Utils.mapFromChunk(startChunk);

        int[] startPos = new int[] {(int)localStartBlock.x, (int)localStartBlock.y, (int)localStartBlock.z};
        int[] endPos = new int[] {(int)localGoalBlock.x, (int)localGoalBlock.y, (int)localGoalBlock.z};

        Pathfinding pathfinding = new Pathfinding();
        List<Tile> route = pathfinding.getRoute(map, startPos, endPos);
        sendMessage(zone, "Computing route...");

        if (route.isEmpty()) {
            sendMessage(zone, "Route is empty");
            return;
        }

        sendMessage(zone, "Found a route!");

        for (int i = 1; i < route.size(); ++i) {
            Tile tile = route.get(route.size() - i - 1);
            Vector3 worldPosition = new Vector3(
                    tile.x + startChunk.getBlockX() + 0.5f,
                    tile.y + startChunk.getBlockY(),
                    tile.z + startChunk.getBlockZ() + 0.5f
            );

            waypoints.add(new Waypoint(worldPosition, tile.jump));

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

    @Override
    public void update(Zone zone, double deltaTime) {
        if (bodyBone == null) {
            EntityModelInstance entityModelInstance = ((EntityModel) model).getModelInstance(this);
            HashMap boneMap = ((EntityModelInstanceGetBoneMap) entityModelInstance).getBoneMap();
            bodyBone = boneMap.get("body");
        }

        headRotationY = 0;
        headRotationX = 90;

        //bodyRotationY = 0;

        if (CommandStart.positionInQueue) {
            CommandStart.positionInQueue = false;
            CommandStart.busy = true;
            busy = true;

            Thread thread = new Thread(() -> {
                startPathfinding(zone, CommandStart.queuePosition);
                CommandStart.busy = false;
                busy = false;
            });

            sendMessage(zone, "Started pathfinding");
            thread.start();
        }

        if (!waypoints.isEmpty() && !busy) {
            Waypoint waypoint = waypoints.get(0);

            Vector3 currentPosition = waypoint.position;
            targetRotationY = -lookAt(position.x, position.z, currentPosition.x, currentPosition.z) + 90;

            if (waypoints.size() > 1) {
                Vector3 nextPosition = waypoints.get(1).position;
                float nextTargetRotationY = -lookAt(position.x, position.z, nextPosition.x, nextPosition.z) + 90;
                bodyRotationY = lerpRotation(bodyRotationY, nextTargetRotationY, (float)deltaTime * 5f);
            }

            if (waypoint.jump && isOnGround) {
                isOnGround = false;
                velocity.add(0, 10, 0);
            }

            move(1);
            playAnimation("follow");

            Vector3 checkPosition = new Vector3(position);

            float distance = checkPosition.dst(currentPosition);
            if (distance < 0.25f) {
                waypoints.remove(0);
                if (waypoints.isEmpty())
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

    public static float[] lookAt(float x1, float y1, float z1, float x2, float y2, float z2) {
        // Calculate the difference vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        // Compute the yaw (rotation around the y-axis)
        double yawRadians = Math.atan2(dz, dx);

        // Compute the pitch (rotation around the x-axis)
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitchRadians = Math.atan2(dy, distance);

        // Convert the angles to degrees
        double yawDegrees = Math.toDegrees(yawRadians);
        double pitchDegrees = Math.toDegrees(pitchRadians);

        return new float[] {(float)yawDegrees, (float)pitchDegrees};
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

    public Player getClosestPlayerToEntity(Zone zone) {
        Player closest = null;
        float closestDistance = Float.MAX_VALUE;

        float range = ((EntityGetSightRange)this).getSightRange();
        for (int i = 0; i < zone.players.size; ++i) {
            Player player = zone.players.get(i);
            Entity entity = player.getEntity();

            if (entity != null) {
                float distance = position.dst(entity.position);
                if (distance < closestDistance) {
                    closest = player;
                    closestDistance = distance;
                }
            }
        }

        return closest;
    }

    static {
        for (int i = 0; i < yearnSounds.length; ++i)
            yearnSounds[i] = GameAssetLoader.getSound("assets/sounds/entities/saturn/yearn/" + (i + 1) + ".ogg");

        yearnMessages[0] = "I need log.";
        yearnMessages[1] = "May I have a log?";
        yearnMessages[2] = "Log please!";
        yearnMessages[3] = "Please give me log!";
        yearnMessages[4] = "One log please!";
        yearnMessages[5] = "Give me log now!";
        yearnMessages[6] = "I am in need of a log.";
        yearnMessages[7] = "Give me log.";

        for (int i = 0; i < thankSounds.length; ++i)
            thankSounds[i] = GameAssetLoader.getSound("assets/sounds/entities/saturn/thank/" + (i + 1) + ".ogg");

        thankMessages[0] = "Thank you.";
        thankMessages[1] = "Thank you so much!";
        thankMessages[2] = "I am so thankful!";
        thankMessages[3] = "I needed that.";
        thankMessages[4] = "I am really grateful.";
        thankMessages[5] = "Yay!";
        thankMessages[6] = "Thank you for the log.";
        thankMessages[7] = "Thank you kind sir.";
    }
}
