package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONTools {
    /**
     * Get a value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.name")
     * @param classType The value class type (example: `String.class`, `Integer.class`)
     * @return The value at the path, or null if not found
     * @param <T> The value type
     */
    public static <T> T get(JSONObject object, String path, Class<T> classType) {
        String[] pathArr = path.split("\\.");
        Object currentVal = object.opt(pathArr[0]);
        for (int i = 1; i < pathArr.length; i++) { // Loop through array to get the value at nested path
            String key = pathArr[i];
            Object subVal = null;
            if (currentVal instanceof JSONObject) {
                // Get value from the key since currently in an object
                subVal = ((JSONObject) currentVal).opt(key);
            } else if (currentVal instanceof JSONArray) {
                // Get value from the index since current in an array
                int index = Integer.parseInt(key);
                subVal = ((JSONArray) currentVal).opt(index);
            }
            currentVal = subVal;
            if (currentVal == null) {
                // Early return since the current value is null
                break;
            }
        }
        
        if (classType.isInstance(currentVal)) {
            return classType.cast(currentVal); // Cast found value to the class type and return
        } else if (currentVal == null) {
            return null;
        } else {
            // Incorrect class type
            throw new IllegalArgumentException("Value at path '" + path + "' is not of type " + classType.getName());
        }
    }

    /**
     * Get a JSONArray value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.siblings")
     * @return The value at the path, or null if not found
     */
    public static JSONArray getJSONArray(JSONObject object, String path) {
        return get(object, path, JSONArray.class);
    }

    /**
     * Get a JSONObject value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.siblings")
     * @return The value at the path, or null if not found
     */
    public static JSONObject getJSONObject(JSONObject object, String path) {
        return get(object, path, JSONObject.class);
    }

    /**
     * Get a String value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.name")
     * @return The value at the path, or null if not found
     */
    public static String getString(JSONObject object, String path) {
        return get(object, path, String.class);
    }

    /**
     * Get an int value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.age")
     * @param defaultVal The default value if integer not found at path
     * @return The value at the path, or value of defaultVal if not found
     */
    public static int getInt(JSONObject object, String path, int defaultVal) {
        Integer integer = get(object, path, Integer.class);
        if (integer == null) return defaultVal;
        return integer;
    }
    /**
     * Get an int value of a JSONObject at a specific path
     * @param object The object to get the value from
     * @param path The path, split by periods (example: "users.0.age")
     * @return The value at the path, or 0 if not found
     */
    public static int getInt(JSONObject object, String path) {
        Integer integer = get(object, path, Integer.class);
        if (integer == null) return 0;
        return integer;
    }


    /**
     * Convert a JSONArray object to an ArrayList object
     * @param jsonArray The JSONArray object to convert
     * @return The ArrayList of the JSONArray
     * @param <T> The type of values in the array
     */
    @SuppressWarnings("unchecked") // Ignore unchecked cast
    public static <T> ArrayList<T> toArrayList(JSONArray jsonArray) {
        ArrayList<T> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            arrayList.add((T) jsonArray.get(i));
        }
        return arrayList;
    }
}
