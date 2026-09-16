package com.adonut.core;

import com.adonut.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            resetState();
            return;
        }

        ModConfig config = ModConfig.getInstance();
        if (!config.dropperEnabled) {
            resetState();
            return;
        }

        if (!(client.gui.screen() instanceof AbstractContainerScreen<?> containerScreen)) {
            resetState();
            return;
        }

        String rawTitle = containerScreen.getTitle().getString();
        String cleanTitle = stripColorCodes(rawTitle);

        if (!cleanTitle.contains(config.dropperMenuTitle) && !rawTitle.contains(config.dropperMenuTitle)) {
            resetState();
            return;
        }

        AbstractContainerMenu menu = containerScreen.getMenu();
        long now = System.currentTimeMillis();

        switch (state) {
            case READY_TO_DROP -> {
                int dropSlotId = findDropSlot(menu, config);
                if (dropSlotId != -1 && client.gameMode != null) {
                    client.gameMode.handleContainerInput(menu.containerId, dropSlotId, 0, ContainerInput.PICKUP, client.player);
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
                int nextSlotId = findNextPageSlot(menu, config);
                if (nextSlotId != -1 && client.gameMode != null) {
                    client.gameMode.handleContainerInput(menu.containerId, nextSlotId, 0, ContainerInput.PICKUP, client.player);
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

    private int findDropSlot(AbstractContainerMenu menu, ModConfig config) {
        if (config.smartSearch) {
            for (Slot slot : menu.slots) {
                if (slot.index > 53) continue;
                ItemStack stack = slot.getItem();
                if (stack.is(Items.DROPPER)) {
                    String name = stripColorCodes(stack.getHoverName().getString()).toLowerCase();
                    if (name.contains("drop")) {
                        return slot.index;
                    }
                }
            }
        }
        if (config.dropSlotIndex >= 0 && config.dropSlotIndex < menu.slots.size()) {
            return config.dropSlotIndex;
        }
        return -1;
    }

    private int findNextPageSlot(AbstractContainerMenu menu, ModConfig config) {
        if (config.smartSearch) {
            for (Slot slot : menu.slots) {
                if (slot.index > 53) continue;
                ItemStack stack = slot.getItem();
                if (stack.is(Items.ARROW)) {
                    String name = stripColorCodes(stack.getHoverName().getString()).toLowerCase();
                    if (name.contains("next") && !name.contains("prev") && !name.contains("previous")) {
                        return slot.index;
                    }
                }
            }
        }
        if (config.nextSlotIndex >= 0 && config.nextSlotIndex < menu.slots.size()) {
            return config.nextSlotIndex;
        }
        return -1;
    }

    public static String stripColorCodes(String input) {
        if (input == null) return "";
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
