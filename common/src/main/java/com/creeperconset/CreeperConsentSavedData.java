/*
 * Copyright (c) 2025 Viktor Milivojević
 * CreeperConsent is licensed under Mulan PubL v2.
 * You can use this software according to the terms and conditions of the Mulan PubL v2.
 * You may obtain a copy of Mulan PubL v2 at:
 *     http://license.coscl.org.cn/MulanPubL-2.0
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PubL v2 for more details.
 */

package com.creeperconset;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreeperConsentSavedData extends SavedData {

    final Map<UUID, Long> deniedCreepers = new HashMap<>();
    final Map<UUID, Long> fleeCreepers = new HashMap<>();
    final Set<UUID> friendlyCreepers = new HashSet<>();

    /**
     * Minecraft 1.21.1 SavedData uses a SavedData.Factory consisting of a
     * constructor supplier and an NBT loader. The file name is supplied to
     * DimensionDataStorage#computeIfAbsent from the platform module.
     */
    public static final SavedData.Factory<CreeperConsentSavedData> FACTORY =
            new SavedData.Factory<>(
                    CreeperConsentSavedData::create,
                    CreeperConsentSavedData::load
            );

    public static CreeperConsentSavedData create() {
        return new CreeperConsentSavedData();
    }

    public static CreeperConsentSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CreeperConsentSavedData data = create();

        CompoundTag deniedTag = tag.getCompound("DeniedCreepers");
        for (String key : deniedTag.getAllKeys()) {
            try {
                data.deniedCreepers.put(UUID.fromString(key), deniedTag.getLong(key));
            } catch (IllegalArgumentException ignored) {
                CreeperConsentState.LOGGER.warn("Ignoring invalid denied creeper UUID in saved data: {}", key);
            }
        }

        CompoundTag fleeTag = tag.getCompound("FleeCreepers");
        for (String key : fleeTag.getAllKeys()) {
            try {
                data.fleeCreepers.put(UUID.fromString(key), fleeTag.getLong(key));
            } catch (IllegalArgumentException ignored) {
                CreeperConsentState.LOGGER.warn("Ignoring invalid fleeing creeper UUID in saved data: {}", key);
            }
        }

        ListTag friendlyTag = tag.getList("FriendlyCreepers", Tag.TAG_STRING);
        for (int i = 0; i < friendlyTag.size(); i++) {
            String value = friendlyTag.getString(i);
            try {
                data.friendlyCreepers.add(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
                CreeperConsentState.LOGGER.warn("Ignoring invalid friendly creeper UUID in saved data: {}", value);
            }
        }

        CreeperConsentState.LOGGER.info(
                "Loaded consent data: {} denied, {} friendly",
                data.deniedCreepers.size(),
                data.friendlyCreepers.size()
        );
        return data;
    }

    private CreeperConsentSavedData() {
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CompoundTag deniedTag = new CompoundTag();
        deniedCreepers.forEach((uuid, expiry) -> deniedTag.putLong(uuid.toString(), expiry));
        tag.put("DeniedCreepers", deniedTag);

        CompoundTag fleeTag = new CompoundTag();
        fleeCreepers.forEach((uuid, expiry) -> fleeTag.putLong(uuid.toString(), expiry));
        tag.put("FleeCreepers", fleeTag);

        ListTag friendlyTag = new ListTag();
        friendlyCreepers.stream()
                .map(UUID::toString)
                .forEach(friendlyTag::addString);
        tag.put("FriendlyCreepers", friendlyTag);

        return tag;
    }

    public boolean isCreeperDenied(UUID uuid, long gameTime) {
        Long expiry = deniedCreepers.get(uuid);
        return expiry != null && gameTime < expiry;
    }

    public boolean clearExpiredDenial(UUID uuid, long gameTime) {
        Long expiry = deniedCreepers.get(uuid);
        if (expiry == null) return false;
        if (gameTime >= expiry) {
            deniedCreepers.remove(uuid);
            fleeCreepers.remove(uuid);
            friendlyCreepers.remove(uuid);
            setDirty();
            return true;
        }
        return false;
    }

    public boolean isInFleePeriod(UUID uuid, long gameTime) {
        Long expiry = fleeCreepers.get(uuid);
        return expiry != null && gameTime < expiry;
    }

    public void addDeniedCreeper(UUID uuid, long expiry) {
        deniedCreepers.put(uuid, expiry);
        setDirty();
    }

    public void addFleeCreeper(UUID uuid, long expiry) {
        fleeCreepers.put(uuid, expiry);
        setDirty();
    }

    public void addFriendlyCreeper(UUID uuid) {
        friendlyCreepers.add(uuid);
        setDirty();
    }

    public boolean isFriendlyCreeper(UUID uuid) {
        return friendlyCreepers.contains(uuid);
    }

    public void removeFriendlyCreeper(UUID uuid) {
        friendlyCreepers.remove(uuid);
        setDirty();
    }

    public Collection<UUID> getFriendlyCreepers() {
        return friendlyCreepers;
    }
}
