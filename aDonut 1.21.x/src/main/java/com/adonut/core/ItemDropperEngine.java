package com.adonut.core;

import com.adonut.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.regex.Pattern;

public class ItemDropperEngine {
    private static final ItemDropperEngine INSTANCE = new ItemDropperEngine();
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private enum DropState {
        READY_TO_DROP,
        WAITING_AFTER_DROP,
        READY_TO_NEXT,
        WAITING_AFTER_NEXT
    }

    private DropState state = DropState.READY_TO_DROP;
    private long lastActionTime = 0;

    public static ItemDropperEngine getInstance() {
        return INSTANCE;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            resetState();
            return;
        }

        ModConfig config = ModConfig.getInstance();
        if (!config.dropperEnabled) {
            resetState();
            return;
        }

        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
            resetState();
            return;
        }

        String rawTitle = handledScreen.getTitle().getString();
        String cleanTitle = stripColorCodes(rawTitle);

        if (!cleanTitle.contains(config.dropperMenuTitle) && !rawTitle.contains(config.dropperMenuTitle)) {
            resetState();
            return;
        }

        ScreenHandler handler = handledScreen.getScreenHandler();
        long now = System.currentTimeMillis();

        switch (state) {
            case READY_TO_DROP -> {
                int dropSlotId = findDropSlot(handler, config);
                if (dropSlotId != -1 && client.interactionManager != null) {
                    client.interactionManager.clickSlot(handler.syncId, dropSlotId, 0, SlotActionType.PICKUP, client.player);
                    lastActionTime = now;
                    state = DropState.WAITING_AFTER_DROP;
                }
            }
            case WAITING_AFTER_DROP -> {
                if (now - lastActionTime >= config.dropDelayMs) {
                    state = DropState.READY_TO_NEXT;
                }
            }
            case READY_TO_NEXT -> {
                int nextSlotId = findNextPageSlot(handler, config);
                if (nextSlotId != -1 && client.interactionManager != null) {
                    client.interactionManager.clickSlot(handler.syncId, nextSlotId, 0, SlotActionType.PICKUP, client.player);
                    lastActionTime = now;
                    state = DropState.WAITING_AFTER_NEXT;
                }
            }
            case WAITING_AFTER_NEXT -> {
                if (now - lastActionTime >= config.loopDelayMs) {
                    state = DropState.READY_TO_DROP;
                }
            }
        }
    }

    public void resetState() {
        state = DropState.READY_TO_DROP;
        lastActionTime = 0;
    }

    private int findDropSlot(ScreenHandler handler, ModConfig config) {
        if (config.smartSearch) {
            for (Slot slot : handler.slots) {
                if (slot.id > 53) continue;
                ItemStack stack = slot.getStack();
                if (stack.isOf(Items.DROPPER)) {
                    String name = stripColorCodes(stack.getName().getString()).toLowerCase();
                    if (name.contains("drop")) {
                        return slot.id;
                    }
                }
            }
        }
        if (config.dropSlotIndex >= 0 && config.dropSlotIndex < handler.slots.size()) {
            return config.dropSlotIndex;
        }
        return -1;
    }

    private int findNextPageSlot(ScreenHandler handler, ModConfig config) {
        if (config.smartSearch) {
            for (Slot slot : handler.slots) {
                if (slot.id > 53) continue;
                ItemStack stack = slot.getStack();
                if (stack.isOf(Items.ARROW)) {
                    String name = stripColorCodes(stack.getName().getString()).toLowerCase();
                    if (name.contains("next") && !name.contains("prev") && !name.contains("previous")) {
                        return slot.id;
                    }
                }
            }
        }
        if (config.nextSlotIndex >= 0 && config.nextSlotIndex < handler.slots.size()) {
            return config.nextSlotIndex;
        }
        return -1;
    }

    public static String stripColorCodes(String input) {
        if (input == null) return "";
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
