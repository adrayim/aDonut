package com.adonut.core;

import com.adonut.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.regex.Pattern;

public class AutoSellEngine {
    private static final AutoSellEngine INSTANCE = new AutoSellEngine();
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private enum SellState {
        FILLING,
        WAITING_AFTER_MOVE,
        CONFIRMING,
        WAITING_AFTER_CONFIRM
    }

    private SellState state = SellState.FILLING;
    private long lastActionTime = 0;
    private long lastCommandTime = 0;

    public static AutoSellEngine getInstance() {
        return INSTANCE;
    }

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            resetState();
            return;
        }

        ModConfig config = ModConfig.getInstance();
        if (!config.sellEnabled) {
            resetState();
            return;
        }

        long now = System.currentTimeMillis();

        // 1. If in-game (menu closed or after sell confirm)
        if (!(client.gui.screen() instanceof AbstractContainerScreen<?> containerScreen)) {
            resetState();

            if (now - lastCommandTime >= Math.max(0, config.sellLoopDelayMs)) {
                if (hasMatchingItemsInInventory(client, config)) {
                    if (client.player.connection != null) {
                        String cmd = config.sellCommand.startsWith("/") ? config.sellCommand.substring(1) : config.sellCommand;
                        client.player.connection.sendCommand(cmd);
                        lastCommandTime = now;
                    }
                }
            }
            return;
        }

        String rawTitle = containerScreen.getTitle().getString();
        String cleanTitle = stripColorCodes(rawTitle);

        if (!cleanTitle.equalsIgnoreCase(config.sellMenuTitle) && !cleanTitle.contains(config.sellMenuTitle)) {
            resetState();
            return;
        }

        AbstractContainerMenu menu = containerScreen.getMenu();

        // 2. INSTANT BATCH MODE (Toplu Aktarım)
        if (config.batchMode) {
            if (client.gameMode == null) return;

            // Shift-click ALL matching inventory slots in one single tick
            for (Slot slot : menu.slots) {
                if (slot.index < 54) continue; // Player inventory slots (54..89)
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && matchesFilter(stack, config)) {
                    client.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.QUICK_MOVE, client.player);
                }
            }

            // Immediately click confirm button (slot 53 / lime_stained_glass_pane)
            int confirmSlot = findConfirmSlot(menu, config);
            if (confirmSlot != -1) {
                client.gameMode.handleContainerInput(menu.containerId, confirmSlot, 0, ContainerInput.PICKUP, client.player);
                lastActionTime = now;
                lastCommandTime = now;
            }
            return;
        }

        // 3. Sequential Mode
        switch (state) {
            case FILLING -> {
                int playerSlotToMove = findMatchingPlayerSlot(menu, config);
                boolean hasSpaceInSellGrid = hasSpaceInSellArea(menu);

                if (playerSlotToMove != -1 && hasSpaceInSellGrid) {
                    if (client.gameMode != null) {
                        client.gameMode.handleContainerInput(menu.containerId, playerSlotToMove, 0, ContainerInput.QUICK_MOVE, client.player);
                        lastActionTime = now;
                        state = SellState.WAITING_AFTER_MOVE;
                    }
                } else {
                    if (countItemsInSellArea(menu) > 0) {
                        state = SellState.CONFIRMING;
                    }
                }
            }
            case WAITING_AFTER_MOVE -> {
                if (now - lastActionTime >= config.sellItemDelayMs) {
                    state = SellState.FILLING;
                }
            }
            case CONFIRMING -> {
                int confirmSlot = findConfirmSlot(menu, config);
                if (confirmSlot != -1 && client.gameMode != null) {
                    client.gameMode.handleContainerInput(menu.containerId, confirmSlot, 0, ContainerInput.PICKUP, client.player);
                    lastActionTime = now;
                    lastCommandTime = now;
                    state = SellState.WAITING_AFTER_CONFIRM;
                }
            }
            case WAITING_AFTER_CONFIRM -> {
                if (now - lastActionTime >= config.sellLoopDelayMs) {
                    state = SellState.FILLING;
                }
            }
        }
    }

    public void resetState() {
        state = SellState.FILLING;
        lastActionTime = 0;
    }

    private boolean hasMatchingItemsInInventory(Minecraft client, ModConfig config) {
        if (client.player == null) return false;
        for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && matchesFilter(stack, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceInSellArea(AbstractContainerMenu menu) {
        for (int i = 0; i <= 44 && i < menu.slots.size(); i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private int countItemsInSellArea(AbstractContainerMenu menu) {
        int count = 0;
        for (int i = 0; i <= 44 && i < menu.slots.size(); i++) {
            if (!menu.getSlot(i).getItem().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int findMatchingPlayerSlot(AbstractContainerMenu menu, ModConfig config) {
        for (Slot slot : menu.slots) {
            if (slot.index < 54) continue;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            if (matchesFilter(stack, config)) {
                return slot.index;
            }
        }
        return -1;
    }

    private boolean matchesFilter(ItemStack stack, ModConfig config) {
        if (config.sellAll) {
            return true;
        }

        if (config.sellItemList == null || config.sellItemList.isEmpty()) {
            return false;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase();
        String itemPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();
        String displayName = stripColorCodes(stack.getHoverName().getString()).toLowerCase();

        for (String target : config.sellItemList) {
            if (target == null || target.trim().isEmpty()) continue;
            String targetClean = target.trim().toLowerCase();
            if (itemId.equals(targetClean) || itemPath.equals(targetClean) || displayName.equalsIgnoreCase(targetClean)
                    || itemId.contains(targetClean) || itemPath.contains(targetClean) || displayName.contains(targetClean)) {
                return true;
            }
        }

        return false;
    }

    private int findConfirmSlot(AbstractContainerMenu menu, ModConfig config) {
        for (Slot slot : menu.slots) {
            if (slot.index > 53) continue;
            ItemStack stack = slot.getItem();
            if (stack.is(Items.STAINED_GLASS_PANE.lime())) {
                return slot.index;
            }
        }
        if (config.sellConfirmSlotIndex >= 0 && config.sellConfirmSlotIndex < menu.slots.size()) {
            return config.sellConfirmSlotIndex;
        }
        return 53;
    }

    private static String stripColorCodes(String input) {
        if (input == null) return "";
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
