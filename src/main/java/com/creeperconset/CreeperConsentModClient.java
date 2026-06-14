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

import com.creeperconset.client.ConsentScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class CreeperConsentModClient {

    private static final Set<UUID> friendlyCreepers = ConcurrentHashMap.newKeySet();

    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(ConsentRequestPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                removeFriendlyCreeper(payload.creeperUuid());
                if (CreeperConsentMod.setClientPendingCreeper(payload.creeperUuid())) {
                    context.client().setScreen(new ConsentScreen(payload.creeperUuid()));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FriendlyCreeperPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                friendlyCreepers.add(payload.creeperUuid());
            });
        });
    }

    public static boolean isFriendlyCreeper(UUID uuid) {
        return friendlyCreepers.contains(uuid);
    }

    public static void removeFriendlyCreeper(UUID uuid) {
        friendlyCreepers.remove(uuid);
    }
}
