package net.dohaw.customoregen;

import lombok.Getter;
import lombok.Setter;
import net.dohaw.corelib.helpers.MathHelper;
import net.dohaw.customoregen.config.CustomOreConfig;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CustomOreManager {

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

    @Getter @Setter
    private int numBlocksChangingPerItr;

    @Getter
    private JavaPlugin plugin;

    public CustomOreManager(JavaPlugin plugin, String customOreName, CustomOreConfig config){
        this.plugin = plugin;
        this.config = config;
        this.customOreName = customOreName;
    }

    public void startGenerator(){
        this.generator = new CustomOreGenerator(plugin, this);
        // Do this so that generating doesn't happen at the exact same interval
        int interval = MathHelper.getRandomInteger(1200, 600);
        generator.runTaskTimer(plugin, 2L, 20);
    }

    public void startOrHaltGeneration(boolean decision){
        if(decision){
            startGenerator();
        }else{
            if(generator != null){
                this.generator.cancel();
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

}
