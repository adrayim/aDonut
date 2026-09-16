package com.adonut.gui;

import com.adonut.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class ItemSelectionScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private ItemListWidget listWidget;
    private TextFieldWidget inputField;
    private ModernPurpleButton addBtn;
    private ModernPurpleButton removeBtn;

    public ItemSelectionScreen(Screen parent) {
        super(Text.literal("Satılacak Eşyalar"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        this.listWidget = new ItemListWidget(this.client, this.width, this.height - 75, 36, 28);
        this.addDrawableChild(this.listWidget);
        populateList();

        int bottomY = this.height - 32;
        int inputWidth = 130;

        this.inputField = new TextFieldWidget(this.textRenderer, 18, bottomY + 2, inputWidth - 4, 16, Text.literal("Item ID/Name"));
        this.inputField.setPlaceholder(Text.literal("§7eşya adı veya ID"));
        this.inputField.setMaxLength(64);
        this.inputField.setDrawsBackground(false);
        this.addDrawableChild(this.inputField);

        this.addBtn = new ModernPurpleButton(
                16 + inputWidth + 6, bottomY, 56, 20,
                Text.literal("§a+ Ekle"),
                ModernPurpleButton.Style.PRIMARY,
                button -> {
                    String text = this.inputField.getText().trim();
                    if (!text.isEmpty()) {
                        addItem(text);
                        this.inputField.setText("");
                        populateList();
                        config.save();
                    }
                }
        );
        this.addDrawableChild(this.addBtn);

        this.removeBtn = new ModernPurpleButton(
                16 + inputWidth + 6 + 60, bottomY, 75, 20,
                Text.literal("§c✕ Sil"),
                ModernPurpleButton.Style.DANGER,
                button -> {
                    ItemListWidget.ItemEntry selected = this.listWidget.getSelectedOrNull();
                    if (selected != null) {
                        config.sellItemList.remove(selected.rawString);
                        populateList();
                        config.save();
                    }
                }
        );
        this.addDrawableChild(this.removeBtn);

        this.addDrawableChild(new ModernPurpleButton(
                16 + inputWidth + 6 + 60 + 79, bottomY, 90, 20,
                Text.literal("§e📦 Envanteri Ekle"),
                ModernPurpleButton.Style.SECONDARY,
                button -> addInventoryItems()
        ));

        this.addDrawableChild(new ModernPurpleButton(
                this.width - 100, bottomY, 86, 20,
                Text.literal("§a✔ Kaydet"),
                ModernPurpleButton.Style.PRIMARY,
                button -> {
                    config.save();
                    this.close();
                }
        ));
    }

    private void addInventoryItems() {
        if (this.client != null && this.client.player != null) {
            for (ItemStack stack : this.client.player.getInventory().main) {
                if (!stack.isEmpty()) {
                    String id = Registries.ITEM.getId(stack.getItem()).getPath();
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
        this.listWidget.clear();
        for (String itemStr : new ArrayList<>(config.sellItemList)) {
            ItemStack stack = findItemStack(itemStr);
            this.listWidget.addEntry(this.listWidget.new ItemEntry(itemStr, stack));
        }
    }

    private ItemStack findItemStack(String nameOrId) {
        String clean = nameOrId.toLowerCase().trim();
        Identifier id = Identifier.tryParse(clean.contains(":") ? clean : "minecraft:" + clean);
        if (id != null && Registries.ITEM.containsId(id)) {
            Item item = Registries.ITEM.get(id);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        for (Item item : Registries.ITEM) {
            String path = Registries.ITEM.getId(item).getPath().toLowerCase();
            String name = item.getName().getString().toLowerCase();
            if (path.contains(clean) || name.contains(clean)) {
                return new ItemStack(item);
            }
        }
        return new ItemStack(Items.CHEST);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Header purple bar with neon glow
        context.fill(0, 0, this.width, 32, 0xF50D041A);
        context.fill(0, 0, this.width, 1, 0xFFC084FC);
        context.fill(0, 31, this.width, 32, 0xFF9333EA);
        context.drawBorder(0, 30, this.width, 2, 0x44A855F7);

        // Footer purple bar with neon glow
        int footerTop = this.height - 38;
        context.fill(0, footerTop, this.width, this.height, 0xF50D041A);
        context.fill(0, footerTop, this.width, footerTop + 1, 0xFF9333EA);
        context.drawBorder(0, footerTop - 1, this.width, 2, 0x44A855F7);

        // Input frame
        int inputWidth = 130;
        int bottomY = this.height - 32;
        boolean focused = inputField != null && inputField.isFocused();
        context.fill(16, bottomY, 16 + inputWidth, bottomY + 20, 0xDD090214);
        if (focused) {
            context.drawBorder(15, bottomY - 1, inputWidth + 2, 22, 0x66C084FC);
            context.drawBorder(16, bottomY, inputWidth, 20, 0xFFE879F9);
        } else {
            context.drawBorder(16, bottomY, inputWidth, 20, 0xFF4C1D95);
        }

        super.render(context, mouseX, mouseY, delta);

        String title = "Satılacak Eşyalar (" + config.sellItemList.size() + ")";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§d§l✦ aDonut §f| §5" + title + " ✦"), this.width / 2, 11, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    public class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemListWidget.ItemEntry> {
        public ItemListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
        }

        public void clear() {
            super.clearEntries();
        }

        public int addEntry(ItemEntry entry) {
            return super.addEntry(entry);
        }

        public class ItemEntry extends AlwaysSelectedEntryListWidget.Entry<ItemEntry> {
            public final String rawString;
            public final ItemStack itemStack;

            public ItemEntry(String rawString, ItemStack itemStack) {
                this.rawString = rawString;
                this.itemStack = itemStack;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
                boolean selected = isSelected();

                if (selected) {
                    context.fill(x - 2, y, x + entryWidth + 2, y + 26, 0xDD2A0A4A);
                    context.drawBorder(x - 2, y, entryWidth + 4, 26, 0xFFE879F9);
                } else if (hovered) {
                    context.fill(x - 2, y, x + entryWidth + 2, y + 26, 0x881E0838);
                    context.drawBorder(x - 2, y, entryWidth + 4, 26, 0x66A855F7);
                }

                context.drawItem(itemStack, x + 4, y + 5);

                String displayName = itemStack.getItem() != Items.AIR ? itemStack.getName().getString() : rawString;
                context.drawTextWithShadow(textRenderer, Text.literal("§f" + displayName), x + 26, y + 5, 0xFFFFFF);

                String idText = "minecraft:" + Registries.ITEM.getId(itemStack.getItem()).getPath();
                context.drawTextWithShadow(textRenderer, Text.literal("§d" + idText), x + 26, y + 15, 0xC084FC);
            }

            public boolean isSelected() {
                return ItemListWidget.this.getSelectedOrNull() == this;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ItemListWidget.this.setSelected(this);
                return true;
            }

            @Override
            public Text getNarration() {
                return Text.literal(rawString);
            }
        }
    }
}
