package me.dags.blockr;

import java.util.HashMap;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class Flags extends HashMap<String, String> {

    public <T> T getFlag(String key, Function<String, T> func, T def) {
        String val = get(key);
        if (val == null) {
            return def;
        }
        return func.apply(val);
    }

    public static Flags parse(String[] args) {
        Flags flags = new Flags();
        for (int i = 0 ; i < args.length; i += 1) {
            String key = args[i];

            if (key.startsWith("--")) {
                if (i + 1 < args.length) {
                    flags.put(args[i].substring(2), args[i + 1]);
                }
            } else if (key.startsWith("-")) {
                flags.put(args[i].substring(1), "true");
            }
        }
        return flags;
    }
}
