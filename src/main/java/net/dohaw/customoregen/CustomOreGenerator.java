package net.dohaw.customoregen;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomOreGenerator extends BukkitRunnable {

    private CustomOreManager chunkManager;

    public CustomOreGenerator(CustomOreManager chunkManager){
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        for(World world : Bukkit.getWorlds()){
            String worldName = world.getName();
            OreWorldData oreWorldData = chunkManager.getOreWorldData().get(worldName);
            if(oreWorldData != null){



            }
        }
    }

}
