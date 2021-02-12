package net.dohaw.customoregen.config;

import net.dohaw.corelib.Config;
import net.dohaw.customoregen.ChunkLocation;
import net.dohaw.customoregen.CustomOreManager;
import net.dohaw.customoregen.OreWorldData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOreConfig extends Config {

    private String customOreName;

    public CustomOreConfig(JavaPlugin plugin, String customOreName) {
        super(plugin, plugin.getDataFolder() + File.separator + customOreName);
        this.customOreName = customOreName;
    }

    public CustomOreConfig(String customOreName, File file){
        super(file);
        this.customOreName = customOreName;
    }

    public CustomOreManager loadChunkManager(){
        CustomOreManager chunkManager = new CustomOreManager(customOreName, this);
        chunkManager.setOreWorldData(loadWorldData());
        return chunkManager;
    }

    private Map<String, OreWorldData> loadWorldData(){

        Map<String, OreWorldData> worldData = new HashMap<>();

        ConfigurationSection worldsSection = config.getConfigurationSection("Worlds");
        if(worldsSection != null){
            for(String worldName : worldsSection.getKeys(false)){
                boolean isGeneratingOreInWorld = worldsSection.getBoolean("Is Generating Ore", false);
                if(isGeneratingOreInWorld){
                    int minYLevel = worldsSection.getInt(worldName + ".Minimum Y Level");
                    int maxYLevel = worldsSection.getInt(worldName + ".Maximum Y Level");
                    double spawnChance = worldsSection.getDouble(worldName + ".Spawn Chance");
                    OreWorldData oreWorldData = new OreWorldData(minYLevel, maxYLevel, spawnChance);
                    worldData.put(worldName, oreWorldData);
                }
            }
        }else{
            plugin.getLogger().warning("The custom ore file " + fileName + " does not have the \"Worlds\" section or it is empty...");
        }

        return worldData;
    }

}
