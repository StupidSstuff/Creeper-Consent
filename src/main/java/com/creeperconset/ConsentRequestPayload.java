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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ConsentRequestPayload(UUID creeperUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConsentRequestPayload> TYPE = new CustomPacketPayload.Type<>(CreeperConsentMod.CONSENT_REQUEST_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ConsentRequestPayload> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeUUID(value.creeperUuid()),
            buf -> new ConsentRequestPayload(buf.readUUID())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}