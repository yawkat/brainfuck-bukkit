function include(file) {
    load(__DIR__ + file);
}

print("Loading runtime...");

include("extended-env.js");

print("Runtime loaded; Preparing extended environment...");

var root_dir = plugin.getDataFolder().getAbsolutePath();

overrides = {}
overrides[plugin] = {
    on: function(event_name, lambda) {
        event_name = event_name.replace(":", ".");
        var Class = Java.type("java.lang.Class");
        var event_type;
        try {
            event_type = Class.forName(event_name);
        } catch(e) {
            event_type = Class.forName("org.bukkit.event." + event_name);
        }
        var EventPriority = Java.type("org.bukkit.event.EventPriority");
        plugin.getServer().getPluginManager().registerEvent(event_type, plugin, EventPriority.NORMAL, function(l, e) {
            lambda.invoke([e]);
        }, plugin, true);
    },
    log: function(str) {
        plugin.getLogger().info(str);
    }
}

var main_env = ExtendedEnv(root_dir + "/", plugin);

print("Compiling main...");

var main_script = compile_file(root_dir + "/main.bf");

print("Executing main...");

main_env.run(main_script);
