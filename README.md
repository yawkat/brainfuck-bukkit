brainfuck-bukkit
================

Next-generation brainfuck interpreter with bukkit plugin support.

Introduction
------------

Ever felt sick of the object-oriented hell that is java? Are you missing the freedom, flexibility and scalability of modern languages when coding for your minecraft server? Don't worry, you are not alone with this problem! Say hello to brainfuck-bukkit, a modern-day brainfuck plugin system for bukkit, enabling you to finally abandon java as your primary language and learning to love the raw power of brainfuck and the brainfuck-bukkit API.

Usage
-----

The interpreter is a simple brainfuck interpreter with memory ranging from `-50.000` to `+50 000`. The API is accessed through IO (`.` and `,`).

How to use an API function:

- Print the OPCODE of the function (single byte)
- Print the arguments of the function
- Read the return value of the function (doing this also executes the function)

OPCODEs:

- `LAMBDA_INVOKE`: `0`
  **Arguments**: `<lambda address> [arg1_address [arg2_addres ...]]`
  **Returns**: address of the return value
  Invokes a lambda with the arguments at the following addresses.
- `STORE_LAMBDA`: `1`
  **Arguments**: `<accessible address> <function name>`
  **Returns**: address of the lambda
  Gets a function of a given accessible and stores it in the returned address.
- `STORE_CLASS`: `2`
  **Argmuents**: `<class name>`
  **Returns**: address of the class accessible
  Creates an accessible for the static members of the given class.
- `STORE_FIELD`: `3`
  **Arguments**: `<accessible address> <field name>`
  **Returns**: address of the field value
  Gets a field value of a given accessible and stores it in the returned address.
- `IMPORT_LAMBDA`: `4`
  **Arguments**: `<file name>`
  **Returns**: address of the lambda
  Load a brainfuck file and store it as a lambda. When this lambda is called with arguments (for example as an event handler) the arguments will be readable at execution start via standard input (2 address bytes each).
- `STORE_STRING`: `5`
  **Arguments**: `<string>`
  **Returns**: address of the string
  Stores a given string.
- `ACCESSIBLE`: `6`
  **Arguments**: `<address>`
  **Returns**: address of the accessible
  Makes an object from the given address accessible so `STORE_FIELD` and `STORE_LAMBDA` can be used on it.
