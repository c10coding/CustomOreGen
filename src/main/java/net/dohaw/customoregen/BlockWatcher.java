package net.dohaw.customoregen;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class BlockWatcher implements Listener {

    private CustomOreGenPlugin plugin;

    public BlockWatcher(CustomOreGenPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){

        Location blockLocation = e.getBlock().getLocation();
        Map<String, CustomOreManager> managers = plugin.getCustomOreManagers();
        for(Map.Entry<String, CustomOreManager> entry : managers.entrySet()){
            CustomOreManager manager = entry.getValue();
            List<Location> customOreLocations = manager.getCustomOreLocations();
            if(customOreLocations.contains(blockLocation)){
                customOreLocations.remove(blockLocation);
                manager.setCustomOreLocations(customOreLocations);
                e.setDropItems(false);
                ItemStack itemDropped = manager.getItemDroppedOnMine().clone();
                e.getPlayer().getWorld().dropItemNaturally(blockLocation, itemDropped);
                break;
            }
        }

    }

}
