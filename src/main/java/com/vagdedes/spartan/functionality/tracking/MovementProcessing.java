package com.vagdedes.spartan.functionality.tracking;

import com.vagdedes.spartan.compatibility.Compatibility;
import com.vagdedes.spartan.abstraction.protocol.PlayerTrackers;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.abstraction.world.SpartanBlock;
import com.vagdedes.spartan.abstraction.world.SpartanLocation;
import com.vagdedes.spartan.compatibility.manual.abilities.ItemsAdder;
import com.vagdedes.spartan.compatibility.manual.building.MythicMobs;
import com.vagdedes.spartan.compatibility.manual.vanilla.Attributes;
import com.vagdedes.spartan.functionality.server.MultiVersion;
import com.vagdedes.spartan.functionality.server.TPS;
import com.vagdedes.spartan.utils.math.AlgebraUtils;
import com.vagdedes.spartan.utils.minecraft.entity.CombatUtils;
import com.vagdedes.spartan.utils.minecraft.inventory.MaterialUtils;
import com.vagdedes.spartan.utils.minecraft.world.BlockUtils;
import com.vagdedes.spartan.utils.minecraft.world.GroundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovementProcessing {

    private static final boolean
            v1_8 = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_8);
    private static final Material
            MAGMA_BLOCK = MaterialUtils.get("magma"),
            WATER = MaterialUtils.get("water"),
            LAVA = MaterialUtils.get("lava");
    public static final int
            motionPrecision = 4,
            heightPrecision = 3;

    public static void run(SpartanProtocol protocol,
                           SpartanLocation to,
                           double vertical, double box) {
        if (!calculateLiquid(protocol, to)) {
            if (!calculateGroundCollision(protocol, vertical, box)) {
                calculateBouncing(protocol, to, vertical);
            }
        } else {
            calculateBouncing(protocol, to, vertical);
        }

        // Separator

        ServerFlying.run(protocol);
    }

    private static boolean calculateGroundCollision(SpartanProtocol protocol,
                                                    double vertical, double box) {
        if (protocol.spartan.isOnGround(true)
                && protocol.spartan.movement.getTicksOnAir() == 0
                && protocol.spartan.getVehicle() == null
                && vertical == 0.0
                && GroundUtils.collisionHeightExists(box)) {
            protocol.spartan.trackers.removeMany(PlayerTrackers.TrackerFamily.MOTION);
            protocol.spartan.trackers.removeMany(PlayerTrackers.TrackerFamily.VELOCITY);
            protocol.spartan.movement.removeLastLiquidTime();
            return true;
        } else {
            return false;
        }
    }

    private static void calculateBouncing(SpartanProtocol protocol, SpartanLocation location,
                                          double vertical) {
        if (v1_8 && vertical != 0.0) {
            if (BlockUtils.isSlime(protocol.spartan, location, 4)) {
                int time = (int) (TPS.maximum * 2);
                protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BOUNCING_BLOCKS, time);
                protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BOUNCING_BLOCKS, "slime", time);
            } else if (BlockUtils.isBed(protocol.spartan, location, 4)) {
                int time = (int) (TPS.maximum * 2);
                protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BOUNCING_BLOCKS, time);
                protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BOUNCING_BLOCKS, "bed", time);
            }
        }
    }

    // Separator

    private static boolean calculateLiquid(SpartanProtocol protocol, SpartanLocation location) {
        if (location.getBlock().isLiquidOrWaterLogged(false)) {
            protocol.spartan.movement.setLastLiquid(WATER);

            if (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)
                    && protocol.spartan.movement.isLowEyeHeight()) {
                protocol.spartan.movement.setArtificialSwimming();
            }
            calculateBubbleWater(protocol, location);
            return true;
        } else if (location.getBlock().isLiquid(LAVA)) {
            protocol.spartan.movement.setLastLiquid(LAVA);
            return true;
        } else {
            for (double i = 0.0; i < Math.ceil(protocol.bukkit.getEyeHeight()); i++) {
                for (SpartanLocation locationModified : location.getSurroundingLocations(GroundUtils.boundingBox, i, GroundUtils.boundingBox)) {
                    if (locationModified.getBlock().isLiquidOrWaterLogged(false)) {
                        protocol.spartan.movement.setLastLiquid(WATER);

                        if (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)
                                && protocol.spartan.movement.isLowEyeHeight()) {
                            protocol.spartan.movement.setArtificialSwimming();
                        }
                        calculateBubbleWater(protocol, locationModified);
                        return true;
                    } else if (locationModified.getBlock().isLiquid(LAVA)) {
                        protocol.spartan.movement.setLastLiquid(LAVA);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void calculateBubbleWater(SpartanProtocol protocol, SpartanLocation location) {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)) {
            int blockY = location.getBlockY(), minY = BlockUtils.getMinHeight(protocol.spartan.getWorld());

            if (blockY > minY) {
                SpartanLocation locationModified = location.clone();
                int max = (blockY - minY),
                        playerHeight = AlgebraUtils.integerCeil(protocol.bukkit.getEyeHeight());
                Set<Integer> emptyNonLiquid = new HashSet<>(max),
                        fullNonLiquid = new HashSet<>(max);

                for (int i = 0; i <= max; i++) {
                    Collection<SpartanLocation> locations = locationModified.clone().add(0, -i, 0).getSurroundingLocations(GroundUtils.boundingBox, 0, GroundUtils.boundingBox);

                    for (SpartanLocation loc : locations) {
                        SpartanBlock block = loc.getBlock();
                        Material type = block.getType();

                        if (type == Material.SOUL_SAND) {
                            protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BUBBLE_WATER, AlgebraUtils.integerCeil(TPS.maximum));
                            protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BUBBLE_WATER, "soul-sand", AlgebraUtils.integerCeil(TPS.maximum));
                            return;
                        } else if (type == MAGMA_BLOCK) {
                            protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BUBBLE_WATER, AlgebraUtils.integerCeil(TPS.maximum));
                            protocol.spartan.trackers.add(PlayerTrackers.TrackerType.BUBBLE_WATER, "magma-block", AlgebraUtils.integerCeil(TPS.maximum));
                            return;
                        } else if (BlockUtils.isSolid(type)) {
                            if (!block.isWaterLogged()) {
                                fullNonLiquid.add(i);

                                if (fullNonLiquid.size() == playerHeight) {
                                    return;
                                }
                            }
                        } else if (!block.isWaterLogged()) {
                            emptyNonLiquid.add(i);

                            if (emptyNonLiquid.size() == 8) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    // Separator

    public static boolean canCheck(SpartanProtocol protocol,
                                   boolean elytra,
                                   boolean flight,
                                   boolean playerAttributes,
                                   boolean environmentalAttributes) {
        if ((elytra || !protocol.spartan.movement.isGliding())
                && (flight || !protocol.spartan.movement.wasFlying())

                && (playerAttributes
                || Attributes.getAmount(protocol, Attributes.GENERIC_MOVEMENT_SPEED) == 0.0
                && Attributes.getAmount(protocol, Attributes.GENERIC_JUMP_STRENGTH) == 0.0)

                && (environmentalAttributes
                || Attributes.getAmount(protocol, Attributes.GENERIC_STEP_HEIGHT) == 0.0
                && Attributes.getAmount(protocol, Attributes.GENERIC_GRAVITY) == 0.0
                && Attributes.getAmount(protocol, Attributes.GENERIC_STEP_HEIGHT) == 0.0)) {
            if (Compatibility.CompatibilityType.MYTHIC_MOBS.isFunctional()
                    || Compatibility.CompatibilityType.ITEMS_ADDER.isFunctional()) {
                List<Entity> entities = protocol.spartan.getNearbyEntities(
                        CombatUtils.maxHitDistance,
                        CombatUtils.maxHitDistance,
                        CombatUtils.maxHitDistance
                );

                if (!entities.isEmpty()) {
                    for (Entity entity : entities) {
                        if (MythicMobs.is(entity) || ItemsAdder.is(entity)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

}
