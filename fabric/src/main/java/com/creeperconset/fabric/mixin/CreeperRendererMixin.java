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

package com.creeperconset.fabric.mixin;

import com.creeperconset.CreeperConsentState;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperRenderer.class)
public class CreeperRendererMixin {
    @Unique
    private static final ResourceLocation FRIENDLY_CREEPER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreeperConsentState.MOD_ID,
                    "textures/entity/creeper/friendly_creeper.png");

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/monster/Creeper;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true)
    private void onGetTextureLocation(Creeper creeper, CallbackInfoReturnable<ResourceLocation> cir) {
        if (CreeperConsentState.isFriendlyCreeper(creeper.getUUID())) {
            cir.setReturnValue(FRIENDLY_CREEPER_TEXTURE);
        }
    }
}
