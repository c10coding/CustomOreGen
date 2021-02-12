package net.dohaw.customoregen.config;

import net.dohaw.corelib.Config;
import net.dohaw.customoregen.CustomOreManager;
import net.dohaw.customoregen.OreWorldData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomOreConfig extends Config {

    private String customOreName;

    public CustomOreConfig(JavaPlugin plugin, String customOreName) {
        super(plugin, customOreName);
        this.customOreName = customOreName;
    }

    public CustomOreConfig(String customOreName, File file){
        super(file);
        this.customOreName = customOreName;
    }

    public CustomOreManager loadChunkManager(){

        boolean isGeneratingOre = config.getBoolean("Is Generating Ore");
        CustomOreManager manager = new CustomOreManager(plugin, customOreName, this);
        manager.setChanceOreIsInChunk(config.getDouble("Chunk Spawn Chance"));
        loadWorldData(manager);

        if(isGeneratingOre){
            manager.startGenerator();
        }

        return manager;
    }

    public void loadWorldData(CustomOreManager manager){

        Map<String, OreWorldData> worldData = new HashMap<>();

        ConfigurationSection worldsSection = config.getConfigurationSection("Worlds");
        if(worldsSection != null){
            for(String worldName : worldsSection.getKeys(false)){
                int minYLevel = worldsSection.getInt(worldName + ".Minimum Y Level");
                int maxYLevel = worldsSection.getInt(worldName + ".Maximum Y Level");
                double spawnChance = worldsSection.getDouble(worldName + ".Spawn Chance");
                boolean willGenerateOre = worldsSection.getBoolean(worldName + ".Is Generating Ore");
                OreWorldData oreWorldData = new OreWorldData(minYLevel, maxYLevel, spawnChance, willGenerateOre);
                worldData.put(worldName, oreWorldData);
            }
        }else{
            plugin.getLogger().warning("The custom ore file " + fileName + " does not have the \"Worlds\" section or it is empty...");
        }

        manager.setOreWorldData(worldData);

    }

    public void setIsGeneratingOre(boolean value){
        config.set("Is Generating Ore", value);
        saveConfig();
    }

}
