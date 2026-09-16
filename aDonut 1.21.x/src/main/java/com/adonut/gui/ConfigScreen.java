package com.adonut.gui;

import com.adonut.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private enum Tab {
        SELL,
        DROPPER
    }

    private Tab currentTab = Tab.SELL;

    // Tabs
    private ModernPurpleButton tabSellBtn;
    private ModernPurpleButton tabDropperBtn;

    // --- Sell Widgets ---
    private ModernPurpleButton sellToggleBtn;
    private ModernPurpleButton batchModeBtn;
    private ModernPurpleButton openItemSelectorBtn;
    private TextFieldWidget sellConfirmDelayField;
    private TextFieldWidget sellLoopDelayField;
    private TextFieldWidget sellTitleField;
    private TextFieldWidget sellCommandField;

    // --- Dropper Widgets ---
    private ModernPurpleButton dropperToggleBtn;
    private TextFieldWidget dropperDropDelayField;
    private TextFieldWidget dropperLoopDelayField;
    private ModernPurpleButton dropperSmartSearchBtn;
    private TextFieldWidget dropperTitleField;
    private TextFieldWidget dropperDropSlotField;
    private TextFieldWidget dropperNextSlotField;

    public ConfigScreen(Screen parent) {
        super(Text.literal("aDonut Kontrol Paneli"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        int centerX = this.width / 2;
        int cardTop = 8;
        int tabY = cardTop + 28;
        int fieldWidth = 230;

        // --- Tabs ---
        this.tabSellBtn = new ModernPurpleButton(
                centerX - 120, tabY, 115, 22,
                Text.literal((currentTab == Tab.SELL ? "✦ " : "") + "Auto Sell (/sell)"),
                currentTab == Tab.SELL ? ModernPurpleButton.Style.TAB_ACTIVE : ModernPurpleButton.Style.TAB_INACTIVE,
                button -> {
                    saveCurrentFields();
                    currentTab = Tab.SELL;
                    this.init();
                }
        );
        this.addDrawableChild(this.tabSellBtn);

        this.tabDropperBtn = new ModernPurpleButton(
                centerX + 5, tabY, 115, 22,
                Text.literal((currentTab == Tab.DROPPER ? "✦ " : "") + "Auto Dropper"),
                currentTab == Tab.DROPPER ? ModernPurpleButton.Style.TAB_ACTIVE : ModernPurpleButton.Style.TAB_INACTIVE,
                button -> {
                    saveCurrentFields();
                    currentTab = Tab.DROPPER;
                    this.init();
                }
        );
        this.addDrawableChild(this.tabDropperBtn);

        if (currentTab == Tab.SELL) {
            // 1. Toggle Button
            this.sellToggleBtn = new ModernPurpleButton(
                    centerX - fieldWidth / 2, cardTop + 56, fieldWidth, 22,
                    getSellToggleText(),
                    config.sellEnabled ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF,
                    button -> {
                        config.sellEnabled = !config.sellEnabled;
                        button.setMessage(getSellToggleText());
                        button.setStyle(config.sellEnabled ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF);
                    }
            );
            this.addDrawableChild(this.sellToggleBtn);

            // 2. Batch Mode Toggle (Toplu Aktarım)
            this.batchModeBtn = new ModernPurpleButton(
                    centerX - fieldWidth / 2, cardTop + 82, fieldWidth, 22,
                    getBatchModeText(),
                    config.batchMode ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF,
                    button -> {
                        config.batchMode = !config.batchMode;
                        button.setMessage(getBatchModeText());
                        button.setStyle(config.batchMode ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF);
                    }
            );
            this.addDrawableChild(this.batchModeBtn);

            // 3. Open Item Selection Screen
            String itemCount = config.sellAll ? "Tüm Eşyalar" : config.sellItemList.size() + " Eşya Seçili";
            this.openItemSelectorBtn = new ModernPurpleButton(
                    centerX - fieldWidth / 2, cardTop + 108, fieldWidth, 22,
                    Text.literal("§d📋 Satılacak Eşyalar: §e" + itemCount + " §d➔"),
                    ModernPurpleButton.Style.PRIMARY,
                    button -> {
                        saveCurrentFields();
                        if (this.client != null) {
                            this.client.setScreen(new ItemSelectionScreen(this));
                        }
                    }
            );
            this.addDrawableChild(this.openItemSelectorBtn);

            // 4. Delays (Side-by-side)
            int halfWidth = (fieldWidth - 10) / 2;
            this.sellConfirmDelayField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 152, halfWidth - 4, 16, Text.literal("Confirm Delay"));
            this.sellConfirmDelayField.setText(String.valueOf(config.sellConfirmDelayMs));
            this.sellConfirmDelayField.setDrawsBackground(false);
            this.addDrawableChild(this.sellConfirmDelayField);

            this.sellLoopDelayField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + halfWidth + 10 + 2, cardTop + 152, halfWidth - 4, 16, Text.literal("Loop Delay"));
            this.sellLoopDelayField.setText(String.valueOf(config.sellLoopDelayMs));
            this.sellLoopDelayField.setDrawsBackground(false);
            this.addDrawableChild(this.sellLoopDelayField);

            // 5. Target Menu Title & Sell Command (Side by side)
            this.sellTitleField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 192, halfWidth - 4, 16, Text.literal("Sell Title"));
            this.sellTitleField.setText(config.sellMenuTitle);
            this.sellTitleField.setMaxLength(64);
            this.sellTitleField.setDrawsBackground(false);
            this.addDrawableChild(this.sellTitleField);

            this.sellCommandField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + halfWidth + 10 + 2, cardTop + 192, halfWidth - 4, 16, Text.literal("Sell Command"));
            this.sellCommandField.setText(config.sellCommand);
            this.sellCommandField.setMaxLength(32);
            this.sellCommandField.setDrawsBackground(false);
            this.addDrawableChild(this.sellCommandField);
        } else {
            // 1. Dropper Toggle Button
            this.dropperToggleBtn = new ModernPurpleButton(
                    centerX - fieldWidth / 2, cardTop + 56, fieldWidth, 22,
                    getDropperToggleText(),
                    config.dropperEnabled ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF,
                    button -> {
                        config.dropperEnabled = !config.dropperEnabled;
                        button.setMessage(getDropperToggleText());
                        button.setStyle(config.dropperEnabled ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF);
                    }
            );
            this.addDrawableChild(this.dropperToggleBtn);

            // 2. Drop Delay Field
            this.dropperDropDelayField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 96, fieldWidth - 4, 16, Text.literal("Drop Delay"));
            this.dropperDropDelayField.setText(String.valueOf(config.dropDelayMs));
            this.dropperDropDelayField.setDrawsBackground(false);
            this.addDrawableChild(this.dropperDropDelayField);

            // 3. Loop Delay Field
            this.dropperLoopDelayField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 132, fieldWidth - 4, 16, Text.literal("Loop Delay"));
            this.dropperLoopDelayField.setText(String.valueOf(config.loopDelayMs));
            this.dropperLoopDelayField.setDrawsBackground(false);
            this.addDrawableChild(this.dropperLoopDelayField);

            // 4. Smart Search Button
            this.dropperSmartSearchBtn = new ModernPurpleButton(
                    centerX - fieldWidth / 2, cardTop + 156, fieldWidth, 20,
                    getDropperSmartSearchText(),
                    config.smartSearch ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF,
                    button -> {
                        config.smartSearch = !config.smartSearch;
                        button.setMessage(getDropperSmartSearchText());
                        button.setStyle(config.smartSearch ? ModernPurpleButton.Style.TOGGLE_ON : ModernPurpleButton.Style.TOGGLE_OFF);
                    }
            );
            this.addDrawableChild(this.dropperSmartSearchBtn);

            // 5. Target Menu Title
            this.dropperTitleField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 192, fieldWidth - 4, 16, Text.literal("Menu Title"));
            this.dropperTitleField.setText(config.dropperMenuTitle);
            this.dropperTitleField.setMaxLength(64);
            this.dropperTitleField.setDrawsBackground(false);
            this.addDrawableChild(this.dropperTitleField);

            // 6. Slots (Side by side)
            int halfWidth = (fieldWidth - 10) / 2;
            this.dropperDropSlotField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + 2, cardTop + 226, halfWidth - 4, 16, Text.literal("Drop Slot"));
            this.dropperDropSlotField.setText(String.valueOf(config.dropSlotIndex));
            this.dropperDropSlotField.setDrawsBackground(false);
            this.addDrawableChild(this.dropperDropSlotField);

            this.dropperNextSlotField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2 + halfWidth + 10 + 2, cardTop + 226, halfWidth - 4, 16, Text.literal("Next Slot"));
            this.dropperNextSlotField.setText(String.valueOf(config.nextSlotIndex));
            this.dropperNextSlotField.setDrawsBackground(false);
            this.addDrawableChild(this.dropperNextSlotField);
        }

        // --- Bottom Action Buttons ---
        int bottomY = cardTop + 256;
        this.addDrawableChild(new ModernPurpleButton(
                centerX - fieldWidth / 2, bottomY, fieldWidth / 2 - 5, 22,
                Text.literal("§a✔ Kaydet"),
                ModernPurpleButton.Style.PRIMARY,
                button -> saveAndClose()
        ));

        this.addDrawableChild(new ModernPurpleButton(
                centerX + 5, bottomY, fieldWidth / 2 - 5, 22,
                Text.literal("§c↺ Sıfırla"),
                ModernPurpleButton.Style.DANGER,
                button -> resetFields()
        ));
    }

    private Text getSellToggleText() {
        return Text.literal((config.sellEnabled ? "§a✔ " : "§c✖ ") + "Auto Sell: " + (config.sellEnabled ? "§aAÇIK" : "§cKAPALI"));
    }

    private Text getBatchModeText() {
        return Text.literal((config.batchMode ? "§b⚡ Toplu Aktarım: §aAÇIK" : "§7⚡ Toplu Aktarım: §cKAPALI"));
    }

    private Text getDropperToggleText() {
        return Text.literal((config.dropperEnabled ? "§a✔ " : "§c✖ ") + "Auto Dropper: " + (config.dropperEnabled ? "§aAÇIK" : "§cKAPALI"));
    }

    private Text getDropperSmartSearchText() {
        return Text.literal("Akıllı Arama: " + (config.smartSearch ? "§aAÇIK" : "§7KAPALI (Sabit Slot)"));
    }

    private void saveCurrentFields() {
        if (currentTab == Tab.SELL) {
            if (sellConfirmDelayField != null) {
                try { config.sellConfirmDelayMs = Math.max(0, Integer.parseInt(sellConfirmDelayField.getText().trim())); } catch (Exception ignored) {}
            }
            if (sellLoopDelayField != null) {
                try { config.sellLoopDelayMs = Math.max(0, Integer.parseInt(sellLoopDelayField.getText().trim())); } catch (Exception ignored) {}
            }
            if (sellTitleField != null) {
                String t = sellTitleField.getText().trim();
                if (!t.isEmpty()) config.sellMenuTitle = t;
            }
            if (sellCommandField != null) {
                String c = sellCommandField.getText().trim();
                if (!c.isEmpty()) config.sellCommand = c;
            }
        } else {
            if (dropperDropDelayField != null) {
                try { config.dropDelayMs = Math.max(0, Integer.parseInt(dropperDropDelayField.getText().trim())); } catch (Exception ignored) {}
            }
            if (dropperLoopDelayField != null) {
                try { config.loopDelayMs = Math.max(0, Integer.parseInt(dropperLoopDelayField.getText().trim())); } catch (Exception ignored) {}
            }
            if (dropperTitleField != null) {
                String t = dropperTitleField.getText().trim();
                if (!t.isEmpty()) config.dropperMenuTitle = t;
            }
            if (dropperDropSlotField != null) {
                try { config.dropSlotIndex = Integer.parseInt(dropperDropSlotField.getText().trim()); } catch (Exception ignored) {}
            }
            if (dropperNextSlotField != null) {
                try { config.nextSlotIndex = Integer.parseInt(dropperNextSlotField.getText().trim()); } catch (Exception ignored) {}
            }
        }
        config.save();
    }

    private void saveAndClose() {
        saveCurrentFields();
        this.close();
    }

    private void resetFields() {
        config.resetDefaults();
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int cardWidth = 264;
        int cardHeight = 290;
        int cardTop = 6;
        int cardLeft = centerX - cardWidth / 2;
        int cardRight = centerX + cardWidth / 2;
        int cardBottom = cardTop + cardHeight;
        int fieldWidth = 230;
        int halfWidth = (fieldWidth - 10) / 2;

        // 1. Multi-Layer Neon Glow Effect around the entire card
        context.drawBorder(cardLeft - 2, cardTop - 2, cardWidth + 4, cardHeight + 4, 0x227E22CE);
        context.drawBorder(cardLeft - 1, cardTop - 1, cardWidth + 2, cardHeight + 2, 0x44A855F7);

        // 2. Obsidian Violet Glass Panel Fill
        context.fill(cardLeft, cardTop, cardRight, cardBottom, 0xF50D041A);

        // 3. Core Vibrant Violet Border
        context.drawBorder(cardLeft, cardTop, cardWidth, cardHeight, 0xFF9333EA);
        context.drawBorder(cardLeft + 1, cardTop + 1, cardWidth - 2, cardHeight - 2, 0x33E879F9);

        // 4. Header Bar with Gradient Accent
        context.fill(cardLeft + 1, cardTop + 1, cardRight - 1, cardTop + 24, 0xFF1F0838);
        context.fill(cardLeft + 1, cardTop + 1, cardRight - 1, cardTop + 2, 0xFFC084FC);
        context.fill(cardLeft + 1, cardTop + 24, cardRight - 1, cardTop + 25, 0xFFA855F7);

        // 5. Sub-Card Input Boxes & Glass Insets
        if (currentTab == Tab.SELL) {
            int delayBoxTop = cardTop + 134;
            drawCustomInputBox(context, centerX - fieldWidth / 2, delayBoxTop + 16, halfWidth, 20, sellConfirmDelayField != null && sellConfirmDelayField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2 + halfWidth + 10, delayBoxTop + 16, halfWidth, 20, sellLoopDelayField != null && sellLoopDelayField.isFocused());

            int titleBoxTop = cardTop + 174;
            drawCustomInputBox(context, centerX - fieldWidth / 2, titleBoxTop + 16, halfWidth, 20, sellTitleField != null && sellTitleField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2 + halfWidth + 10, titleBoxTop + 16, halfWidth, 20, sellCommandField != null && sellCommandField.isFocused());
        } else {
            drawCustomInputBox(context, centerX - fieldWidth / 2, cardTop + 94, fieldWidth, 20, dropperDropDelayField != null && dropperDropDelayField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2, cardTop + 130, fieldWidth, 20, dropperLoopDelayField != null && dropperLoopDelayField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2, cardTop + 190, fieldWidth, 20, dropperTitleField != null && dropperTitleField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2, cardTop + 224, halfWidth, 20, dropperDropSlotField != null && dropperDropSlotField.isFocused());
            drawCustomInputBox(context, centerX - fieldWidth / 2 + halfWidth + 10, cardTop + 224, halfWidth, 20, dropperNextSlotField != null && dropperNextSlotField.isFocused());
        }

        super.render(context, mouseX, mouseY, delta);

        // Header Title
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§d§l✦ aDonut §f| §5Kontrol Paneli ✦"), centerX, cardTop + 8, 0xFFFFFF);

        // Labels with clean cyber icons & colors
        if (currentTab == Tab.SELL) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d⏱ Onay Gecikmesi:"), centerX - fieldWidth / 2, cardTop + 138, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d🔄 Tur Gecikmesi:"), centerX - fieldWidth / 2 + halfWidth + 10, cardTop + 138, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d🏷 Menü Başlığı:"), centerX - fieldWidth / 2, cardTop + 178, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d⌨ Komut (/sell):"), centerX - fieldWidth / 2 + halfWidth + 10, cardTop + 178, 0xFFE9D5FF);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d⏱ Drop ➔ Next Gecikmesi (ms):"), centerX - fieldWidth / 2, cardTop + 83, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d🔄 Next ➔ Drop Tur Gecikmesi (ms):"), centerX - fieldWidth / 2, cardTop + 119, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d🏷 Hedef Menü Başlığı:"), centerX - fieldWidth / 2, cardTop + 179, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d📦 Drop Slot:"), centerX - fieldWidth / 2, cardTop + 213, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("§d📦 Next Slot:"), centerX - fieldWidth / 2 + halfWidth + 10, cardTop + 213, 0xFFE9D5FF);
        }
    }

    private void drawCustomInputBox(DrawContext context, int x, int y, int width, int height, boolean focused) {
        context.fill(x, y, x + width, y + height, 0xDD090214);
        if (focused) {
            context.drawBorder(x - 1, y - 1, width + 2, height + 2, 0x66C084FC);
            context.drawBorder(x, y, width, height, 0xFFE879F9);
        } else {
            context.drawBorder(x, y, width, height, 0xFF4C1D95);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
