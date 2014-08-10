package at.yawk.brainfuckloader.brainfuck;

import java.util.List;

/**
 * @author yawkat
 */
public class BrainfuckVM {
    private static final int INIT_SIZE = 128;

    private byte[] memory;
    private int pointer;

    public BrainfuckVM() {
        reset();
    }

    public synchronized void reset() {
        memory = new byte[INIT_SIZE];
        pointer = INIT_SIZE / 2;
    }

    private void grow() {
        byte[] oldMem = memory;
        memory = new byte[oldMem.length * 2];
        System.arraycopy(oldMem, 0, memory, oldMem.length / 2, oldMem.length);
        pointer += oldMem.length / 2;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public synchronized void execute(BrainfuckFile file, IOHandler io) {
        int index = 0;
        List<PositionedInstruction> instructions = file.getInstructions();
        while (index < instructions.size()) {
            PositionedInstruction positioned = instructions.get(index);
            Instruction instruction = positioned.getInstruction();
            try {
                if (instruction instanceof BaseInstruction) {
                    switch ((BaseInstruction) instruction) {
                    case INCREMENT:
                        memory[pointer]++;
                        break;
                    case DECREMENT:
                        memory[pointer]--;
                        break;
                    case MOVE_LEFT:
                        pointer--;
                        if (pointer < 0) {
                            grow();
                        }
                        break;
                    case MOVE_RIGHT:
                        pointer++;
                        if (pointer >= memory.length) {
                            grow();
                        }
                        break;
                    case OUTPUT:
                        io.out(memory[pointer]);
                        break;
                    case INPUT:
                        memory[pointer] = io.in();
                        break;
                    }
                } else if (instruction instanceof JumpInstruction) {
                    if (memory[pointer] != 0) {
                        index += ((JumpInstruction) instruction).getOffset();
                        if (index < 0) {
                            index = 0;
                        }
                    }
                } else if (instruction instanceof DebugInstruction) {
                    System.out.println(debug(((DebugInstruction) instruction).getName()));
                }
            } catch (Throwable t) {
                throw new BrainfuckException(
                        "Error at " + instruction + " " + positioned.getLine() + ":" + positioned.getCharInLine(), t);
            }
            index++;
        }
    }

    private String debug(String id) {
        StringBuilder builder = new StringBuilder("\nDebug ----- ").append(id).append('\n');
        StringBuilder top = new StringBuilder();
        StringBuilder middle = new StringBuilder();
        StringBuilder bottom = new StringBuilder();
        for (int i = -9; i < 10; i++) {
            top.append(String.format("%4x", memory[pointer + i]));
            char c = (char) memory[pointer + i];
            middle.append(String.format("%4s", Character.isLetterOrDigit(c) ? c : ""));
            bottom.append(String.format("%4s", i == 0 ? "|" : i));
        }
        builder.append(top).append('\n');
        builder.append(middle).append('\n');
        builder.append(bottom).append('\n');
        return builder.toString();
    }
}
