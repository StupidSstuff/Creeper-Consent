/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record FriendlyCreeperPayload(UUID creeperUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FriendlyCreeperPayload> TYPE = new CustomPacketPayload.Type<>(CreeperConsentMod.FRIENDLY_CREEPER_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, FriendlyCreeperPayload> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeUUID(value.creeperUuid()),
            buf -> new FriendlyCreeperPayload(buf.readUUID())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
