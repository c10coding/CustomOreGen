package net.dohaw.customoregen;

import lombok.Getter;
import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import net.dohaw.customoregen.config.CustomOreConfig;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class CustomOreGenPlugin extends JavaPlugin {

    public static final String CUSTOM_ORE_FOLDER_NAME = "customOres";

    private CustomOreGenCommand mainCommand;

    @Getter
    private Map<String, CustomOreManager> customOreManagers = new HashMap<>();

    @Override
    public void onEnable() {

        CoreLib.setInstance(this);
        JPUtils.validateFolders(
            new HashMap<String, Object>(){{
                put(CUSTOM_ORE_FOLDER_NAME, getDataFolder());
            }}
        );
        loadCustomOreManagers();

        this.mainCommand = new CustomOreGenCommand(this);
        JPUtils.registerCommand("customoregen", mainCommand);
        JPUtils.registerEvents(new BlockWatcher(this));

    }

    @Override
    public void onDisable() {
        for(CustomOreManager manager : customOreManagers.values()){
            manager.getConfig().saveCustomOreLocation(manager.getCustomOreLocations());
        }
    }

    private void loadCustomOreManagers(){

        getLogger().info("Loading custom ore managers!");
        File folder = new File(getDataFolder(), CUSTOM_ORE_FOLDER_NAME);
        File[] fileNamesInFolder = folder.listFiles();

        if(fileNamesInFolder != null){
            for(File file : fileNamesInFolder){
                String fileName = file.getName();
                String customOreName = fileName.replace(".yml", "");
                System.out.println("Loaded Custom Ore " + customOreName);
                CustomOreConfig config = new CustomOreConfig(customOreName, file);
                customOreManagers.put(customOreName, config.loadChunkManager());
            }
        }else{
            getLogger().info("The custom ores folder has no files in it. This is usually nothing to worry about...");
        }

        getLogger().info("Custom ore managers have been loaded!");


    }

    public boolean isCustomOre(String customOreName){
        return customOreManagers.containsKey(customOreName);
    }

    public void createNewCustomOre(String customOreName, CustomOreManager customOreManager){
        customOreManagers.put(customOreName, customOreManager);
    }

}
