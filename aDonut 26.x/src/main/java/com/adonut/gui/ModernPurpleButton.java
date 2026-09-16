package com.adonut.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ModernPurpleButton extends AbstractWidget {
    public interface OnPress {
        void onPress(ModernPurpleButton button);
    }

    public enum Style {
        PRIMARY,
        TOGGLE_ON,
        TOGGLE_OFF,
        DANGER,
        SECONDARY,
        TAB_ACTIVE,
        TAB_INACTIVE
    }

    private final OnPress onPress;
    private Style style = Style.SECONDARY;

    public ModernPurpleButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public ModernPurpleButton(int x, int y, int width, int height, Component message, Style style, OnPress onPress) {
        super(x, y, width, height, message);
        this.style = style;
        this.onPress = onPress;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return this.style;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hovered = this.isHovered();
        boolean active = this.isActive();

        int topColor;
        int bottomColor;
        int borderColor;
        int glowColor = 0;
        int textColor;

        if (!active) {
            topColor = 0xAA120524;
            bottomColor = 0xAA0A0314;
            borderColor = 0x334C1D95;
            textColor = 0xFF666677;
        } else {
            switch (style) {
                case TAB_ACTIVE:
                    topColor = hovered ? 0xF03B0F64 : 0xEE2A0A4A;
                    bottomColor = hovered ? 0xF0200538 : 0xEE150326;
                    borderColor = 0xFFE879F9;
                    glowColor = hovered ? 0x55E879F9 : 0x22C084FC;
                    textColor = 0xFFFFFFFF;
                    break;
                case TAB_INACTIVE:
                    topColor = hovered ? 0xCC20083B : 0x99100520;
                    bottomColor = hovered ? 0xCC110424 : 0x99090214;
                    borderColor = hovered ? 0xFFA855F7 : 0x44581C87;
                    glowColor = hovered ? 0x33A855F7 : 0;
                    textColor = hovered ? 0xFFE9D5FF : 0xFF9CA3AF;
                    break;
                case TOGGLE_ON:
                    topColor = hovered ? 0xEE113D22 : 0xDD0B2B18;
                    bottomColor = hovered ? 0xEE092213 : 0xDD061B0E;
                    borderColor = hovered ? 0xFF4ADE80 : 0xFF22C55E;
                    glowColor = hovered ? 0x6622C55E : 0x2216A34A;
                    textColor = 0xFFFFFFFF;
                    break;
                case TOGGLE_OFF:
                    topColor = hovered ? 0xEE450E1F : 0xDD2F0A15;
                    bottomColor = hovered ? 0xEE270711 : 0xDD1D050D;
                    borderColor = hovered ? 0xFFF87171 : 0xFFEF4444;
                    glowColor = hovered ? 0x66EF4444 : 0x22DC2626;
                    textColor = 0xFFFFFFFF;
                    break;
                case DANGER:
                    topColor = hovered ? 0xEE4A0E1F : 0xCC2E0812;
                    bottomColor = hovered ? 0xEE2A0610 : 0xCC180308;
                    borderColor = hovered ? 0xFFF87171 : 0xFFDC2626;
                    glowColor = hovered ? 0x55EF4444 : 0;
                    textColor = hovered ? 0xFFFFFFFF : 0xFFFCA5A5;
                    break;
                case PRIMARY:
                    topColor = hovered ? 0xF04C1D95 : 0xEE2E1065;
                    bottomColor = hovered ? 0xF02B0E5A : 0xEE190638;
                    borderColor = hovered ? 0xFFF0ABFC : 0xFFC084FC;
                    glowColor = hovered ? 0x77E879F9 : 0x33A855F7;
                    textColor = 0xFFFFFFFF;
                    break;
                case SECONDARY:
                default:
                    topColor = hovered ? 0xF02C0E4E : 0xDD1C0835;
                    bottomColor = hovered ? 0xF017052C : 0xDD0E031E;
                    borderColor = hovered ? 0xFFD8B4FE : 0xFF9333EA;
                    glowColor = hovered ? 0x44C084FC : 0;
                    textColor = hovered ? 0xFFFFFFFF : 0xFFE9D5FF;
                    break;
            }
        }

        // 1. Outer Glow
        if (glowColor != 0) {
            extractor.outline(x - 1, y - 1, w + 2, h + 2, glowColor);
        }

        // 2. Button Glass Fill (Vertical Gradient)
        renderGradient(extractor, x, y, x + w, y + h, topColor, bottomColor);

        // 3. Top subtle highlight sheen
        if (active && h > 4) {
            extractor.fill(x + 1, y + 1, x + w - 1, y + 2, 0x33FFFFFF);
        }

        // 4. Glowing Neon Border
        extractor.outline(x, y, w, h, borderColor);

        // 5. Active Tab Bottom Accent Bar
        if (style == Style.TAB_ACTIVE) {
            extractor.fill(x + 2, y + h - 2, x + w - 2, y + h, 0xFFF472B6);
        }

        // 6. Centered Text
        Font font = Minecraft.getInstance().font;
        int textY = y + (h - 8) / 2;
        extractor.centeredText(font, this.getMessage(), x + w / 2, textY, textColor);
    }

    private void renderGradient(GuiGraphicsExtractor extractor, int x1, int y1, int x2, int y2, int colTop, int colBottom) {
        int height = y2 - y1;
        if (height <= 0) return;

        int a1 = (colTop >> 24) & 0xFF, r1 = (colTop >> 16) & 0xFF, g1 = (colTop >> 8) & 0xFF, b1 = colTop & 0xFF;
        int a2 = (colBottom >> 24) & 0xFF, r2 = (colBottom >> 16) & 0xFF, g2 = (colBottom >> 8) & 0xFF, b2 = colBottom & 0xFF;

        for (int row = y1; row < y2; row++) {
            float ratio = (float) (row - y1) / (float) height;
            int a = (int) (a1 + (a2 - a1) * ratio);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            extractor.fill(x1, row, x2, row + 1, col);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
