/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CreeperConsentMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "creeperconset";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CONSENT_REQUEST_ID = Identifier.of(MOD_ID, "consent_request");
    public static final Identifier CONSENT_RESPONSE_ID = Identifier.of(MOD_ID, "consent_response");

    private static final Map<UUID, CreeperEntity> awaitingConsent = new ConcurrentHashMap<>();
    private static final Set<UUID> requestedCreepers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> clientHandledCreepers = ConcurrentHashMap.newKeySet();
    private static UUID clientPendingCreeperUuid = null;

    @Override
    public void onInitialize() {
        LOGGER.info("Creeper Consent Mod initialized (Server)");

        PayloadTypeRegistry.playC2S().register(ConsentResponsePayload.ID, ConsentResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConsentRequestPayload.ID, ConsentRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConsentResponsePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                handleConsentResponse(payload.creeperUuid(), payload.allowed(), context.player());
            });
        });
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Creeper Consent Mod initialized (Client)");
        CreeperConsentModClient.registerClientNetworking();
    }

    public static void requestConsent(CreeperEntity creeper, ServerPlayerEntity player) {
        UUID creeperUuid = creeper.getUuid();

        if (!requestedCreepers.add(creeperUuid)) {
            return;
        }

        awaitingConsent.put(creeperUuid, creeper);

        ConsentRequestPayload payload = new ConsentRequestPayload(creeperUuid);
        ServerPlayNetworking.send(player, payload);

        LOGGER.info("Sent consent request to player {} for creeper {}", player.getName().getString(), creeperUuid);
    }

    private static void handleConsentResponse(UUID creeperUuid, boolean allowed, ServerPlayerEntity player) {
        CreeperEntity creeper = awaitingConsent.remove(creeperUuid);
        requestedCreepers.remove(creeperUuid);

        if (creeper == null || creeper.isRemoved()) {
            LOGGER.warn("Creeper {} no longer exists", creeperUuid);
            return;
        }

        if (allowed) {
            LOGGER.info("Player {} granted consent for explosion", player.getName().getString());
            creeper.getEntityWorld().createExplosion(
                    creeper,
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    3.0f,
                    net.minecraft.world.World.ExplosionSourceType.MOB
            );
        } else {
            LOGGER.info("Player {} denied consent", player.getName().getString());
        }

        creeper.discard();
    }

    public static boolean setClientPendingCreeper(UUID creeperUuid) {
        if (!clientHandledCreepers.add(creeperUuid)) {
            LOGGER.info("Client already handling creeper {}, ignoring duplicate request", creeperUuid);
            return false;
        }

        clientPendingCreeperUuid = creeperUuid;
        LOGGER.info("Client received consent request for creeper {}", creeperUuid);
        return true;
    }

    public static UUID getClientPendingCreeperUuid() {
        return clientPendingCreeperUuid;
    }

    public static void clearClientPendingCreeper(UUID creeperUuid) {
        if (clientPendingCreeperUuid != null && clientPendingCreeperUuid.equals(creeperUuid)) {
            clientPendingCreeperUuid = null;
        }
        clientHandledCreepers.remove(creeperUuid);
    }
}