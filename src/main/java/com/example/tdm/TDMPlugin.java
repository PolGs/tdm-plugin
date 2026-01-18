package com.example.tdm;

import com.example.tdm.commands.BackCommand;
import com.example.tdm.commands.LoadoutCommand;
import com.example.tdm.commands.ServerInfoCommand;
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

        // Register commands
        getCommandRegistry().registerCommand(new LoadoutCommand());
        getCommandRegistry().registerCommand(new BackCommand());
        getCommandRegistry().registerCommand(new ServerInfoCommand());

        getLogger().at(Level.INFO).log("TDM Plugin loaded!");
        getLogger().at(Level.INFO).log("  /menu - Open team selection");
        getLogger().at(Level.INFO).log("  /back - Return to lobby");
        getLogger().at(Level.INFO).log("  /serverinfo - Show server info");
    }

    public static TDMPlugin get() {
        return instance;
    }
}
