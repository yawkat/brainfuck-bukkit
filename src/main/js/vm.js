base_instructions = {
    "INCREMENT": function(vm) {
        vm.memory[vm.pointer]++;
    },
    "DECREMENT": function(vm) {
        vm.memory[vm.pointer]--;
    },
    "MOVE_LEFT": function(vm) {
        vm.pointer--;
        if (vm.pointer < 0) vm.grow();
    },
    "MOVE_RIGHT": function(vm) {
        vm.pointer++;
        if (vm.pointer >= vm.memory.length) vm.grow();
    },
    "OUTPUT": function(vm) {
        vm.io.out(vm.memory[vm.pointer]);
    },
    "INPUT": function(vm) {
        vm.memory[vm.pointer] = vm.io.in();
    }
};

function compile_file(filename) {
    var Paths = Java.type("java.nio.file.Paths");
    var path = Paths.get(filename);
    var Files = Java.type("java.nio.file.Files");

    var reader = Files.newBufferedReader(path);
    var result = compile(reader);
    reader.close();
    return result;
}

function compile(reader) {
    var instructions = [];
    var loop_stack = [];
    var c;
    while ((c = reader.read()) != -1) {
        var instruction = null;
        switch (String.fromCharCode(c)) {
        case '+':
            instruction = base_instructions["INCREMENT"];
            break;
        case '-':
            instruction = base_instructions["DECREMENT"];
            break;
        case '<':
            instruction = base_instructions["MOVE_LEFT"];
            break;
        case '>':
            instruction = base_instructions["MOVE_RIGHT"];
            break;
        case '.':
            instruction = base_instructions["OUTPUT"];
            break;
        case ',':
            instruction = base_instructions["INPUT"];
            break;
        case '[':
            loop_stack.push(instructions.length);
            break;
        case ']':
            var target = loop_stack.pop();
            var offset = target - instructions.length - 1;
            instruction = function(vm) {
                vm.index += offset;
            }
            break;
        }
        if (instruction != null) {
            instructions.push(instruction);
        }
    }
    print("Compiled " + instructions.length + " instructions.");
    return instructions;
}

// wat, why do I have to do this
Array.prototype.fill = function(val) {
    for (var i = 0; i < this.length; i++) {
        this[i] = val;
    }
}

function execute(instructions, io) {
    print("Executing " + instructions.length + " instructions")
    vm = {
        memory: Array(128),
        pointer: 64,
        index: 0,
        instructions: null,
        io: null,
        grow: function() {
            var old_memory = this.memory;
            this.memory = Array(old_memory.length);
            this.memory.fill(0);
            var shift = old_memory.length / 2;
            for (var i = 0; i < old_memory.length; i++) {
                this.memory[i + shift] = old_memory[i];
            }
            this.pointer += shift;
        }
    };
    vm.io = io;
    vm.instructions = instructions;
    vm.memory.fill(0);
    while (vm.index < instructions.length && vm.index >= 0) {
        var instruction = vm.instructions[vm.index];
        instruction(vm);
        vm.index++;
    }
}
