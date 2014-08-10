var pluginDescription = {
    name: "Brainfuck",
    version: "1.0-SNAPSHOT"
};

var BrainfuckBukkit = Java.extend(Plugin, {
    onEnable: function() {
        var File = Java.type("java.io.File");
        var runtime_file = new File(plugin.getDataFolder(), "runtime");
        var runtime_base = runtime_file.getAbsolutePath();
        print("Runtime path: " + runtime_base);
        load(runtime_base + "/runtime.js");
    },
})

var plugin = new BrainfuckBukkit();
