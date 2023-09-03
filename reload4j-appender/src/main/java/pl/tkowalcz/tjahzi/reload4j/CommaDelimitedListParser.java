package pl.tkowalcz.tjahzi.reload4j;

import org.apache.log4j.helpers.LogLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class CommaDelimitedListParser {

    public static <T> List<T> parseString(
            String commaDelimitedList,
            BiFunction<String, String, T> converter) {
        if (commaDelimitedList == null || commaDelimitedList.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<T> result = new ArrayList<>();
        String[] keyValuePairs = commaDelimitedList.split(",");
        for (String keyValuePair : keyValuePairs) {
            String[] keyAndValue = keyValuePair.split(":", 2);

            if (keyAndValue.length != 2) {
                LogLog.warn("Invalid key-value pair format: " + keyValuePair);
                continue;
            }

            String key = keyAndValue[0].trim();
            String value = keyAndValue[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                LogLog.warn("Key or value cannot be empty: " + keyValuePair);
                continue;
            }

            result.add(converter.apply(key, value));
        }

        return result;
    }
}
