package com.adonut;

import com.adonut.config.ModConfig;
import com.adonut.core.AutoSellEngine;
import com.adonut.core.ItemDropperEngine;
import com.adonut.gui.ConfigScreen;
import com.adonut.gui.ItemSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class ADonutClient implements ClientModInitializer {
    public static final String MOD_ID = "adonut";
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static KeyBinding toggleDropperKeyBinding;
    private static KeyBinding toggleSellKeyBinding;
    private static KeyBinding openMenuKeyBinding;

    @Override
    public void onInitializeClient() {
        ModConfig.getInstance(); // Load configuration

        // Register keybindings
        toggleDropperKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adonut.toggle_dropper",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.adonut"
        ));

        toggleSellKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adonut.toggle_sell",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.adonut"
        ));

        openMenuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adonut.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_INSERT,
                "category.adonut"
        ));

        // Screen key events: Only allow hotkeys inside the specific target menus (Sell & Dropper menus)
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                if (scr instanceof ConfigScreen || scr instanceof ItemSelectionScreen) return;

                if (toggleSellKeyBinding.matchesKey(key, scancode)) {
                    // Only trigger if inside the 'Sell' menu
                    if (isSellMenu(scr)) {
                        toggleSell(client);
                    }
                } else if (toggleDropperKeyBinding.matchesKey(key, scancode)) {
                    // Only trigger if inside the 'Orders -> Collect Items' dropper menu
                    if (isDropperMenu(scr)) {
                        toggleDropper(client);
                    }
                } else if (openMenuKeyBinding.matchesKey(key, scancode)) {
                    client.setScreen(new ConfigScreen(scr));
                }
            });
        });

        // In-game client tick event (when no screen is open)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleDropperKeyBinding.wasPressed()) {
                toggleDropper(client);
            }

            while (toggleSellKeyBinding.wasPressed()) {
                toggleSell(client);
            }

            while (openMenuKeyBinding.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
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

    public static void toggleDropper(MinecraftClient client) {
        ModConfig config = ModConfig.getInstance();
        config.dropperEnabled = !config.dropperEnabled;
        config.save();

        if (client.player != null) {
            client.player.sendMessage(
                    Text.translatable(config.dropperEnabled ? "text.adonut.dropper_enabled" : "text.adonut.dropper_disabled"),
                    true
            );
        }
    }

    public static void toggleSell(MinecraftClient client) {
        ModConfig config = ModConfig.getInstance();
        config.sellEnabled = !config.sellEnabled;
        config.save();

        if (client.player != null) {
            client.player.sendMessage(
                    Text.translatable(config.sellEnabled ? "text.adonut.sell_enabled" : "text.adonut.sell_disabled"),
                    true
            );

            // If toggled ON and not already in a HandledScreen, immediately send /sell command!
            if (config.sellEnabled && client.player.networkHandler != null) {
                if (!(client.currentScreen instanceof HandledScreen)) {
                    String cmd = config.sellCommand.startsWith("/") ? config.sellCommand.substring(1) : config.sellCommand;
                    client.player.networkHandler.sendCommand(cmd);
                }
            }
        }
    }
}
