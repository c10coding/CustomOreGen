package net.dohaw.customoregen;

import net.dohaw.corelib.ProbabilityUtilities;
import net.dohaw.customoregen.exception.LargerMinYException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomOreGenerator extends BukkitRunnable {

    private Queue<Location> blocksNeedChanging = new LinkedBlockingQueue<>();
    
    private JavaPlugin plugin;
    private CustomOreManager chunkManager;

    public CustomOreGenerator(JavaPlugin plugin, CustomOreManager chunkManager){
        this.plugin = plugin;
        this.chunkManager = chunkManager;
        System.out.println("World data: " + chunkManager.getOreWorldData());
    }

    @Override
    public void run() {
        System.out.println("me go run run");
        for(World world : Bukkit.getWorlds()){
            OreWorldData oreWorldData = chunkManager.getOreWorldData().get(world.getName());
            if(oreWorldData != null){
                if(oreWorldData.isWillGeneratorOre()){
                    populateQueue(oreWorldData, world);
                    changeBlocks();
                }
            }
        }
    }

    private void populateQueue(OreWorldData worldData, World world){

        System.out.println("POPULATING!");

        int minY = worldData.getMinY();
        int maxY = worldData.getMaxY();

        if(minY > maxY) {
            try {
                throw new LargerMinYException(chunkManager.getCustomOreName());
            } catch (LargerMinYException e) {
                e.printStackTrace();
            }
        }

        double genChance = worldData.getSpawnChance();

        System.out.println("Loaded chunks size: " + world.getLoadedChunks().length);
        for(Chunk chunk : world.getLoadedChunks()){

            ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for(int xx = 0; xx < 16; xx++) {
                    for (int zz = 0; zz < 16; zz++) {
                        for (int yy = minY; yy < maxY; yy++) {
                            Material blockSnapshotMat = chunkSnapshot.getBlockType(xx, yy, zz);
                            if (blockSnapshotMat == Material.STONE) {

                                ProbabilityUtilities pu = new ProbabilityUtilities();
                                int numSpawnChance = (int) (genChance * 100);
                                if(numSpawnChance > 100){
                                    numSpawnChance = 50;
                                    plugin.getLogger().warning("There was an error pertaining the spawn chance of " + chunkManager.getCustomOreName() + ". It is greater than 100!");
                                }
                                pu.addChance(true, numSpawnChance);
                                pu.addChance(false, 100 - numSpawnChance);

                                boolean willGen = (boolean) pu.getRandomElement();
                                if(willGen){

                                    Location loc = new Location(world, xx, yy, zz);
                                    blocksNeedChanging.add(loc);
                                    System.out.println("ADDING LOCATION " + loc.toString());

                                }

                            }
                        }
                    }
                }
            });
        }
    }

    private void changeBlocks(){

        System.out.println("CHANGING THINGS!");
        int numBlocksChanging = chunkManager.getNumBlocksChangingPerItr();
        int blocksChanged = 0;
        while(blocksChanged != numBlocksChanging){
            Location loc = blocksNeedChanging.poll();
            assert loc != null;
            Block block = loc.getBlock();
            if(block.getType() == Material.STONE){
                block.setType(Material.GOLD_BLOCK);
                blocksChanged++;
            }
        }

    }

}
