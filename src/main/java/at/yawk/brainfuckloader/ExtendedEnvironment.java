package at.yawk.brainfuckloader;

import at.yawk.brainfuckloader.brainfuck.BrainfuckException;
import at.yawk.brainfuckloader.brainfuck.BrainfuckFile;
import at.yawk.brainfuckloader.brainfuck.BrainfuckVM;
import at.yawk.brainfuckloader.brainfuck.IOHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author yawkat
 */
class ExtendedEnvironment implements IOHandler {
    private static final byte INVOKE_LAMBDA = 0;
    private static final byte STORE_LAMBDA = 1;
    private static final byte STORE_CLASS = 2;
    private static final byte STORE_FIELD = 3;
    private static final byte IMPORT_LAMBDA = 4;
    private static final byte STORE_STRING = 5;
    private static final byte ACCESSIBLE = 6;

    private final FileManager fileManager;

    private final BrainfuckVM vm = new BrainfuckVM();

    private final Object[] expandedMemory = new Object[Short.MAX_VALUE];
    private short memoryCount = 0;

    private final List<Byte> commandStack = new ArrayList<>();
    private final Queue<Byte> output = new ArrayDeque<>();

    public ExtendedEnvironment(FileManager fileManager, Object initialHandle) {
        this.fileManager = fileManager;
        store(ObjectTypeTuple.object(initialHandle));
    }

    public synchronized void run(BrainfuckFile file) {
        vm.execute(file, this);
    }

    private short store(Object o) {
        expandedMemory[memoryCount] = o;
        return memoryCount++;
    }

    private Object retrieve(short address) {
        return expandedMemory[address];
    }

    private void pushOutput(int... output) {
        for (int i : output) {
            this.output.offer((byte) i);
        }
    }

    private void pushAddress(short address) {
        pushOutput(address >> 8, address & 0xFF);
    }

    @Override
    public void out(byte value) {
        commandStack.add(value);
        onOutput();
    }

    @Override
    public byte in() {
        onRequestInput();
        if (output.isEmpty()) {
            throw new BrainfuckException("No input available");
        }
        return output.poll();
    }

    private void onRequestInput() {
        if (commandStack.isEmpty()) {
            return;
        }
        byte op = commandStack.get(0);
        if (op == STORE_LAMBDA) {
            short objectAddress = readAddress(1);
            String name = readString(3, commandStack.size());
            Lambda lambda = ((ObjectTypeTuple) retrieve(objectAddress)).getLambda(name);
            pushAddress(store(lambda));
        } else if (op == STORE_CLASS) {
            String name = readString(1, commandStack.size());
            Class<?> clazz;
            try {
                clazz = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new BrainfuckException(e);
            }
            pushAddress(store(ObjectTypeTuple.statik(clazz)));
        } else if (op == STORE_FIELD) {
            short objectAddress = readAddress(1);
            String name = readString(3, commandStack.size());
            Object val = ((ObjectTypeTuple) retrieve(objectAddress)).getField(name);
            pushAddress(store(val));
        } else if (op == INVOKE_LAMBDA) {
            short lambdaAddress = readAddress(1);
            Lambda lambda = (Lambda) retrieve(lambdaAddress);
            Object[] args = new Object[lambda.getArgumentCount()];
            for (int i = 0; i < args.length; i++) {
                short argumentAddress = readAddress(3 + 2 * i);
                Object arg = retrieve(argumentAddress);
                args[i] = arg;
            }
            Object result = lambda.getFunction().apply(args);
            pushAddress(store(result));
        } else if (op == IMPORT_LAMBDA) {
            BrainfuckFile file = fileManager.getFile(readString(1, commandStack.size()));
            Lambda lambda = new Lambda(objects -> {
                for (Object arg : objects) {
                    pushAddress(store(arg));
                }
                run(file); // TODO fix memory leak when saving lambdas (for example as event listener)
                return null;
            }, 0);
            pushAddress(store(lambda));
        } else if (op == STORE_STRING) {
            pushAddress(store(readString(1, commandStack.size())));
        } else if (op == ACCESSIBLE) {
            pushAddress(store(ObjectTypeTuple.object(retrieve(readAddress(1)))));
        } else {
            throw new BrainfuckException("Unsupported opcode " + op);
        }
        commandStack.clear();
    }

    private void onOutput() {}

    private short readAddress(int at) {
        byte msb = commandStack.get(at);
        byte lsb = commandStack.get(at + 1);
        return (short) (msb << 8 | lsb & 0xff);
    }

    private String readString(int start, int end) {
        byte[] data = new byte[end - start];
        for (int i = 0; i < data.length; i++) {
            data[i] = commandStack.get(i + start);
        }
        return new String(data, StandardCharsets.UTF_8);
    }
}
