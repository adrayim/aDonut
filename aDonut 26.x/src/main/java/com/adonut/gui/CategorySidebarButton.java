package com.adonut.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CategorySidebarButton extends AbstractWidget {
    public interface OnPress {
        void onPress(CategorySidebarButton button);
    }

    private final boolean activeCategory;
    private final OnPress onPress;

    public CategorySidebarButton(int x, int y, int width, int height, Component message, boolean activeCategory, OnPress onPress) {
        super(x, y, width, height, message);
        this.activeCategory = activeCategory;
        this.onPress = onPress;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        if (!this.visible) return;

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hovered = this.isHovered();

        if (activeCategory) {
            // Active background
            extractor.fill(x, y, x + w, y + h, 0xEE2A0E4E);
            extractor.outline(x, y, w, h, 0xFF7C3AED);
            // Left indicator bar
            extractor.fill(x, y + 2, x + 3, y + h - 2, 0xFFE879F9);
        } else if (hovered) {
            extractor.fill(x, y, x + w, y + h, 0x881E0A38);
            extractor.outline(x, y, w, h, 0x44581C87);
        }

        Font font = Minecraft.getInstance().font;
        int textColor = activeCategory ? 0xFFFFFFFF : (hovered ? 0xFFE9D5FF : 0xFF9CA3AF);
        int textY = y + (h - 8) / 2;
        extractor.text(font, this.getMessage(), x + (activeCategory ? 8 : 6), textY, textColor, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
