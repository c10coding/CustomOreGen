package net.dohaw.customoregen;

import lombok.Getter;
import net.dohaw.corelib.ResponderFactory;
import net.dohaw.customoregen.config.CustomOreConfig;
import net.dohaw.customoregen.exception.UnexpectedFileExists;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CustomOreGenCommand implements CommandExecutor {

    @Getter
    private Map<String, BukkitTask> oresInDeletionProcess = new HashMap<>();

    private CustomOreGenPlugin plugin;

    public CustomOreGenCommand(CustomOreGenPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        ResponderFactory rFactory = new ResponderFactory(sender, null);

        if(sender.hasPermission("customoregen.use")){
            if(args[0].equalsIgnoreCase("create") && args.length == 2){
                String customOreName = args[1];
                if(!plugin.isCustomOre(customOreName)){
                    boolean hasBeenCreated = createNewCustomOre(customOreName);
                    if(!hasBeenCreated){
                       rFactory.sendMessage("&cThere has been an error trying to create this ore. Please contact an administrator...");
                    }else{
                        rFactory.sendMessage("You have created a new ore!");
                    }
                }else{
                    rFactory.sendMessage("This is already a custom ore name!");
                }
            }else if(args[0].equalsIgnoreCase("gen") && args.length == 3){

                String customOreName = args[1];
                String boolArg = args[2];

                if(plugin.isCustomOre(customOreName)){
                    boolean bool = Boolean.parseBoolean(boolArg);
                    CustomOreManager customOreManager = plugin.getCustomOreManagers().get(customOreName);
                    customOreManager.startOrHaltGeneration(bool);
                    if(bool){
                        rFactory.sendMessage("This ore will now start to be generated throughout the worlds defined in the config!");
                    }else{
                        rFactory.sendMessage("This ore has halted generation!");
                    }
                }else{
                    rFactory.sendMessage("This is not a valid custom ore!");
                }
            }else if(args[0].equalsIgnoreCase("list") && args.length == 1) {
                Set<String> customOresNames = plugin.getCustomOreManagers().keySet();
                rFactory.sendCenteredMessage("&lCustom Ores:");
                for (String s : customOresNames) {
                    rFactory.sendMessage("&e&l- " + s);
                }
            }else if(args[0].equalsIgnoreCase("delete") && args.length >= 2) {
                String customOreArg = args[1];
                if (plugin.isCustomOre(customOreArg)) {
                    if (args.length == 3) {
                        String decision = args[2];
                        if (decision.equalsIgnoreCase("c") || decision.equalsIgnoreCase("cancel")) {
                            if (oresInDeletionProcess.containsKey(customOreArg)) {
                                BukkitTask deletionTask = oresInDeletionProcess.get(customOreArg);
                                deletionTask.cancel();
                                rFactory.sendMessage("You have halted the deletion process for this ore!");
                                rFactory.sendMessage("To start generating this ore again, run this command: &6/cog gen " + customOreArg + " true");
                            } else {
                                rFactory.sendMessage("This ore isn't being deleted at the moment!");
                            }
                        } else {
                            rFactory.sendMessage("Maybe you meant to type \"c\" or \"cancel\"?");
                        }
                    } else {
                        startOreDeletionProcess(customOreArg);
                        rFactory.sendMessage("Starting the removal of these ores! The config file will be deleted once all the ores have been removed from the world!");
                        rFactory.sendMessage("If you restart the server, you'll have to run this command again to continue the deletion process...");
                    }
                } else {
                    rFactory.sendMessage("This is not a valid custom ore!");
                }
            }else if(args[0].equalsIgnoreCase("reload")){
                /*
                    I have to do this because if i try saving the locations before using plugin.loadCustomOreManagers, it'll overwrite anything I am manually changing in the config file because it has different contents in memory that it's saving.
                 */
                final Map<String, CustomOreManager> MANAGERS = new HashMap<>(plugin.getCustomOreManagers());
                plugin.loadCustomOreManagers();
                for(CustomOreManager manager : plugin.getCustomOreManagers().values()){
                    CustomOreManager correspondingManager = MANAGERS.get(manager.getCustomOreName());
                    manager.setCustomOreLocations(correspondingManager.getCustomOreLocations());
                }
            }else if(args[0].equalsIgnoreCase("help")){
                rFactory.sendMessage("Commands for this plugin: ");
                rFactory.sendMessage("&6/cog create <ore name>&f - Creates a new custom ore.");
                rFactory.sendMessage("&6/cog reload&f - Reloads the plugin");
                rFactory.sendMessage("&6/cog gen <ore name> <true | false>&f - Either starts or stops the generation of ores.");
                rFactory.sendMessage("&6/cog delete <ore name> <optional{\"cancel\" or \"c\"}>&f - Slowly starts removing the ore from the world.");
            }
        }
        return false;
    }

    private boolean createNewCustomOre(String customOreName){

        File file = new File(plugin.getDataFolder() + File.separator + CustomOreGenPlugin.CUSTOM_ORE_FOLDER_NAME + File.separator + customOreName + ".yml");
        if(!file.exists()){

            InputStream inputStream = plugin.getResource("defaultCustomOre.yml");
            if(inputStream != null){

                boolean fileHasBeenCreated = false;
                try {
                    fileHasBeenCreated = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(fileHasBeenCreated){

                    try {
                        copyInputStreamToFile(inputStream, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    CustomOreConfig config = new CustomOreConfig(customOreName, file);
                    CustomOreManager customOreManager = config.loadChunkManager();
                    plugin.createNewCustomOre(customOreName, customOreManager);

                    return true;
                }

            }else{
                plugin.getLogger().severe("The resource defaultCustomOre.yml has not been found! Can't create new custom ore!");
            }

        }else{
            try {
                throw new UnexpectedFileExists(file.getName());
            } catch (UnexpectedFileExists unexpectedFileExists) {
                unexpectedFileExists.printStackTrace();
            }
        }

        return false;

    }

    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

    private void startOreDeletionProcess(String customOreArg){

        CustomOreManager customOreManager = plugin.getCustomOreManagers().get(customOreArg);
        customOreManager.startOrHaltGeneration(false);

        // We want to keep saving the locations even if the contents are being removed because if the server restarts while the deletion process is going, it can pick up where it left off if the deletion process is started back up
        customOreManager.startCustomOreLocationSaver();
        List<Location> customOreLocations = customOreManager.getCustomOreLocations();

        BukkitTask deletionProcess = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int maxNumBlockEdits = 200;
            int numBlocksEdited = 0;
            if (!customOreLocations.isEmpty()) {
                Iterator<Location> itr = customOreLocations.iterator();
                while (itr.hasNext()) {
                    if (numBlocksEdited == maxNumBlockEdits) return;
                    Location loc = itr.next();
                    String worldName = loc.getWorld().getName();
                    OreWorldData worldData = customOreManager.getOreWorldData().get(worldName);
                    loc.getBlock().setType(worldData.getMaterialReplaced());
                    numBlocksEdited++;
                    itr.remove();
                }
            }else{

                BukkitTask task = oresInDeletionProcess.get(customOreArg);
                task.cancel();
                customOreManager.getConfig().deleteFile();
                plugin.getCustomOreManagers().remove(customOreArg);
                plugin.getLogger().info("The deletion process for the ore " + customOreArg + " has finished!");

            }

            /*
                Have to remove the marking in case you make another custom ore with the same name.
             */
            for(World world : Bukkit.getWorlds()){
                for(Chunk chunk : world.getLoadedChunks()){
                    if(customOreManager.isChunkMarked(chunk)){
                        customOreManager.unmarkChunk(chunk);
                    }
                }
            }

        }, 0, 100);
        oresInDeletionProcess.put(customOreArg, deletionProcess);

    }

}
