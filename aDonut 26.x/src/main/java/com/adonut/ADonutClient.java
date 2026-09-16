package com.adonut;

import com.adonut.config.ModConfig;
import com.adonut.core.AutoSellEngine;
import com.adonut.core.ItemDropperEngine;
import com.adonut.gui.ConfigScreen;
import com.adonut.gui.ItemSelectionScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class ADonutClient implements ClientModInitializer {
    public static final String MOD_ID = "adonut";
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static KeyMapping toggleDropperKeyBinding;
    private static KeyMapping toggleSellKeyBinding;
    private static KeyMapping openMenuKeyBinding;

    @Override
    public void onInitializeClient() {
        ModConfig.getInstance(); // Load configuration

        // Register keybindings
        toggleDropperKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.adonut.toggle_dropper",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        toggleSellKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.adonut.toggle_sell",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC
        ));

        openMenuKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.adonut.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_INSERT,
                KeyMapping.Category.MISC
        ));

        // Screen key events: Only allow hotkeys inside target menus
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, event) -> {
                if (scr instanceof ConfigScreen || scr instanceof ItemSelectionScreen) return;

                if (toggleSellKeyBinding.matches(event)) {
                    if (isSellMenu(scr)) {
                        toggleSell(client);
                    }
                } else if (toggleDropperKeyBinding.matches(event)) {
                    if (isDropperMenu(scr)) {
                        toggleDropper(client);
                    }
                } else if (openMenuKeyBinding.matches(event)) {
                    client.setScreenAndShow(new ConfigScreen(scr));
                }
            });
        });

        // In-game client tick event (when no screen is open)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleDropperKeyBinding.consumeClick()) {
                toggleDropper(client);
            }

            while (toggleSellKeyBinding.consumeClick()) {
                toggleSell(client);
            }

            while (openMenuKeyBinding.consumeClick()) {
                client.setScreenAndShow(new ConfigScreen(client.gui.screen()));
            }

            // Tick both engines
            ItemDropperEngine.getInstance().tick(client);
            AutoSellEngine.getInstance().tick(client);
        });
    }

    public static boolean isSellMenu(Screen screen) {
        if (screen == null) return false;
        String title = stripColor(screen.getTitle().getString()).trim();
        String target = ModConfig.getInstance().sellMenuTitle.trim();
        return title.equalsIgnoreCase(target) || title.contains(target);
    }

    public static boolean isDropperMenu(Screen screen) {
        if (screen == null) return false;
        String cleanTitle = stripColor(screen.getTitle().getString());
        String rawTitle = screen.getTitle().getString();
        String target = ModConfig.getInstance().dropperMenuTitle;
        return cleanTitle.contains(target) || rawTitle.contains(target);
    }

    public static String stripColor(String input) {
        if (input == null) return "";
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static void toggleDropper(Minecraft client) {
        ModConfig config = ModConfig.getInstance();
        config.dropperEnabled = !config.dropperEnabled;
        config.save();

        if (client.player != null) {
            client.player.sendOverlayMessage(
                    Component.translatable(config.dropperEnabled ? "text.adonut.dropper_enabled" : "text.adonut.dropper_disabled")
            );
        }
    }

    public static void toggleSell(Minecraft client) {
        ModConfig config = ModConfig.getInstance();
        config.sellEnabled = !config.sellEnabled;
        config.save();

        if (client.player != null) {
            client.player.sendOverlayMessage(
                    Component.translatable(config.sellEnabled ? "text.adonut.sell_enabled" : "text.adonut.sell_disabled")
            );

            // If toggled ON and not already in an AbstractContainerScreen, send /sell
            if (config.sellEnabled && client.player.connection != null) {
                if (!(client.gui.screen() instanceof AbstractContainerScreen)) {
                    String cmd = config.sellCommand.startsWith("/") ? config.sellCommand.substring(1) : config.sellCommand;
                    client.player.connection.sendCommand(cmd);
                }
            }
        }
    }
}
