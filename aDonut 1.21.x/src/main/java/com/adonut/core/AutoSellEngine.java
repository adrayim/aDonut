package com.adonut.core;

import com.adonut.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

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

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
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
        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
            resetState();

            if (now - lastCommandTime >= Math.max(0, config.sellLoopDelayMs)) {
                if (hasMatchingItemsInInventory(client, config)) {
                    if (client.player.networkHandler != null) {
                        String cmd = config.sellCommand.startsWith("/") ? config.sellCommand.substring(1) : config.sellCommand;
                        client.player.networkHandler.sendCommand(cmd);
                        lastCommandTime = now;
                    }
                }
            }
            return;
        }

        String rawTitle = handledScreen.getTitle().getString();
        String cleanTitle = stripColorCodes(rawTitle);

        if (!cleanTitle.equalsIgnoreCase(config.sellMenuTitle) && !cleanTitle.contains(config.sellMenuTitle)) {
            resetState();
            return;
        }

        ScreenHandler handler = handledScreen.getScreenHandler();

        // 2. INSTANT BATCH MODE (Toplu Aktarım - Saniyede Yüzlerce Eşya)
        if (config.batchMode) {
            if (client.interactionManager == null) return;

            boolean movedAny = false;
            // Shift-click ALL matching slots in one single frame!
            for (Slot slot : handler.slots) {
                if (slot.id < 54) continue; // Player inventory only
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && matchesFilter(stack, config)) {
                    client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
                    movedAny = true;
                }
            }

            // Immediately click confirm button
            int confirmSlot = findConfirmSlot(handler, config);
            if (confirmSlot != -1) {
                client.interactionManager.clickSlot(handler.syncId, confirmSlot, 0, SlotActionType.PICKUP, client.player);
                lastActionTime = now;
                lastCommandTime = now;
            }
            return;
        }

        // 3. Sequential Mode (with configured delays)
        switch (state) {
            case FILLING -> {
                int playerSlotToMove = findMatchingPlayerSlot(handler, config);
                boolean hasSpaceInSellGrid = hasSpaceInSellArea(handler);

                if (playerSlotToMove != -1 && hasSpaceInSellGrid) {
                    if (client.interactionManager != null) {
                        client.interactionManager.clickSlot(handler.syncId, playerSlotToMove, 0, SlotActionType.QUICK_MOVE, client.player);
                        lastActionTime = now;
                        state = SellState.WAITING_AFTER_MOVE;
                    }
                } else {
                    if (countItemsInSellArea(handler) > 0) {
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
                int confirmSlot = findConfirmSlot(handler, config);
                if (confirmSlot != -1 && client.interactionManager != null) {
                    client.interactionManager.clickSlot(handler.syncId, confirmSlot, 0, SlotActionType.PICKUP, client.player);
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

    private boolean hasMatchingItemsInInventory(MinecraftClient client, ModConfig config) {
        if (client.player == null) return false;
        for (ItemStack stack : client.player.getInventory().main) {
            if (!stack.isEmpty() && matchesFilter(stack, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceInSellArea(ScreenHandler handler) {
        for (int i = 0; i <= 44 && i < handler.slots.size(); i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.isEmpty() || stack.getCount() < stack.getMaxCount()) {
                return true;
            }
        }
        return false;
    }

    private int countItemsInSellArea(ScreenHandler handler) {
        int count = 0;
        for (int i = 0; i <= 44 && i < handler.slots.size(); i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int findMatchingPlayerSlot(ScreenHandler handler, ModConfig config) {
        for (Slot slot : handler.slots) {
            if (slot.id < 54) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (matchesFilter(stack, config)) {
                return slot.id;
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

        String itemId = Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();
        String itemPath = Registries.ITEM.getId(stack.getItem()).getPath().toLowerCase();
        String displayName = stripColorCodes(stack.getName().getString()).toLowerCase();

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

    private int findConfirmSlot(ScreenHandler handler, ModConfig config) {
        for (Slot slot : handler.slots) {
            if (slot.id > 53) continue;
            ItemStack stack = slot.getStack();
            if (stack.isOf(Items.LIME_STAINED_GLASS_PANE)) {
                return slot.id;
            }
        }
        if (config.sellConfirmSlotIndex >= 0 && config.sellConfirmSlotIndex < handler.slots.size()) {
            return config.sellConfirmSlotIndex;
        }
        return 53;
    }

    private static String stripColorCodes(String input) {
        if (input == null) return "";
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
