package info.itsthesky.skriptstorage;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import info.itsthesky.skriptstorage.api.PluginUpdater;
import info.itsthesky.skriptstorage.api.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class SkriptStorage extends JavaPlugin {

    private static SkriptStorage INSTANCE;
    private static SkriptAddon SKRIPT_ADDON;
    private static Logger LOGGER;
    public static final boolean DEBUG = false;

    @Override
    public void onEnable() {

        INSTANCE = this;
        LOGGER = Bukkit.getLogger();

        if (ReflectionUtils.classExist("ch.njol.skript.Skript") && Skript.isAcceptRegistrations()) {

            LOGGER.fine("Skript found! Starting registration ...");
            SKRIPT_ADDON = Skript.registerAddon(this);
            try {
                SKRIPT_ADDON.loadClasses("info.itsthesky.skriptstorage.core");
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.severe("Wait, this is anormal. Please report the error above on the skript-storage GitHub!.");
                return;
            }
        } else {
            LOGGER.severe("Unable to find Skript or Skript's registration is closed!");
            LOGGER.severe("Can't load skript-storage properly, disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final PluginUpdater updater = PluginUpdater.create(this, "SkyCraft78", "skript-storage");
        final PluginUpdater.UpdateState state = updater.check();
        switch (state) {
            case EQUAL:
                LOGGER.info("You are on the latest skript-storage version!");
                break;
            case LOWER:
                LOGGER.warning("New update of skript-storage available: " + updater.getLatest());
                LOGGER.warning("Download it here: " + updater.getLatestVersionURL());
                break;
            case GREATER:
                LOGGER.warning("Custom, tester of nighty version detected of skript-storage!");
                LOGGER.warning("Report every bugs on the skript-storage's issues tracker!");
                break;
        }

        LOGGER.info("Loaded skript-storage successfully! Made by Sky with <3");
    }

    @Override
    public void onDisable() {
        LOGGER.info("skript-storage has been disabled!");
    }

    public static SkriptStorage getInstance() {
        return INSTANCE;
    }

    public static SkriptAddon getSkriptAddon() {
        return SKRIPT_ADDON;
    }

    public static Logger getServerLogger() {
        return LOGGER;
    }

    public static boolean isDebug() {
        return DEBUG;
    }
}
