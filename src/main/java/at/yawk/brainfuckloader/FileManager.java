package at.yawk.brainfuckloader;

import at.yawk.brainfuckloader.brainfuck.BrainfuckCompiler;
import at.yawk.brainfuckloader.brainfuck.BrainfuckException;
import at.yawk.brainfuckloader.brainfuck.BrainfuckFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class FileManager {
    private final Path root;

    private final Map<String, BrainfuckFile> cache = new HashMap<>();

    public synchronized BrainfuckFile getFile(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        Path path = root.resolve(name);
        if (!Files.exists(path)) {
            path = root.resolve(name + ".bf");
        }
        if (!Files.exists(path)) {
            path = root.resolve(name + ".brainfuck");
        }
        if (!Files.exists(path)) {
            throw new BrainfuckException("File not found: " + name + " in " + root);
        }

        BrainfuckFile result;
        try {
            result = new BrainfuckCompiler().compile(path);
        } catch (IOException e) {
            throw new BrainfuckException(e);
        }
        cache.put(name, result);
        return result;
    }
}
