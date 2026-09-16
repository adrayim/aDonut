package com.adonut.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ModernSwitchWidget extends AbstractWidget {
    public interface OnToggle {
        void onToggle(ModernSwitchWidget widget, boolean checked);
    }

    private boolean checked;
    private final OnToggle onToggle;

    public ModernSwitchWidget(int x, int y, boolean initialValue, OnToggle onToggle) {
        super(x, y, 36, 16, Component.literal("Switch"));
        this.checked = initialValue;
        this.onToggle = onToggle;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.checked = !this.checked;
        if (this.onToggle != null) {
            this.onToggle.onToggle(this, this.checked);
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

        int trackBg;
        int trackBorder;
        int knobX;
        int knobColor;

        if (checked) {
            trackBg = hovered ? 0xFF8B5CF6 : 0xFF7C3AED;
            trackBorder = hovered ? 0xFFE879F9 : 0xFFC084FC;
            knobX = x + w - 14;
            knobColor = 0xFFFFFFFF;
        } else {
            trackBg = hovered ? 0xFF24163B : 0xFF180E29;
            trackBorder = hovered ? 0xFF6B21A8 : 0xFF4C1D95;
            knobX = x + 2;
            knobColor = 0xFF9CA3AF;
        }

        // 1. Track Outer Glow on Hover
        if (hovered) {
            extractor.outline(x - 1, y - 1, w + 2, h + 2, checked ? 0x44C084FC : 0x22A855F7);
        }

        // 2. Track Background
        extractor.fill(x, y, x + w, y + h, trackBg);
        extractor.outline(x, y, w, h, trackBorder);

        // 3. Track Text (ON / OFF)
        Font font = Minecraft.getInstance().font;
        if (checked) {
            extractor.text(font, "ON", x + 5, y + 4, 0xFFE9D5FF, false);
        } else {
            extractor.text(font, "OFF", x + 15, y + 4, 0xFF6B7280, false);
        }

        // 4. Sliding Knob (12x12 square with slight bevel)
        int knobY = y + 2;
        extractor.fill(knobX, knobY, knobX + 12, knobY + 12, knobColor);
        extractor.outline(knobX, knobY, 12, 12, checked ? 0xFFE9D5FF : 0xFF374151);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
