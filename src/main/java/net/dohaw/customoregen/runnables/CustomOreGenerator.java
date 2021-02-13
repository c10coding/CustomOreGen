package net.dohaw.customoregen.runnables;

import net.dohaw.corelib.ProbabilityUtilities;
import net.dohaw.customoregen.CustomOreManager;
import net.dohaw.customoregen.OreWorldData;
import net.dohaw.customoregen.exception.LargerMinYException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CustomOreGenerator extends BukkitRunnable {

    private JavaPlugin plugin;
    private CustomOreManager chunkManager;

    public CustomOreGenerator(JavaPlugin plugin, CustomOreManager chunkManager){
        this.plugin = plugin;
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        for(World world : Bukkit.getWorlds()){
            OreWorldData oreWorldData = chunkManager.getOreWorldData().get(world.getName());
            if(oreWorldData != null){
                if(oreWorldData.isWillGeneratorOre()){
                    makeChangesToWorld(oreWorldData, world);
                }
            }
        }
    }

    private void makeChangesToWorld(OreWorldData worldData, World world){

        int minY = worldData.getMinY();
        int maxY = worldData.getMaxY();

        if(minY > maxY) {
            try {
                throw new LargerMinYException(chunkManager.getCustomOreName());
            } catch (LargerMinYException e) {
                e.printStackTrace();
            }
            this.cancel();
        }

        double genChance = worldData.getSpawnChance();

        int maxChunksToCheck = 10;
        int numChunksMarked = 0;

        for(Chunk chunk : world.getLoadedChunks()){

            double chunkSpawnChance = chunkManager.getChanceOreIsInChunk();
            int numChunkSpawnChance = (int) (chunkSpawnChance * 100);
            ProbabilityUtilities pu = new ProbabilityUtilities();
            pu.addChance(true, numChunkSpawnChance);
            pu.addChance(false, 100 - numChunkSpawnChance);

            boolean willSpawnInChunk = (boolean) pu.getRandomElement();

            if(!willSpawnInChunk) continue;
            if(numChunksMarked == maxChunksToCheck) return;

            if(!chunkManager.isChunkMarked(chunk)) {

                numChunksMarked++;
                ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

                List<Location> locationsNeedChanging = new ArrayList<>();

                int chunkSnapshotX = chunkSnapshot.getX();
                int chunkSnapshotZ = chunkSnapshot.getZ();
                int chunkX = chunkSnapshotX * 16;
                int chunkZ = chunkSnapshotZ * 16;

                for (int xx = 0; xx < 16; xx++) {
                    for (int zz = 0; zz < 16; zz++) {
                        for (int yy = minY; yy < maxY; yy++) {
                            Material blockSnapshotMat = chunkSnapshot.getBlockType(xx, yy, zz);
                            if (blockSnapshotMat == worldData.getMaterialReplaced()) {

                                ProbabilityUtilities pu1 = new ProbabilityUtilities();
                                int numSpawnChance = (int) (genChance * 100);
                                if (numSpawnChance > 100) {
                                    numSpawnChance = 50;
                                    plugin.getLogger().warning("There was an error pertaining the spawn chance of " + chunkManager.getCustomOreName() + ". It is greater than 100!");
                                }
                                pu1.addChance(true, numSpawnChance);
                                pu1.addChance(false, 100 - numSpawnChance);

                                boolean willGen = (boolean) pu1.getRandomElement();
                                if (willGen) {
                                    int blockX = chunkX + xx;
                                    int blockZ = chunkZ + zz;
                                    Location loc = new Location(world, blockX, yy, blockZ);
                                    locationsNeedChanging.add(loc);
                                }

                            }
                        }
                    }
                }

                changeBlocks(locationsNeedChanging);
                chunkManager.markChunk(chunk);

            }

        }
    }

    private void changeBlocks(List<Location> blocksToChange){
        for(Location loc : blocksToChange){
            Block block = loc.getBlock();
            chunkManager.getCustomOreLocations().add(loc);
            block.setType(chunkManager.getOreMaterial());
        }
    }

}
