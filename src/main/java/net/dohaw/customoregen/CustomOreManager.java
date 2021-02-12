package net.dohaw.customoregen;

import lombok.Getter;
import lombok.Setter;
import net.dohaw.customoregen.config.CustomOreConfig;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class CustomOreManager {

    @Getter @Setter
    private Map<String, OreWorldData> oreWorldData = new HashMap<>();

    @Getter
    private CustomOreConfig config;

    @Getter
    private String customOreName;

    public CustomOreManager(String customOreName, CustomOreConfig config){
       this.config = config;
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
