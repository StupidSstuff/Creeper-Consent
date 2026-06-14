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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Creeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CreeperConsentMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "creeperconset";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CONSENT_REQUEST_ID = Identifier.fromNamespaceAndPath(MOD_ID, "consent_request");
    public static final Identifier CONSENT_RESPONSE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "consent_response");
    public static final Identifier FRIENDLY_CREEPER_ID = Identifier.fromNamespaceAndPath(MOD_ID, "friendly_creeper");

    private static final long DENIAL_DURATION_TICKS = 72000L;
    private static final long FLEE_DURATION_TICKS = 6000L;

    private static final Map<UUID, Creeper> awaitingConsent = new ConcurrentHashMap<>();
    private static final Set<UUID> requestedCreepers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> clientHandledCreepers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> deniedCreepers = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> fleeCreepers = new ConcurrentHashMap<>();
    private static UUID clientPendingCreeperUuid = null;

    @Override
    public void onInitialize() {
        LOGGER.info("Creeper Consent Mod initialized (Server)");

        PayloadTypeRegistry.serverboundPlay().register(ConsentResponsePayload.TYPE, ConsentResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConsentRequestPayload.TYPE, ConsentRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FriendlyCreeperPayload.TYPE, FriendlyCreeperPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConsentResponsePayload.TYPE, (payload, context) -> {
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

    public static void requestConsent(Creeper creeper, ServerPlayer player) {
        UUID creeperUuid = creeper.getUUID();

        if (!requestedCreepers.add(creeperUuid)) {
            return;
        }

        awaitingConsent.put(creeperUuid, creeper);

        ConsentRequestPayload payload = new ConsentRequestPayload(creeperUuid);
        ServerPlayNetworking.send(player, payload);

        LOGGER.info("Sent consent request to player {} for creeper {}", player.getName().getString(), creeperUuid);
    }

    private static void handleConsentResponse(UUID creeperUuid, boolean allowed, ServerPlayer player) {
        Creeper creeper = awaitingConsent.remove(creeperUuid);
        requestedCreepers.remove(creeperUuid);

        if (creeper == null || creeper.isRemoved()) {
            LOGGER.warn("Creeper {} no longer exists", creeperUuid);
            return;
        }

        if (allowed) {
            LOGGER.info("Player {} granted consent for explosion", player.getName().getString());
            creeper.level().explode(
                    creeper,
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    3.0f,
                    net.minecraft.world.level.Level.ExplosionInteraction.MOB
            );
            creeper.discard();
        } else {
            LOGGER.info("Player {} denied consent", player.getName().getString());
            long gameTime = creeper.level().getGameTime();
            deniedCreepers.put(creeperUuid, gameTime + DENIAL_DURATION_TICKS);
            fleeCreepers.put(creeperUuid, gameTime + FLEE_DURATION_TICKS);
            FriendlyCreeperPayload payload = new FriendlyCreeperPayload(creeperUuid);
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static boolean isCreeperDenied(UUID uuid, long gameTime) {
        Long expiry = deniedCreepers.get(uuid);
        if (expiry == null) return false;
        if (gameTime < expiry) return true;
        deniedCreepers.remove(uuid);
        fleeCreepers.remove(uuid);
        return false;
    }

    public static boolean isInFleePeriod(UUID uuid, long gameTime) {
        Long expiry = fleeCreepers.get(uuid);
        if (expiry == null) return false;
        if (gameTime < expiry) return true;
        return false;
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
