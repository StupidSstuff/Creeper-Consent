/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset.client;

import com.creeperconset.CreeperConsentMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class ConsentScreen extends Screen {
    private static final Text TITLE = Text.literal("Creeper Consent Request");
    private static final Text MESSAGE = Text.literal("A creeper has asked for your permission to explode (on you)");
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 10;
    
    public ConsentScreen() {
        super(TITLE);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Sure, let's go"),
                button -> handleConsent(true)
        ).dimensions(
                centerX - BUTTON_WIDTH - BUTTON_SPACING / 2,
                centerY + 30,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());
        
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Nah, not today"),
                button -> handleConsent(false)
        ).dimensions(
                centerX + BUTTON_SPACING / 2,
                centerY + 30,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                TITLE,
                this.width / 2,
                this.height / 2 - 50,
                0xFFFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                MESSAGE,
                this.width / 2,
                this.height / 2 - 20,
                0xFFFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }
    
    private void handleConsent(boolean allowed) {
        CreeperEntity creeper = CreeperConsentMod.getPendingCreeper();
        
        if (creeper != null && !creeper.isRemoved()) {
            World world = creeper.getEntityWorld();
            if (allowed) {
                CreeperConsentMod.LOGGER.info("Consent granted.");
                if (world != null) {
                    world.createExplosion(
                        creeper,
                        creeper.getX(),
                        creeper.getY(),
                        creeper.getZ(),
                        3.0f,
                        World.ExplosionSourceType.MOB
                    );
                }
                creeper.discard();
            } else {
                CreeperConsentMod.LOGGER.info("Consent denied.");
                creeper.discard();
            }
        } else {
            CreeperConsentMod.LOGGER.warn("No active creeper.");
        }
        
        CreeperConsentMod.clearPendingCreeper();
        
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return true;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
