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

public record ConsentRequestPayload(UUID creeperUuid) implements CustomPayload {
    public static final Id<ConsentRequestPayload> ID = new Id<>(CreeperConsentMod.CONSENT_REQUEST_ID);

    public static final PacketCodec<RegistryByteBuf, ConsentRequestPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeUuid(value.creeperUuid),
            buf -> new ConsentRequestPayload(buf.readUuid())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}