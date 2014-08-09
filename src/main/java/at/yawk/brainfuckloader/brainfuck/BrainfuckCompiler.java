package at.yawk.brainfuckloader.brainfuck;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.Value;

/**
 * @author yawkat
 */
public class BrainfuckCompiler {
    private boolean parseDebug = true;

    public BrainfuckFile compile(String code) {
        try {
            return compile(new StringReader(code));
        } catch (IOException e) {
            throw new RuntimeException(e); // wat
        }
    }

    public BrainfuckFile compile(Reader reader) throws IOException {
        List<PositionedInstruction> instructions = new ArrayList<>();
        Deque<LoopStart> loopStartIndexes = new ArrayDeque<>();

        int line = 1;
        int charInLine = 0;

        int c;
        while ((c = reader.read()) != -1) {
            charInLine++;
            Instruction instruction = null;
            switch (c) {
            case '+':
                instruction = BaseInstruction.INCREMENT;
                break;
            case '-':
                instruction = BaseInstruction.DECREMENT;
                break;
            case '<':
                instruction = BaseInstruction.MOVE_LEFT;
                break;
            case '>':
                instruction = BaseInstruction.MOVE_RIGHT;
                break;
            case '.':
                instruction = BaseInstruction.OUTPUT;
                break;
            case ',':
                instruction = BaseInstruction.INPUT;
                break;
            case '[':
                loopStartIndexes.offerLast(new LoopStart(instructions.size(), line, charInLine));
                break;
            case ']':
                int back = loopStartIndexes.pollLast().getInstructionIndex();
                int offset = back - instructions.size() - 1;
                instruction = new JumpInstruction(offset);
                break;
            case '\u00b0':
                if (parseDebug) {
                    char name = (char) reader.read();
                    instruction = new DebugInstruction(String.valueOf(name));
                }
                break;
            case '\n':
                line++;
                charInLine = 0;
                break;
            }
            if (instruction != null) {
                instructions.add(new PositionedInstruction(instruction, line, charInLine));
            }
        }

        if (!loopStartIndexes.isEmpty()) {
            LoopStart last = loopStartIndexes.peekLast();
            String message = "Unclosed loops at #" + last.getLine() + ":" + last.getCharInLine();
            if (loopStartIndexes.size() > 1) {
                message += " (and " + (loopStartIndexes.size() - 1) + " more)";
            }
            throw new BrainfuckException(message);
        }

        return new BrainfuckFile(instructions);
    }

    public BrainfuckFile compile(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return compile(reader);
        }
    }

    @Value
    private class LoopStart {
        int instructionIndex;
        int line;
        int charInLine;
    }
}
