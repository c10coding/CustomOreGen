package net.dohaw.customoregen.config;

import net.dohaw.corelib.Config;
import net.dohaw.customoregen.CustomOreManager;
import net.dohaw.customoregen.OreWorldData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

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
        manager.setCustomOreLocations(getCustomOreLocations());
        manager.setItemDroppedOnMine(loadItemDroppedOnMine());
        manager.setOreMaterial(Material.valueOf(config.getString("Ore Material", "COAL_ORE")));
        loadWorldData(manager);

        if(isGeneratingOre){
            manager.startGenerator();
        }

        return manager;
    }

    private void loadWorldData(CustomOreManager manager){

        Map<String, OreWorldData> worldData = new HashMap<>();

        ConfigurationSection worldsSection = config.getConfigurationSection("Worlds");
        if(worldsSection != null){
            for(String worldName : worldsSection.getKeys(false)){
                int minYLevel = worldsSection.getInt(worldName + ".Minimum Y Level");
                int maxYLevel = worldsSection.getInt(worldName + ".Maximum Y Level");
                double spawnChance = worldsSection.getDouble(worldName + ".Spawn Chance");
                boolean willGenerateOre = worldsSection.getBoolean(worldName + ".Is Generating Ore");
                Material materialReplaced = Material.valueOf(worldsSection.getString(worldName + ".Material Replaced", "STONE"));
                OreWorldData oreWorldData = new OreWorldData(minYLevel, maxYLevel, spawnChance, willGenerateOre, materialReplaced);
                worldData.put(worldName, oreWorldData);
            }
        }else{
            plugin.getLogger().warning("The custom ore file " + fileName + " does not have the \"Worlds\" section or it is empty...");
        }

        manager.setOreWorldData(worldData);

    }

    private ItemStack loadItemDroppedOnMine(){
        Material mat = Material.valueOf(config.getString("Dropped Item.Material", "COAL_ORE"));
        int amountDropped = config.getInt("Dropped Item.Amount", 1);
        String displayName = config.getString("Dropped Item.Display Name", null);
        List<String> lore = config.getStringList("Dropped Item.Lore");
        ItemStack customOre = new ItemStack(mat, amountDropped);
        ItemMeta meta = customOre.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        customOre.setItemMeta(meta);
        return customOre;
    }

    public void setIsGeneratingOre(boolean value){
        config.set("Is Generating Ore", value);
        saveConfig();
    }

    public List<Location> getCustomOreLocations(){
        return (List<Location>) config.getList("Ore Locations", new ArrayList<>());
    }

    public void saveCustomOreLocation(List<Location> customOreLocations){
        config.set("Ore Locations", customOreLocations);
        saveConfig();
    }

    public void deleteFile(){
        file.delete();
    }

}
