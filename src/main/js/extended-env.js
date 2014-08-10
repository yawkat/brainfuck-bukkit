include("vm.js");

function ExtendedEnv(path_prefix, init_obj) {
    var env = {
        _output: [],
        _extended_memory: [_make_accessible(init_obj, init_obj.getClass())],
        _input: [],
        _store: function(obj) {
            return this._extended_memory.push(obj) - 1;
        },
        _retrieve: function(address) {
            print("Retr " + address + " = " + this._extended_memory[address]);
            return this._extended_memory[address];
        },
        _push_output: function() {
            this._output.push.apply(this._output, arguments);
        },
        _push_address: function(address) {
            this._push_output(address >> 8, address & 0xFF);
        },
        _read_address: function(at) {
            var msb = this._input[at];
            var lsb = this._input[at + 1];
            return (msb << 8) | lsb;
        },
        _read_string: function(from, to) {
            var result = "";
            for (var i = from; i < to; i++) {
                result += String.fromCharCode(this._input[i]);
            }
            return result;
        },

        run: function(file) {
            execute(file, this);
        },
        in: function() {
            if (this._input.length == 0) {
                return this._output.shift();
            }
            
            print("HLI " + this._input[0]);
            switch (this._input[0]) {
            case 0: // LAMBDA_INVOKE
                var lambda_address = this._read_address(1);
                print(lambda_address + " " + this._extended_memory);
                var lambda = this._retrieve(lambda_address);
                var args = [];
                for (var i = 0; i < lambda.argument_count; i++) {
                    var ref = this._read_address(3 + i * 2);
                    var arg = this._retrieve(ref);
                    args.push(arg);
                    print("Arg #" + i + ": *" + ref + " = " + arg);
                }
                var result = lambda.invoke(args);
                this._push_address(this._store(result));
                break;
            case 1: // STORE_LAMBDA
                var obj_address = this._read_address(1);
                var func_name = this._read_string(3, this._input.length);
                var obj = this._retrieve(obj_address);
                var lambda = obj.get_lambda(func_name);
                this._push_address(this._store(lambda));
                break;
            case 2: // STORE_CLASS
                var class_name = this._read_string(1, this._input.length);
                var Class = Java.type("java.lang.Class");
                var type = Class.forName(class_name.replace(":", "."));
                var accessible = _make_accessible(null, type);
                this._push_address(this._store(lambda));
                break;
            case 3: // STORE_FIELD
                var obj_address = this._read_address(1);
                var field_name = this._read_string(3, this._input.length);
                var obj = this._retrieve(obj_address);
                var val = obj.get_field(field_name);
                this._push_address(this._store(val));
                break;
            case 4: // IMPORT_LAMBDA
                var file_name = plugin.getDataFolder().getAbsolutePath() + "/" + this._read_string(1, this._input.length);
                var file;
                try {
                    file = compile_file(file_name);
                } catch(e) {
                    try {
                        file = compile_file(file_name + ".bf");
                    } catch(f) {
                        file = compile_file(file_name + ".brainfuck");
                    }
                }
                var lambda = {
                    argument_count: 0,
                    invoke: function(args) {
                        for (var i = 0; i < args.length; i++) {
                            env._push_address(env._store(args[i]));
                        }
                        env.run(file);
                    }
                }
                var address = this._store(lambda);
                print("Stored " + file_name + " at *" + address);
                this._push_address(address);
                break;
            case 5: // STORE_STRING
                this._push_address(this._store(this._read_string(1, this._input.length)));
                break;
            case 6: // ACCESSIBLE
                var obj = this._retrieve(this._read_address(1));
                this._push_address(this._store(_make_accessible(obj, obj.getClass())));
                break;
            }
            this._input = [];
            return this._output.shift();
        },
        out: function(val) {
            this._input.push(val);
        },
    };
    return env;
}

function _make_accessible(obj, type) {
    var accessible = {
        get_lambda: function(name) {
            var oride = overrides[obj];
            if (oride) {
                var ovr = oride[name];
                if (ovr) {
                    var lambda = {
                        argument_count: ovr.length,
                        invoke: function(args) {
                            return ovr.apply(obj, args);
                        },
                        toString: function() {
                            return type + "#" + name + " (" + obj + ")";
                        }
                    }
                    return lambda;
                }
            }

            var type0 = type;
            var found = null;
            while (found == null && type0 != null) {
                var methods = type0.getMethods();
                for (var i = 0; i < methods.length; i++) {
                    var method = methods[i];
                    if (method.getName() == name) {
                        found = method;
                        break;
                    }
                }
                type0 = type0.getSuperclass();
            }
            var lambda = {
                argument_count: found.getParameterCount(),
                invoke: function(args) {
                    return found.invoke(obj, args);
                },
                toString: function() {
                    return type + "#" + found.getName() + " (" + obj + ")"
                }
            };
            return lambda;
        },
        get_field: function(name) {
            var oride = overrides[obj];
            if (oride) {
                var ovr = oride[name];
                if (ovr) return ovr;
            }

            var type0 = type;
            while (true) {
                var fields = type0.getFields();
                for (var i = 0; i < fields.length; i++) {
                    var field = fields[i];
                    if (field.getName() == name) {
                        return field.get(obj);
                    }
                }
                type0 = type0.getSuperclass();
            }
        },
        toString: function() {
            return type + " (" + obj + ")";
        }
    };
    return accessible;
}
