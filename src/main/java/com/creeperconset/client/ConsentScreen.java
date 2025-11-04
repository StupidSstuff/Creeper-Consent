/*
 * Licensed under the Mulan Permissive Software License v2
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 */

package com.creeperconset.client;

import com.creeperconset.CreeperConsentMod;
import com.creeperconset.ConsentResponsePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.UUID;

public class ConsentScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 10;

    private final UUID creeperUuid;

    public ConsentScreen(UUID creeperUuid) {
        super(Text.translatable("creeperconset.screen.title"));
        this.creeperUuid = creeperUuid;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("creeperconset.button.accept"),
                button -> handleConsent(true)
        ).dimensions(
                centerX - BUTTON_WIDTH - BUTTON_SPACING / 2,
                centerY + 30,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("creeperconset.button.deny"),
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
                Text.translatable("creeperconset.screen.title"),
                this.width / 2,
                this.height / 2 - 50,
                0xFFFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("creeperconset.screen.message"),
                this.width / 2,
                this.height / 2 - 20,
                0xFFFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void handleConsent(boolean allowed) {
        ConsentResponsePayload payload = new ConsentResponsePayload(this.creeperUuid, allowed);
        ClientPlayNetworking.send(payload);

        CreeperConsentMod.LOGGER.info("Sent consent response: {}", allowed);

        // Clear client-side tracking
        CreeperConsentMod.clearClientPendingCreeper(this.creeperUuid);

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