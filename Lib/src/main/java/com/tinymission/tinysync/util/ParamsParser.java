package com.tinymission.tinysync.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Parses a URL-encoded params string into a JSON object.
 */
public class ParamsParser {

    private String _params;

    public ParamsParser(String params) {
        _params = params;
    }

    private JsonElement stringToPrimitive(String s) {
        if (s.equalsIgnoreCase("true"))
            return new JsonPrimitive(true);
        if (s.equalsIgnoreCase("false"))
            return new JsonPrimitive(false);
        return new JsonPrimitive(s);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        for (String param: Splitter.on("&").split(_params)) {
            String[] comps = Iterables.toArray(Splitter.on("=").split(param), String.class);
            if (comps.length == 2) {
                String key1 = comps[0];
                String value = comps[1];
                if (key1.contains("[")) {
                    String key2 = key1.substring(key1.indexOf("[")+1, key1.indexOf("]"));
                    key1 = key1.substring(0, key1.indexOf("["));
                    JsonObject subObject = root.getAsJsonObject(key1);
                    if (subObject == null) {
                        subObject = new JsonObject();
                        root.add(key1, subObject);
                    }
                    subObject.add(key2, stringToPrimitive(value));
                }
                else { // not nested
                    root.add(key1, stringToPrimitive(value));
                }
            }
        }

        return root;
    }

}
