/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset;

import com.creeperconset.client.ConsentScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class CreeperConsentModClient {

    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(ConsentRequestPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (CreeperConsentMod.setClientPendingCreeper(payload.creeperUuid())) {
                    context.client().setScreen(new ConsentScreen(payload.creeperUuid()));
                }
            });
        });
    }
}