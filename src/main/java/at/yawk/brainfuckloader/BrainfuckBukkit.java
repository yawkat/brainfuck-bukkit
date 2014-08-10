package at.yawk.brainfuckloader;

import at.yawk.brainfuckloader.brainfuck.BrainfuckException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author yawkat
 */
public class BrainfuckBukkit extends JavaPlugin implements Listener {
    private FileManager fileManager;

    @Override
    public void onEnable() {
        fileManager = new FileManager(getDataFolder().toPath());

        makeEnvironment().run(fileManager.getFile("main"));
    }

    private ExtendedEnvironment makeEnvironment() {
        return new ExtendedEnvironment(fileManager, this);
    }

    public void log(String message) {
        getLogger().info(message);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void on(String eventName, Lambda handler) {
        eventName = eventName.replace(':', '.');
        Class<?> eventClass;
        try {
            eventClass = Class.forName(eventName);
        } catch (ClassNotFoundException e) {
            try {
                eventClass = Class.forName("org.bukkit.event." + eventName);
            } catch (ClassNotFoundException f) {
                throw new BrainfuckException(e); // parent error
            }
        }

        PluginManager pmgr = getServer().getPluginManager();
        pmgr.registerEvent((Class) eventClass,
                           this,
                           EventPriority.NORMAL,
                           (l, e) -> handler.getFunction().apply(new Object[]{ e }),
                           this,
                           true);
    }
}
