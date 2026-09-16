package com.adonut.gui;

import com.adonut.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private enum Tab {
        SELL,
        DROPPER
    }

    private Tab currentTab = Tab.SELL;

    // Sidebar Category Buttons
    private CategorySidebarButton sidebarSellBtn;
    private CategorySidebarButton sidebarDropperBtn;

    // --- Sell Widgets ---
    private ModernPurpleButton openItemSelectorBtn;
    private EditBox sellDelayField;
    private EditBox sellTitleField;
    private EditBox sellCommandField;

    // --- Dropper Widgets ---
    private EditBox dropperDelayField;
    private EditBox dropperTitleField;
    private EditBox dropperDropSlotField;
    private EditBox dropperNextSlotField;

    // Footer Buttons
    private ModernPurpleButton saveBtn;
    private ModernPurpleButton resetBtn;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("gui.adonut.title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        // Ensure batchMode is always true
        config.batchMode = true;

        int modalWidth = 360;
        int modalHeight = 186;
        int modalLeft = (this.width - modalWidth) / 2;
        int modalTop = (this.height - modalHeight) / 2;
        int sidebarWidth = 84;
        int contentRight = modalLeft + modalWidth - 12;

        // --- Sidebar Category Buttons ---
        int sidebarY = modalTop + 8;
        this.sidebarSellBtn = new CategorySidebarButton(
                modalLeft + 6, sidebarY, sidebarWidth - 12, 22,
                Component.translatable("gui.adonut.tab.sell"),
                currentTab == Tab.SELL,
                button -> {
                    saveCurrentFields();
                    currentTab = Tab.SELL;
                    this.init();
                }
        );
        this.addRenderableWidget(this.sidebarSellBtn);

        this.sidebarDropperBtn = new CategorySidebarButton(
                modalLeft + 6, sidebarY + 26, sidebarWidth - 12, 22,
                Component.translatable("gui.adonut.tab.dropper"),
                currentTab == Tab.DROPPER,
                button -> {
                    saveCurrentFields();
                    currentTab = Tab.DROPPER;
                    this.init();
                }
        );
        this.addRenderableWidget(this.sidebarDropperBtn);

        // --- Content Rows ---
        if (currentTab == Tab.SELL) {
            // Row 1: Item Selector Button
            this.openItemSelectorBtn = new ModernPurpleButton(
                    contentRight - 64, modalTop + 33, 64, 18,
                    Component.translatable("gui.adonut.sell.edit_btn"),
                    ModernPurpleButton.Style.PRIMARY,
                    button -> {
                        saveCurrentFields();
                        if (this.minecraft != null) {
                            this.minecraft.setScreenAndShow(new ItemSelectionScreen(this));
                        }
                    }
            );
            this.addRenderableWidget(this.openItemSelectorBtn);

            // Row 2: Delay Field
            this.sellDelayField = new EditBox(this.font, contentRight - 50, modalTop + 73, 46, 10, Component.literal("Delay"));
            this.sellDelayField.setValue(String.valueOf(config.sellLoopDelayMs));
            this.sellDelayField.setBordered(false);
            this.addRenderableWidget(this.sellDelayField);

            // Row 3: Menu Title Field
            this.sellTitleField = new EditBox(this.font, contentRight - 126, modalTop + 109, 122, 10, Component.literal("Sell Title"));
            this.sellTitleField.setValue(config.sellMenuTitle);
            this.sellTitleField.setMaxLength(64);
            this.sellTitleField.setBordered(false);
            this.addRenderableWidget(this.sellTitleField);

            // Row 4: Sell Command Field
            this.sellCommandField = new EditBox(this.font, contentRight - 50, modalTop + 145, 46, 10, Component.literal("Sell Command"));
            this.sellCommandField.setValue(config.sellCommand);
            this.sellCommandField.setMaxLength(32);
            this.sellCommandField.setBordered(false);
            this.addRenderableWidget(this.sellCommandField);
        } else {
            // Row 1: Delay Field
            this.dropperDelayField = new EditBox(this.font, contentRight - 50, modalTop + 37, 46, 10, Component.literal("Delay"));
            this.dropperDelayField.setValue(String.valueOf(config.dropDelayMs));
            this.dropperDelayField.setBordered(false);
            this.addRenderableWidget(this.dropperDelayField);

            // Row 2: Menu Title Field
            this.dropperTitleField = new EditBox(this.font, contentRight - 126, modalTop + 73, 122, 10, Component.literal("Menu Title"));
            this.dropperTitleField.setValue(config.dropperMenuTitle);
            this.dropperTitleField.setMaxLength(64);
            this.dropperTitleField.setBordered(false);
            this.addRenderableWidget(this.dropperTitleField);

            // Row 3: Drop Slot & Next Slot Fields
            this.dropperDropSlotField = new EditBox(this.font, contentRight - 70, modalTop + 109, 26, 10, Component.literal("Drop Slot"));
            this.dropperDropSlotField.setValue(String.valueOf(config.dropSlotIndex));
            this.dropperDropSlotField.setBordered(false);
            this.addRenderableWidget(this.dropperDropSlotField);

            this.dropperNextSlotField = new EditBox(this.font, contentRight - 32, modalTop + 109, 26, 10, Component.literal("Next Slot"));
            this.dropperNextSlotField.setValue(String.valueOf(config.nextSlotIndex));
            this.dropperNextSlotField.setBordered(false);
            this.addRenderableWidget(this.dropperNextSlotField);
        }

        // --- Bottom Action Buttons ---
        this.resetBtn = new ModernPurpleButton(
                modalLeft + 6, modalTop + modalHeight - 22, sidebarWidth - 12, 16,
                Component.translatable("gui.adonut.btn.reset"),
                ModernPurpleButton.Style.DANGER,
                button -> resetFields()
        );
        this.addRenderableWidget(this.resetBtn);

        this.saveBtn = new ModernPurpleButton(
                contentRight - 64, modalTop + modalHeight - 22, 64, 16,
                Component.translatable("gui.adonut.btn.save"),
                ModernPurpleButton.Style.PRIMARY,
                button -> saveAndClose()
        );
        this.addRenderableWidget(this.saveBtn);
    }

    private void saveCurrentFields() {
        config.batchMode = true;

        if (currentTab == Tab.SELL) {
            if (sellDelayField != null) {
                try {
                    int delay = Math.max(0, Integer.parseInt(sellDelayField.getValue().trim()));
                    config.sellLoopDelayMs = delay;
                    config.sellConfirmDelayMs = Math.min(25, delay / 2);
                } catch (Exception ignored) {}
            }
            if (sellTitleField != null) {
                String t = sellTitleField.getValue().trim();
                if (!t.isEmpty()) config.sellMenuTitle = t;
            }
            if (sellCommandField != null) {
                String c = sellCommandField.getValue().trim();
                if (!c.isEmpty()) config.sellCommand = c;
            }
        } else {
            if (dropperDelayField != null) {
                try {
                    int delay = Math.max(0, Integer.parseInt(dropperDelayField.getValue().trim()));
                    config.dropDelayMs = delay;
                    config.loopDelayMs = delay * 2;
                } catch (Exception ignored) {}
            }
            if (dropperTitleField != null) {
                String t = dropperTitleField.getValue().trim();
                if (!t.isEmpty()) config.dropperMenuTitle = t;
            }
            if (dropperDropSlotField != null) {
                try { config.dropSlotIndex = Integer.parseInt(dropperDropSlotField.getValue().trim()); } catch (Exception ignored) {}
            }
            if (dropperNextSlotField != null) {
                try { config.nextSlotIndex = Integer.parseInt(dropperNextSlotField.getValue().trim()); } catch (Exception ignored) {}
            }
        }
        config.save();
    }

    private void saveAndClose() {
        saveCurrentFields();
        this.onClose();
    }

    private void resetFields() {
        config.resetDefaults();
        config.batchMode = true;
        this.init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(extractor);

        int modalWidth = 360;
        int modalHeight = 186;
        int modalLeft = (this.width - modalWidth) / 2;
        int modalTop = (this.height - modalHeight) / 2;
        int modalRight = modalLeft + modalWidth;
        int modalBottom = modalTop + modalHeight;
        int sidebarWidth = 84;
        int sidebarRight = modalLeft + sidebarWidth;
        int contentLeft = sidebarRight + 12;
        int contentRight = modalRight - 12;

        // 1. Soft Outer Ambient Glow
        extractor.outline(modalLeft - 2, modalTop - 2, modalWidth + 4, modalHeight + 4, 0x188B5CF6);
        extractor.outline(modalLeft - 1, modalTop - 1, modalWidth + 2, modalHeight + 2, 0x33A855F7);

        // 2. Main Modal Dark Glass Background
        extractor.fill(modalLeft, modalTop, modalRight, modalBottom, 0xF50D061A);
        extractor.outline(modalLeft, modalTop, modalWidth, modalHeight, 0xFF7C3AED);

        // 3. Sidebar Background & Vertical Divider
        extractor.fill(modalLeft, modalTop, sidebarRight, modalBottom, 0x88090214);
        extractor.fill(sidebarRight, modalTop, sidebarRight + 1, modalBottom, 0xFF4C1D95);

        // 4. Input Slots Background Frames
        if (currentTab == Tab.SELL) {
            drawInputFrame(extractor, contentRight - 54, modalTop + 69, 54, 18, sellDelayField != null && sellDelayField.isFocused());
            drawInputFrame(extractor, contentRight - 130, modalTop + 105, 130, 18, sellTitleField != null && sellTitleField.isFocused());
            drawInputFrame(extractor, contentRight - 54, modalTop + 141, 54, 18, sellCommandField != null && sellCommandField.isFocused());

            // Dividers
            int r1 = modalTop + 61;
            int r2 = modalTop + 97;
            int r3 = modalTop + 133;
            extractor.fill(contentLeft, r1, contentRight, r1 + 1, 0x224C1D95);
            extractor.fill(contentLeft, r2, contentRight, r2 + 1, 0x224C1D95);
            extractor.fill(contentLeft, r3, contentRight, r3 + 1, 0x224C1D95);
        } else {
            drawInputFrame(extractor, contentRight - 54, modalTop + 33, 54, 18, dropperDelayField != null && dropperDelayField.isFocused());
            drawInputFrame(extractor, contentRight - 130, modalTop + 69, 130, 18, dropperTitleField != null && dropperTitleField.isFocused());
            drawInputFrame(extractor, contentRight - 74, modalTop + 105, 34, 18, dropperDropSlotField != null && dropperDropSlotField.isFocused());
            drawInputFrame(extractor, contentRight - 36, modalTop + 105, 34, 18, dropperNextSlotField != null && dropperNextSlotField.isFocused());

            // Dividers
            int r1 = modalTop + 61;
            int r2 = modalTop + 97;
            extractor.fill(contentLeft, r1, contentRight, r1 + 1, 0x224C1D95);
            extractor.fill(contentLeft, r2, contentRight, r2 + 1, 0x224C1D95);
        }

        super.extractRenderState(extractor, mouseX, mouseY, delta);

        // Tab Content Title & Description Rows
        if (currentTab == Tab.SELL) {
            extractor.text(this.font, Component.literal("§d§l").append(Component.translatable("gui.adonut.title.sell")), contentLeft, modalTop + 10, 0xFFFFFFFF, true);

            // Row 1: Satılacak Eşyalar
            Component countComp = config.sellAll ? Component.translatable("gui.adonut.sell.items_all") : Component.translatable("gui.adonut.sell.items_count", config.sellItemList.size());
            extractor.text(this.font, Component.translatable("gui.adonut.sell.items_title"), contentLeft, modalTop + 31, 0xFFFFFF, true);
            extractor.text(this.font, Component.literal("§7").append(Component.translatable("gui.adonut.sell.items_desc", Component.literal("§e").append(countComp))), contentLeft, modalTop + 42, 0xFF9CA3AF, false);

            // Row 2: Delay
            extractor.text(this.font, Component.translatable("gui.adonut.sell.delay_title"), contentLeft, modalTop + 67, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.sell.delay_desc"), contentLeft, modalTop + 78, 0xFF9CA3AF, false);

            // Row 3: Menü Başlığı
            extractor.text(this.font, Component.translatable("gui.adonut.sell.menu_title"), contentLeft, modalTop + 103, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.sell.menu_desc"), contentLeft, modalTop + 114, 0xFF9CA3AF, false);

            // Row 4: Komut
            extractor.text(this.font, Component.translatable("gui.adonut.sell.command_title"), contentLeft, modalTop + 139, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.sell.command_desc"), contentLeft, modalTop + 150, 0xFF9CA3AF, false);
        } else {
            extractor.text(this.font, Component.literal("§d§l").append(Component.translatable("gui.adonut.title.dropper")), contentLeft, modalTop + 10, 0xFFFFFFFF, true);

            // Row 1: Delay
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.delay_title"), contentLeft, modalTop + 31, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.delay_desc"), contentLeft, modalTop + 42, 0xFF9CA3AF, false);

            // Row 2: Menü Başlığı
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.menu_title"), contentLeft, modalTop + 67, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.menu_desc"), contentLeft, modalTop + 78, 0xFF9CA3AF, false);

            // Row 3: Slotlar
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.slots_title"), contentLeft, modalTop + 103, 0xFFFFFF, true);
            extractor.text(this.font, Component.translatable("gui.adonut.dropper.slots_desc"), contentLeft, modalTop + 114, 0xFF9CA3AF, false);
        }
    }

    private void drawInputFrame(GuiGraphicsExtractor extractor, int x, int y, int width, int height, boolean focused) {
        extractor.fill(x, y, x + width, y + height, 0xEE120524);
        if (focused) {
            extractor.outline(x - 1, y - 1, width + 2, height + 2, 0x55C084FC);
            extractor.outline(x, y, width, height, 0xFFE879F9);
        } else {
            extractor.outline(x, y, width, height, 0xFF4C1D95);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}
