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

package com.creeperconset.mixin;

import com.creeperconset.CreeperConsentMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Creeper.class)
public class CreeperEntityMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void onRegisterGoals(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        ((MobAccessor) creeper).getGoalSelector().addGoal(2, new AvoidEntityGoal<>(
                creeper, Player.class, 10.0f, 1.0, 1.2,
                livingEntity -> {
                    UUID uuid = creeper.getUUID();
                    long gameTime = creeper.level().getGameTime();
                    return CreeperConsentMod.isCreeperDenied(uuid, gameTime)
                            && CreeperConsentMod.isInFleePeriod(uuid, gameTime);
                }
        ));
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;

        if (!creeper.level().isClientSide()) {
            UUID uuid = creeper.getUUID();
            long gameTime = creeper.level().getGameTime();

            if (CreeperConsentMod.isCreeperDenied(uuid, gameTime)) {
                ci.cancel();
                return;
            }

            if (CreeperConsentMod.clearExpiredDenial(uuid, gameTime)) {
                creeper.setCustomName(null);
                creeper.setCustomNameVisible(false);
            }

            ServerPlayer nearestPlayer = (ServerPlayer) creeper.level()
                    .getNearestPlayer(creeper, 10.0);

            if (nearestPlayer != null) {
                CreeperConsentMod.requestConsent(creeper, nearestPlayer);
                ci.cancel();
            }
        }
    }
}
