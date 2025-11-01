/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.mob.CreeperEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreeperConsentMod implements ClientModInitializer {
    public static final String MOD_ID = "creeperconset";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Object LOCK = new Object();
    private static CreeperEntity pendingCreeper = null;
    private static UUID pendingCreeperUUID = null;
    private static final Set<UUID> processedCreepers = new HashSet<>();
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Creeper Consent Mod initialized");
    }
    
    public static boolean trySetPendingCreeper(CreeperEntity creeper) {
        synchronized (LOCK) {
            UUID creeperUUID = creeper.getUuid();
            
            if (processedCreepers.contains(creeperUUID)) {
                return false;
            }
            
            if (pendingCreeper != null && !pendingCreeper.isRemoved()) {
                return false;
            }
            
            pendingCreeper = creeper;
            pendingCreeperUUID = creeperUUID;
            processedCreepers.add(creeperUUID);
            LOGGER.info("A creeper is requesting consent");
            return true;
        }
    }
    
    public static CreeperEntity getPendingCreeper() {
        synchronized (LOCK) {
            return pendingCreeper;
        }
    }
    
    public static void clearPendingCreeper() {
        synchronized (LOCK) {
            pendingCreeper = null;
            pendingCreeperUUID = null;
        }
    }
    
    public static boolean hasPendingCreeper() {
        synchronized (LOCK) {
            return pendingCreeper != null && !pendingCreeper.isRemoved();
        }
    }
    
    public static void cleanupProcessedCreepers() {
        synchronized (LOCK) {
            if (processedCreepers.size() > 100) {
                processedCreepers.clear();
            }
        }
    }
}