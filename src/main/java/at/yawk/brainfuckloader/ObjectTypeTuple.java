package at.yawk.brainfuckloader;

import at.yawk.brainfuckloader.brainfuck.BrainfuckException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ObjectTypeTuple {
    private final Object object;
    private final Class<?> type;

    public static ObjectTypeTuple object(Object o) {
        return new ObjectTypeTuple(o, o.getClass());
    }

    public static ObjectTypeTuple statik(Class<?> clazz) {
        return new ObjectTypeTuple(null, clazz);
    }

    public Lambda getLambda(String name) {
        Method found = deep(type, t -> {
            for (Method method : t.getMethods()) {
                if (method.getName().equals(name)) {
                    return Optional.of(method);
                }
            }
            return Optional.empty();
        });
        if (found == null) {
            throw new BrainfuckException("Method not found: " + type + "#" + name);
        }
        return new Lambda(objects -> {
            try {
                System.out.println("Invoke " + found + " " + Arrays.toString(objects) + " " + object);
                Object res = found.invoke(object, objects);
                System.out.println("Result: " + res);
                return res;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new BrainfuckException(e);
            }
        }, found.getParameterCount());
    }

    public Object getField(String name) {
        try {
            Field field = deep(type, t -> {
                try {
                    return Optional.of(t.getField(name));
                } catch (NoSuchFieldException e) {
                    return Optional.empty();
                }
            });
            if (field == null) {
                throw new BrainfuckException("Field not found: " + type + "#" + name);
            }
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new BrainfuckException(e);
        }
    }

    private static <T> T deep(Class<?> top, Function<Class<?>, Optional<T>> getter) {
        if (top == null) {
            return null;
        }
        return getter.apply(top).orElseGet(() -> deep(top.getSuperclass(), getter));
    }
}
