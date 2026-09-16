package com.adonut.gui;

import com.adonut.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class ItemSelectionScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private ItemListWidget listWidget;
    private EditBox inputField;
    private ModernPurpleButton addBtn;
    private ModernPurpleButton removeBtn;

    public ItemSelectionScreen(Screen parent) {
        super(Component.translatable("gui.adonut.sell.items_title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        this.listWidget = new ItemListWidget(this.minecraft, this.width, this.height - 75, 36, 28);
        this.addRenderableWidget(this.listWidget);
        populateList();

        int bottomY = this.height - 32;
        int inputWidth = 130;

        this.inputField = new EditBox(this.font, 18, bottomY + 2, inputWidth - 4, 16, Component.literal("Item ID/Name"));
        this.inputField.setHint(Component.translatable("gui.adonut.item_selector.hint"));
        this.inputField.setMaxLength(64);
        this.inputField.setBordered(false);
        this.addRenderableWidget(this.inputField);

        this.addBtn = new ModernPurpleButton(
                16 + inputWidth + 6, bottomY, 56, 20,
                Component.translatable("gui.adonut.item_selector.add"),
                ModernPurpleButton.Style.PRIMARY,
                button -> {
                    String text = this.inputField.getValue().trim();
                    if (!text.isEmpty()) {
                        addItem(text);
                        this.inputField.setValue("");
                        populateList();
                        config.save();
                    }
                }
        );
        this.addRenderableWidget(this.addBtn);

        this.removeBtn = new ModernPurpleButton(
                16 + inputWidth + 6 + 60, bottomY, 75, 20,
                Component.translatable("gui.adonut.item_selector.delete"),
                ModernPurpleButton.Style.DANGER,
                button -> {
                    ItemListWidget.ItemEntry selected = this.listWidget.getSelected();
                    if (selected != null) {
                        config.sellItemList.remove(selected.rawString);
                        populateList();
                        config.save();
                    }
                }
        );
        this.addRenderableWidget(this.removeBtn);

        this.addRenderableWidget(new ModernPurpleButton(
                16 + inputWidth + 6 + 60 + 79, bottomY, 90, 20,
                Component.translatable("gui.adonut.item_selector.add_inventory"),
                ModernPurpleButton.Style.SECONDARY,
                button -> addInventoryItems()
        ));

        this.addRenderableWidget(new ModernPurpleButton(
                this.width - 100, bottomY, 86, 20,
                Component.translatable("gui.adonut.item_selector.save"),
                ModernPurpleButton.Style.PRIMARY,
                button -> {
                    config.save();
                    this.onClose();
                }
        ));
    }

    private void addInventoryItems() {
        if (this.minecraft != null && this.minecraft.player != null) {
            for (ItemStack stack : this.minecraft.player.getInventory().getNonEquipmentItems()) {
                if (!stack.isEmpty()) {
                    String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                    if (!config.sellItemList.contains(id)) {
                        config.sellItemList.add(id);
                    }
                }
            }
            populateList();
            config.save();
        }
    }

    private void addItem(String input) {
        String clean = input.toLowerCase().replace("minecraft:", "").trim();
        if (!config.sellItemList.contains(clean)) {
            config.sellItemList.add(clean);
        }
    }

    private void populateList() {
        this.listWidget.clearEntries();
        for (String itemStr : new ArrayList<>(config.sellItemList)) {
            ItemStack stack = findItemStack(itemStr);
            this.listWidget.addEntry(this.listWidget.new ItemEntry(itemStr, stack));
        }
    }

    private ItemStack findItemStack(String nameOrId) {
        String clean = nameOrId.toLowerCase().trim();
        Identifier id = Identifier.tryParse(clean.contains(":") ? clean : "minecraft:" + clean);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();
            String name = item.getName(new ItemStack(item)).getString().toLowerCase();
            if (path.contains(clean) || name.contains(clean)) {
                return new ItemStack(item);
            }
        }
        return new ItemStack(Items.CHEST);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(extractor);

        // Header purple bar with neon glow
        extractor.fill(0, 0, this.width, 32, 0xF50D041A);
        extractor.fill(0, 0, this.width, 1, 0xFFC084FC);
        extractor.fill(0, 31, this.width, 32, 0xFF9333EA);
        extractor.outline(0, 30, this.width, 2, 0x44A855F7);

        // Footer purple bar with neon glow
        int footerTop = this.height - 38;
        extractor.fill(0, footerTop, this.width, this.height, 0xF50D041A);
        extractor.fill(0, footerTop, this.width, footerTop + 1, 0xFF9333EA);
        extractor.outline(0, footerTop - 1, this.width, 2, 0x44A855F7);

        // Input frame
        int inputWidth = 130;
        int bottomY = this.height - 32;
        boolean focused = inputField != null && inputField.isFocused();
        extractor.fill(16, bottomY, 16 + inputWidth, bottomY + 20, 0xDD090214);
        if (focused) {
            extractor.outline(15, bottomY - 1, inputWidth + 2, 22, 0x66C084FC);
            extractor.outline(16, bottomY, inputWidth, 20, 0xFFE879F9);
        } else {
            extractor.outline(16, bottomY, inputWidth, 20, 0xFF4C1D95);
        }

        super.extractRenderState(extractor, mouseX, mouseY, delta);

        Component titleComp = Component.translatable("gui.adonut.item_selector.title", String.valueOf(config.sellItemList.size()));
        extractor.centeredText(this.font, Component.literal("§d§l✦ aDonut §f| §5").append(titleComp).append(" ✦"), this.width / 2, 11, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    public class ItemListWidget extends ObjectSelectionList<ItemListWidget.ItemEntry> {
        public ItemListWidget(Minecraft client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
        }

        public void clearEntries() {
            super.clearEntries();
        }

        public int addEntry(ItemEntry entry) {
            return super.addEntry(entry);
        }

        public class ItemEntry extends ObjectSelectionList.Entry<ItemEntry> {
            public final String rawString;
            public final ItemStack itemStack;

            public ItemEntry(String rawString, ItemStack itemStack) {
                this.rawString = rawString;
                this.itemStack = itemStack;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean hovered, float delta) {
                int x = getX();
                int y = getY();
                int entryWidth = getWidth();
                boolean selected = isSelected();

                // Entry card glass background
                if (selected) {
                    extractor.fill(x - 2, y, x + entryWidth + 2, y + 26, 0xDD2A0A4A);
                    extractor.outline(x - 2, y, entryWidth + 4, 26, 0xFFE879F9);
                } else if (hovered) {
                    extractor.fill(x - 2, y, x + entryWidth + 2, y + 26, 0x881E0838);
                    extractor.outline(x - 2, y, entryWidth + 4, 26, 0x66A855F7);
                }

                // Item icon
                extractor.item(itemStack, x + 4, y + 5);

                String displayName = itemStack.getItem() != Items.AIR ? itemStack.getHoverName().getString() : rawString;
                extractor.text(font, "§f" + displayName, x + 26, y + 5, 0xFFFFFF, true);

                String idText = "minecraft:" + BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
                extractor.text(font, "§d" + idText, x + 26, y + 15, 0xC084FC, true);
            }

            public boolean isSelected() {
                return ItemListWidget.this.getSelected() == this;
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                ItemListWidget.this.setSelected(this);
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(rawString);
            }
        }
    }
}
