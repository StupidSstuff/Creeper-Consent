/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record ConsentResponsePayload(UUID creeperUuid, boolean allowed) implements CustomPayload {
    public static final Id<ConsentResponsePayload> ID = new Id<>(CreeperConsentMod.CONSENT_RESPONSE_ID);

    public static final PacketCodec<RegistryByteBuf, ConsentResponsePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeUuid(value.creeperUuid);
                buf.writeBoolean(value.allowed);
            },
            buf -> new ConsentResponsePayload(buf.readUuid(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}