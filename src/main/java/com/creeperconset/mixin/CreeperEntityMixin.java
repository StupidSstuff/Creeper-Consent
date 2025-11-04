/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset.mixin;

import com.creeperconset.CreeperConsentMod;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;

        if (!creeper.getEntityWorld().isClient()) {
            ServerPlayerEntity nearestPlayer = (ServerPlayerEntity) creeper.getEntityWorld()
                    .getClosestPlayer(creeper, 10.0);

            if (nearestPlayer != null) {
                CreeperConsentMod.requestConsent(creeper, nearestPlayer);
                ci.cancel();
            }
        }
    }
}