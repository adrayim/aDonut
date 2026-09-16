package com.adonut.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ModernPurpleButton extends ClickableWidget {
    public interface PressAction {
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

    private final PressAction pressAction;
    private Style style = Style.SECONDARY;

    public ModernPurpleButton(int x, int y, int width, int height, Text message, PressAction pressAction) {
        super(x, y, width, height, message);
        this.pressAction = pressAction;
    }

    public ModernPurpleButton(int x, int y, int width, int height, Text message, Style style, PressAction pressAction) {
        super(x, y, width, height, message);
        this.style = style;
        this.pressAction = pressAction;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return this.style;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.pressAction != null) {
            this.pressAction.onPress(this);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hovered = this.isHovered();
        boolean active = this.active;

        int bgColor;
        int borderColor;
        int outerGlow = 0;
        int textColor;

        if (!active) {
            bgColor = 0x88120524;
            borderColor = 0x444C1D95;
            textColor = 0xFF6B7280;
        } else {
            switch (style) {
                case TAB_ACTIVE:
                    bgColor = hovered ? 0xF03B0F64 : 0xEE2A0A4A;
                    borderColor = 0xFFC084FC;
                    outerGlow = hovered ? 0x66E879F9 : 0x33A855F7;
                    textColor = 0xFFFFFFFF;
                    break;
                case TAB_INACTIVE:
                    bgColor = hovered ? 0xCC1E0838 : 0x880E041C;
                    borderColor = hovered ? 0xFFA855F7 : 0x44581C87;
                    outerGlow = hovered ? 0x339333EA : 0;
                    textColor = hovered ? 0xFFE9D5FF : 0xFF9CA3AF;
                    break;
                case TOGGLE_ON:
                    bgColor = hovered ? 0xDD0D331D : 0xCC092314;
                    borderColor = hovered ? 0xFF4ADE80 : 0xFF22C55E;
                    outerGlow = hovered ? 0x6622C55E : 0x2216A34A;
                    textColor = 0xFFDCFCE7;
                    break;
                case TOGGLE_OFF:
                    bgColor = hovered ? 0xDD3B0E1B : 0xCC290A13;
                    borderColor = hovered ? 0xFFF87171 : 0xFFEF4444;
                    outerGlow = hovered ? 0x66EF4444 : 0x22DC2626;
                    textColor = 0xFFFEE2E2;
                    break;
                case DANGER:
                    bgColor = hovered ? 0xEE450A1A : 0xBB2A0610;
                    borderColor = hovered ? 0xFFF87171 : 0xFFDC2626;
                    outerGlow = hovered ? 0x55EF4444 : 0;
                    textColor = hovered ? 0xFFFFFFFF : 0xFFFCA5A5;
                    break;
                case PRIMARY:
                    bgColor = hovered ? 0xF04C1D95 : 0xDD2E1065;
                    borderColor = hovered ? 0xFFE879F9 : 0xFFA855F7;
                    outerGlow = hovered ? 0x77C084FC : 0x338B5CF6;
                    textColor = 0xFFFFFFFF;
                    break;
                case SECONDARY:
                default:
                    bgColor = hovered ? 0xEE2A0E4E : 0xCC1A0833;
                    borderColor = hovered ? 0xFFC084FC : 0xFF7E22CE;
                    outerGlow = hovered ? 0x44A855F7 : 0;
                    textColor = hovered ? 0xFFFFFFFF : 0xFFE9D5FF;
                    break;
            }
        }

        // 1. Outer Glow
        if (outerGlow != 0) {
            context.drawBorder(x - 1, y - 1, w + 2, h + 2, outerGlow);
        }

        // 2. Glass Fill
        context.fill(x, y, x + w, y + h, bgColor);

        // 3. Top subtle highlight sheen
        if (active && h > 6) {
            context.fill(x + 1, y + 1, x + w - 1, y + 2, 0x33FFFFFF);
        }

        // 4. Glowing Border
        context.drawBorder(x, y, w, h, borderColor);

        // 5. Active Tab Bottom Accent Bar
        if (style == Style.TAB_ACTIVE) {
            context.fill(x + 2, y + h - 2, x + w - 2, y + h, 0xFFE879F9);
        }

        // 6. Centered Text
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textY = y + (h - 8) / 2;
        context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), x + w / 2, textY, textColor);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
