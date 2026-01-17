package com.example.tdm;

import com.example.tdm.commands.LoadoutCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;
import java.util.logging.Level;

public class TDMPlugin extends JavaPlugin {

    private static TDMPlugin instance;

    public TDMPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("TDM Plugin loading...");

        // Register the /tdm command
        getCommandRegistry().registerCommand(new LoadoutCommand());

        getLogger().at(Level.INFO).log("TDM Plugin loaded - use /tdm to join a team");
    }

    public static TDMPlugin get() {
        return instance;
    }
}
