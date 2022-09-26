package com.fontys.vistaapplication.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyConstructor {

    HashMap<String, Object> _datapoints;

    public PropertyConstructor() {
        _datapoints = new HashMap<>();
    }

    /**
     * Convert properties into JSON Object
     *
     * @return JSON Object
     */
    public String construct() {
        JSONObject object = new JSONObject();

        for (Map.Entry<String, Object> entry : _datapoints.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            try {

                if(value instanceof List) {
                    JSONArray data = new JSONArray();

                    for(Object obj : (List)value) {
                        data.put(obj.toString());
                    }

                    value = data;
                }

                object.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //System.out.println(object.toString());
        return object.toString();
    }

    /**
     * Set value for a specific property
     *
     * @param key   - JSON property name
     * @param value - JSON property value
     * @return the constructor
     */
    public PropertyConstructor addProperty(String key, Object value) {
        if (_datapoints.containsKey(key)) _datapoints.remove(key);

        _datapoints.put(key, value);
        return this;
    }

    /**
     * Get value from property
     *
     * @param key - JSON property name
     * @return Property value
     */
    public Object getProperty(String key) {
        if (!_datapoints.containsKey(key)) return null; //Key not found!
        return _datapoints.get(key);
    }

}
