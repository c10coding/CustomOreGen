package net.dohaw.customoregen;

import lombok.Getter;
import lombok.Setter;
import net.dohaw.corelib.helpers.MathHelper;
import net.dohaw.customoregen.config.CustomOreConfig;
import net.dohaw.customoregen.runnables.CustomOreGenerator;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOreManager {

    @Getter @Setter
    private List<Location> customOreLocations = new ArrayList<>();

    @Getter @Setter
    private boolean isGeneratingOre;

    @Getter @Setter
    private Map<String, OreWorldData> oreWorldData = new HashMap<>();

    @Getter
    private CustomOreConfig config;

    @Getter
    private String customOreName;

    @Getter
    private CustomOreGenerator generator;
    @Getter
    private BukkitTask customOreChecker, customOreLocationSaver;

    @Getter @Setter
    private double chanceOreIsInChunk;

    @Getter @Setter
    private ItemStack itemDroppedOnMine;

    @Getter @Setter
    private Material oreMaterial;

    @Getter
    private JavaPlugin plugin;

    public CustomOreManager(JavaPlugin plugin, String customOreName, CustomOreConfig config){
        this.plugin = plugin;
        this.config = config;
        this.customOreName = customOreName;
        startCustomOreLocationSaver();
        startCustomOreLocationChecker();
    }

    public void startOrHaltGeneration(boolean decision){
        if(decision){
            startGenerator();
            startCustomOreLocationChecker();
            startCustomOreLocationSaver();
        }else{
            if(generator != null){
                this.generator.cancel();
                this.customOreLocationSaver.cancel();
                this.customOreChecker.cancel();
            }
        }
        config.setIsGeneratingOre(decision);
    }

    public void markChunk(Chunk chunk){
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(NamespacedKey.minecraft("marked_" + customOreName), PersistentDataType.STRING, " ");
    }

    public boolean isChunkMarked(Chunk chunk){
       PersistentDataContainer pdc = chunk.getPersistentDataContainer();
       return pdc.has(NamespacedKey.minecraft("marked_" + customOreName), PersistentDataType.STRING);
    }

    public void startGenerator(){
        this.generator = new CustomOreGenerator(plugin, this);
        // Do this so that generating doesn't happen at the exact same interval
        int interval = MathHelper.getRandomInteger(1200, 600);
        generator.runTaskTimer(plugin, 2L, interval);
    }

    public void startCustomOreLocationSaver(){
        //Periodically save the custom ore locations in case the server crashes and doesn't have time to save.
        this.customOreLocationSaver = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            config.saveCustomOreLocation(customOreLocations);
            // every 2 min
        }, 2400, 2400L);
    }

    private void startCustomOreLocationChecker(){
        this.customOreChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            customOreLocations.removeIf(loc -> loc.getBlock().getType() != oreMaterial);
        }, 0L, 600L);
    }

}
