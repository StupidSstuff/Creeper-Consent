/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset.mixin;

import com.creeperconset.CreeperConsentMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperEntityMixin {

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;

        if (!creeper.level().isClientSide()) {
            ServerPlayer nearestPlayer = (ServerPlayer) creeper.level()
                    .getNearestPlayer(creeper, 10.0);

            if (nearestPlayer != null) {
                CreeperConsentMod.requestConsent(creeper, nearestPlayer);
                ci.cancel();
            }
        }
    }
}