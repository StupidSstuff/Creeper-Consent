/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset.mixin;

import com.creeperconset.client.FriendlyCreeperRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreeperRenderState.class)
public class CreeperRenderStateMixin implements FriendlyCreeperRenderState {
    @Unique
    private boolean friendly;

    @Override
    public boolean isFriendly() {
        return friendly;
    }

    @Override
    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }
}
