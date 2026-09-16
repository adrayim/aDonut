package com.adonut.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig INSTANCE;

    // --- Auto Dropper Settings ---
    public boolean dropperEnabled = false;
    public int dropDelayMs = 250;
    public int loopDelayMs = 500;
    public int dropSlotIndex = 52;
    public int nextSlotIndex = 53;
    public boolean smartSearch = true;
    public String dropperMenuTitle = "Orders -> Collect Items";

    // --- Auto Sell Settings ---
    public boolean sellEnabled = false;
    public boolean sellAll = false;
    public boolean batchMode = true; // Toplu Aktarım (Batch Transfer)
    public List<String> sellItemList = new ArrayList<>(List.of("sugar_cane", "gunpowder", "cactus"));
    public int sellItemDelayMs = 0;
    public int sellConfirmDelayMs = 0;
    public int sellLoopDelayMs = 50;
    public String sellMenuTitle = "Sell";
    public String sellCommand = "sell";
    public int sellConfirmSlotIndex = 53;

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("adonut_26.2.json");
    }

    public static ModConfig load() {
        File file = getConfigFile().toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    if (config.sellItemList == null) {
                        config.sellItemList = new ArrayList<>(List.of("sugar_cane", "gunpowder", "cactus"));
                    }
                    if (config.sellCommand == null || config.sellCommand.isEmpty()) {
                        config.sellCommand = "sell";
                    }
                    return config;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        File file = getConfigFile().toFile();
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetDefaults() {
        this.dropperEnabled = false;
        this.dropDelayMs = 250;
        this.loopDelayMs = 500;
        this.dropSlotIndex = 52;
        this.nextSlotIndex = 53;
        this.smartSearch = true;
        this.dropperMenuTitle = "Orders -> Collect Items";

        this.sellEnabled = false;
        this.sellAll = false;
        this.batchMode = true;
        this.sellItemList = new ArrayList<>(List.of("sugar_cane", "gunpowder", "cactus"));
        this.sellItemDelayMs = 0;
        this.sellConfirmDelayMs = 0;
        this.sellLoopDelayMs = 50;
        this.sellMenuTitle = "Sell";
        this.sellCommand = "sell";
        this.sellConfirmSlotIndex = 53;
        save();
    }
}
