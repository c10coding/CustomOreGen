package net.dohaw.customoregen;

import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.ResponderFactory;
import net.dohaw.customoregen.config.CustomOreConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomOreGenCommand implements CommandExecutor {

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
                    }
                }else{
                    rFactory.sendMessage("This is already a custom ore name!");
                }
            }else if(args[0].equalsIgnoreCase("gen") && args.length == 2){

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

}
