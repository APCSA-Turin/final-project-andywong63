package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper class to make parsing through PokeAPI easier
 */
public class PokeAPITools {
    /**
     * Get the English value of a specific data (ex: name)<br>
     *
     * Example:
     * <pre><code>
     * namesArr = [
     *   {
     *     "language": { "name": "en" },
     *     "name": "Squirtle"
     *   }
     * ]
     * getEnglishString(namesArr, "name") -> "Squirtle"
     * </code></pre>
     *
     * @param dataArr The JSON data of the value to get
     * @param key The key of the value to get (ex: "name", "genus" for classification)
     * @return The English string, or null if not found
     */
    public static String getEnglishString(JSONArray dataArr, String key) {
        for (Object data : dataArr) {
            if (!(data instanceof JSONObject)) continue;
            JSONObject dataObj = (JSONObject) data;

            if (JSONTools.getString(dataObj, "language.name").equals("en")) {
                return dataObj.getString(key);
            }
        }
        return null;
    }

    public static int getIntIf(JSONArray dataArr, String returnKey, String ifKey, String ifKeyEquals) {
        for (Object data : dataArr) {
            if (!(data instanceof JSONObject)) continue;
            JSONObject dataObj = (JSONObject) data;

            if (JSONTools.getString(dataObj, ifKey).equals(ifKeyEquals)) {
                return JSONTools.getInt(dataObj, returnKey);
            }
        }
        return 0;
    }
}
