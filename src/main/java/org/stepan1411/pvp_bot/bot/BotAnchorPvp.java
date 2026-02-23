package org.stepan1411.pvp_bot.bot;

import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Respawn Anchor PVP logic
 * Uses anchors as explosive weapons in Overworld/End
 */
public class BotAnchorPvp {
    
    // Anchor state for each bot
    private static class AnchorState {
        int step = 0;                    // Current step (0=place anchor, 1=charge, 2=detonate)
        BlockPos lastAnchorPos = null;   // Last anchor position
        long lastActionTime = 0;         // Last action time
        int cooldownTicks = 0;           // Cooldown between actions
        int stuckCounter = 0;            // Stuck counter
        int lastStep = -1;               // Last executed step
        int anchorNotFoundCounter = 0;   // "Anchor not found" counter
        int anchorPlaceFailCounter = 0;  // Failed anchor placement counter
        java.util.Set<BlockPos> triedPositions = new java.util.HashSet<>();
        int anchorPlaceAttempts = 0;     // Attempts to place anchor at current position
    }
    
    private static final java.util.Map<String, AnchorState> states = new java.util.HashMap<>();
    
    /**
     * Get bot state
     */
    private static AnchorState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new AnchorState());
    }
    
    /**
     * Check if bot can use Anchor PVP
     */
    public static boolean canUseAnchorPvp(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isAnchorPvpEnabled()) return false;
        
        // Check dimension - anchors explode in Overworld and End
        String dimension = bot.getEntityWorld().getRegistryKey().getValue().toString();
        if (dimension.contains("nether")) return false; // Don't use in Nether
        
        double distance = bot.distanceTo(target);
        if (distance < 2.0 || distance > 8.0) return false; // Increased max distance to 8.0
        
        // Check bot health - don't use anchor if low HP (suicide risk)
        double healthPercent = bot.getHealth() / bot.getMaxHealth();
        if (healthPercent < 0.4) return false; // Don't use if HP < 40%
        
        PlayerInventory inventory = bot.getInventory();
        return hasRespawnAnchor(inventory) && hasGlowstone(inventory);
    }
    
    /**
     * Main method - executes Anchor PVP
     * Returns true if bot is busy with Anchor PVP (no need for regular combat)
     */
    public static boolean doAnchorPvp(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        AnchorState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        
        // Stuck check: if bot is on same step too long - reset
        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) { // 5 seconds (100 ticks)
                System.out.println("[Anchor PVP] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                state.step = 0;
                state.lastAnchorPos = null;
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        
        // Cooldown between actions
        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // If enemy too far - approach
        if (distance > 8.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            state.step = 0;
            state.lastAnchorPos = null;
            return true;
        }
        
        // If enemy too close - retreat
        if (distance < 2.0) {
            moveAway(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Execute current step
        switch (state.step) {
            case 0: // Step 1: Place anchor
                return stepPlaceAnchor(bot, target, state, server, world, settings);
                
            case 1: // Step 2: Charge anchor
                return stepChargeAnchor(bot, target, state, server, world, settings);
                
            case 2: // Step 3: Detonate anchor
                return stepDetonateAnchor(bot, target, state, server, world, settings, distance);
                
            default:
                state.step = 0;
                return true;
        }
    }
    
    /**
     * Step 0: Place anchor
     */
    private static boolean stepPlaceAnchor(ServerPlayerEntity bot, Entity target, AnchorState state,
                                          net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Check for existing anchor near enemy
        BlockPos existingAnchor = findExistingAnchor(bot, target, world, 5.0);
        if (existingAnchor != null) {
            double distToExisting = Math.sqrt(bot.squaredDistanceTo(existingAnchor.getX() + 0.5, existingAnchor.getY() + 0.5, existingAnchor.getZ() + 0.5));
            
            if (distToExisting <= 4.0) {
                System.out.println("[Anchor PVP] " + bot.getName().getString() + " using existing anchor at " + existingAnchor);
                state.lastAnchorPos = existingAnchor;
                state.step = 1; // Go to charging
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                return true;
            } else {
                System.out.println("[Anchor PVP] " + bot.getName().getString() + " approaching existing anchor, distance: " + String.format("%.2f", distToExisting));
                moveToward(bot, target, settings.getMoveSpeed());
                return true;
            }
        }
        
        // No anchor nearby - place new one
        
        // Check for anchor in inventory
        int anchorSlot = findRespawnAnchor(inventory);
        if (anchorSlot < 0) {
            return false; // No anchor - exit Anchor PVP
        }
        
        // Find position for anchor
        BlockPos anchorPos = findBestAnchorPosition(bot, target, world, state.triedPositions);
        if (anchorPos == null) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " could not find anchor position!");
            if (!state.triedPositions.isEmpty()) {
                System.out.println("[Anchor PVP] " + bot.getName().getString() + " clearing tried positions (" + state.triedPositions.size() + " positions)");
                state.triedPositions.clear();
            }
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Check distance to position
        double distToPos = Math.sqrt(bot.squaredDistanceTo(anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5));
        
        if (distToPos > 3.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Stop bot
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Switch to anchor
        if (!selectItem(bot, anchorSlot)) {
            return true;
        }
        
        // Look at position
        lookAt(bot, anchorPos);
        
        // Increase attempt counter for this position
        state.anchorPlaceAttempts++;
        
        // If 3 attempts at one position failed - try different position
        if (state.anchorPlaceAttempts >= 3) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " failed to place anchor 3 times at " + anchorPos + ", trying different position");
            state.triedPositions.add(anchorPos);
            state.anchorPlaceAttempts = 0;
            state.cooldownTicks = 3;
            return true;
        }
        
        // Place block via Carpet command
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once",
                server.getCommandSource()
            );
            
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " placed anchor at " + anchorPos + " (attempt " + state.anchorPlaceAttempts + "/3)");
            
            state.lastAnchorPos = anchorPos;
            state.step = 1;
            state.cooldownTicks = 3; // Reduced cooldown for faster action
            state.stuckCounter = 0;
            state.anchorPlaceAttempts = 0;
            
        } catch (Exception e) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " error placing anchor: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * Step 1: Charge anchor with glowstone
     */
    private static boolean stepChargeAnchor(ServerPlayerEntity bot, Entity target, AnchorState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Check that anchor is placed
        if (state.lastAnchorPos == null) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " no anchor position!");
            state.step = 0;
            return true;
        }
        
        // Check that anchor is actually there
        BlockState blockAtPos = world.getBlockState(state.lastAnchorPos);
        
        if (!(blockAtPos.getBlock() instanceof RespawnAnchorBlock)) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " NO anchor at position! Returning to step 0");
            if (state.lastAnchorPos != null) {
                state.triedPositions.add(state.lastAnchorPos);
            }
            state.step = 0;
            state.lastAnchorPos = null;
            state.anchorPlaceAttempts = 0;
            return true;
        }
        
        // Check anchor charge level
        int charges = blockAtPos.get(RespawnAnchorBlock.CHARGES);
        
        if (charges >= 1) {
            // Charged (even 1 charge is enough) - go to detonation
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " anchor charged (" + charges + "/4), moving to step 2");
            state.step = 2;
            state.cooldownTicks = 0;
            return true;
        }
        
        // Check for glowstone
        int glowstoneSlot = findGlowstone(inventory);
        if (glowstoneSlot < 0) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " no glowstone in inventory! Exiting Anchor PVP.");
            state.step = 0;
            state.lastAnchorPos = null;
            state.stuckCounter = 0;
            return false;
        }
        
        // Check fail counter BEFORE charging
        if (state.anchorPlaceFailCounter >= 5) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " STUCK - anchor charging failed 5 times! Resetting state.");
            state.step = 0;
            state.lastAnchorPos = null;
            state.cooldownTicks = 10;
            state.anchorPlaceFailCounter = 0;
            state.triedPositions.clear();
            return true;
        }
        
        // Stop bot
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Switch to glowstone
        if (!selectItem(bot, glowstoneSlot)) {
            return true;
        }
        
        // Look at anchor
        lookAt(bot, state.lastAnchorPos);
        
        // Charge anchor via Carpet command
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once",
                server.getCommandSource()
            );
            
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " charged anchor once (charges: " + (charges + 1) + "/4)");
            
            // One charge is enough - go to detonation immediately
            state.step = 2;
            state.cooldownTicks = 2; // Short cooldown before detonation
            state.stuckCounter = 0;
            
        } catch (Exception e) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " error charging anchor: " + e.getMessage());
            state.step = 0;
            state.lastAnchorPos = null;
            state.anchorPlaceFailCounter = 0;
        }
        
        return true;
    }
    
    /**
     * Step 2: Detonate anchor
     */
    private static boolean stepDetonateAnchor(ServerPlayerEntity bot, Entity target, AnchorState state,
                                             net.minecraft.server.MinecraftServer server, World world,
                                             BotSettings settings, double distance) {
        
        // Check that anchor is placed
        if (state.lastAnchorPos == null) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " no anchor position!");
            state.step = 0;
            return true;
        }
        
        // Check that anchor is actually there
        BlockState blockAtPos = world.getBlockState(state.lastAnchorPos);
        
        if (!(blockAtPos.getBlock() instanceof RespawnAnchorBlock)) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " NO anchor at position! Returning to step 0");
            state.anchorNotFoundCounter++;
            
            if (state.anchorNotFoundCounter >= 3) {
                System.out.println("[Anchor PVP] " + bot.getName().getString() + " STUCK - anchor not found 3 times! Resetting state.");
                state.step = 0;
                state.lastAnchorPos = null;
                state.cooldownTicks = 10;
                state.anchorNotFoundCounter = 0;
                state.anchorPlaceFailCounter = 0;
                state.triedPositions.clear();
                return true;
            }
            
            state.step = 0;
            state.lastAnchorPos = null;
            return true;
        }
        
        // Check anchor charge level
        int charges = blockAtPos.get(RespawnAnchorBlock.CHARGES);
        
        if (charges == 0) {
            // Not charged - go back to charging
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " anchor not charged, returning to step 1");
            state.step = 1;
            state.cooldownTicks = 0;
            return true;
        }
        
        // Calculate distance from bot to anchor
        double distToAnchor = Math.sqrt(bot.squaredDistanceTo(
            state.lastAnchorPos.getX() + 0.5,
            state.lastAnchorPos.getY() + 0.5,
            state.lastAnchorPos.getZ() + 0.5
        ));
        
        // Calculate distance from target to anchor
        double targetDistToAnchor = Math.sqrt(target.squaredDistanceTo(
            state.lastAnchorPos.getX() + 0.5,
            state.lastAnchorPos.getY() + 0.5,
            state.lastAnchorPos.getZ() + 0.5
        ));
        
        // Safety check: don't detonate if bot is too close (suicide risk)
        if (distToAnchor < 3.0) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " too close to anchor (" + String.format("%.2f", distToAnchor) + "), retreating");
            moveAway(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Check if target is close enough to anchor
        if (targetDistToAnchor > 5.0) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " target too far from anchor (" + String.format("%.2f", targetDistToAnchor) + "), waiting");
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Reset anchor not found counter (anchor exists!)
        state.anchorNotFoundCounter = 0;
        state.anchorPlaceFailCounter = 0;
        
        // Stop bot
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Switch to anchor (more reliable - can place immediately after detonation)
        PlayerInventory inventory = bot.getInventory();
        int anchorSlot = findRespawnAnchor(inventory);
        if (anchorSlot >= 0) {
            selectItem(bot, anchorSlot);
        } else {
            // Fallback to weapon if no anchor
            int weaponSlot = findMeleeWeapon(inventory);
            if (weaponSlot >= 0) {
                selectItem(bot, weaponSlot);
            }
        }
        
        // Look at anchor
        lookAt(bot, state.lastAnchorPos);
        
        // Detonate anchor via Carpet command (right-click on charged anchor in Overworld/End = explosion)
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once",
                server.getCommandSource()
            );
            
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " DETONATED anchor! (charges: " + charges + ")");
            
            // After detonation - reset and start new cycle
            state.step = 0;
            state.lastAnchorPos = null;
            state.cooldownTicks = 5; // Reduced cooldown for faster spam
            state.stuckCounter = 0;
            
        } catch (Exception e) {
            System.out.println("[Anchor PVP] " + bot.getName().getString() + " error detonating anchor: " + e.getMessage());
            state.step = 0;
            state.lastAnchorPos = null;
        }
        
        return true;
    }
    
    /**
     * Maintain optimal distance (3-5 blocks)
     */
    private static void maintainDistance(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        double distance = bot.distanceTo(target);
        
        if (distance < 3.0) {
            moveAway(bot, target, settings.getMoveSpeed() * 0.7);
        } else if (distance > 5.5) {
            moveToward(bot, target, settings.getMoveSpeed() * 0.7);
        } else {
            lookAtEntity(bot, target);
        }
    }
    
    /**
     * Move toward target
     */
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = targetPos.subtract(botPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * Move away from target
     */
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = botPos.subtract(targetPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * Look at position
     */
    private static void lookAt(ServerPlayerEntity bot, BlockPos pos) {
        Vec3d target = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3d botPos = bot.getEyePos();
        
        double dx = target.x - botPos.x;
        double dy = target.y - botPos.y;
        double dz = target.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Look at entity
     */
    private static void lookAtEntity(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Select item in hand
     */
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        PlayerInventory inventory = bot.getInventory();
        
        // If item in inventory (not in hotbar) - move it
        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, item);
            slot = 0;
        }
        
        // Switch slot
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlotAccessor(slot);
        return true;
    }
    
    /**
     * Find existing anchor near enemy
     */
    private static BlockPos findExistingAnchor(ServerPlayerEntity bot, Entity target, World world, double maxDistance) {
        BlockPos targetPos = target.getBlockPos();
        
        int radius = (int) Math.ceil(maxDistance);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = targetPos.add(dx, dy, dz);
                    
                    double distFromTarget = Math.sqrt(target.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromTarget > maxDistance) continue;
                    
                    BlockState blockState = world.getBlockState(pos);
                    if (!(blockState.getBlock() instanceof RespawnAnchorBlock)) continue;
                    
                    double distFromBot = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromBot > 4.0) continue;
                    
                    System.out.println("[Anchor PVP] Found suitable anchor at " + pos + ", distance from enemy: " + String.format("%.2f", distFromTarget) + ", from bot: " + String.format("%.2f", distFromBot));
                    return pos;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find best position for anchor - NEAR enemy
     */
    private static BlockPos findBestAnchorPosition(ServerPlayerEntity bot, Entity target, World world, java.util.Set<BlockPos> triedPositions) {
        BlockPos targetPos = target.getBlockPos();
        
        BlockPos[] candidates = {
            targetPos.north(),
            targetPos.south(),
            targetPos.east(),
            targetPos.west(),
            targetPos.north().east(),
            targetPos.north().west(),
            targetPos.south().east(),
            targetPos.south().west(),
        };
        
        for (BlockPos pos : candidates) {
            if (triedPositions.contains(pos)) continue;
            
            if (!world.getBlockState(pos).isAir() && !world.getBlockState(pos).isReplaceable()) continue;
            
            double dist = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            
            if (dist <= 4.0) {
                System.out.println("[Anchor PVP] Found suitable position: " + pos);
                return pos;
            }
        }
        
        System.out.println("[Anchor PVP] No suitable positions found!");
        return null;
    }
    
    /**
     * Find respawn anchor in inventory
     */
    private static int findRespawnAnchor(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.RESPAWN_ANCHOR) return i;
        }
        return -1;
    }
    
    /**
     * Find glowstone in inventory
     */
    private static int findGlowstone(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.GLOWSTONE) return i;
        }
        return -1;
    }
    
    /**
     * Find melee weapon
     */
    private static int findMeleeWeapon(PlayerInventory inventory) {
        int bestSlot = -1;
        int bestPriority = -1;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            int priority = -1;
            
            if (stack.getItem().toString().contains("sword")) {
                priority = 10;
            } else if (stack.getItem().toString().contains("axe")) {
                priority = 5;
            }
            
            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Check for respawn anchor
     */
    private static boolean hasRespawnAnchor(PlayerInventory inventory) {
        return findRespawnAnchor(inventory) >= 0;
    }
    
    /**
     * Check for glowstone
     */
    private static boolean hasGlowstone(PlayerInventory inventory) {
        return findGlowstone(inventory) >= 0;
    }
    
    /**
     * Reset bot state
     */
    public static void reset(String botName) {
        states.remove(botName);
    }
}
