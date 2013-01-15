package com.kfuntak.gwt.json.serialization.client;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class SerializerHelper {
    public static JSONValue getCollection(final Collection<?> collection, final SerializationCallback cb) {
        if (collection == null) {
            return JSONNull.getInstance();
        }

        final JSONArray result = new JSONArray();
        int idx = 0;
        for (final Object item : collection) {
            final JSONValue value = cb.serialize(item);
            result.set(idx++, value);
        }
        return result;
    }

    public static JSONValue getMap(final Map<String, ?> map, final SerializationCallback cb) {
        if (map == null) {
            return JSONNull.getInstance();
        }

        final JSONObject result = new JSONObject();
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                final JSONValue value = cb.serialize(entry.getValue());
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    public static JSONValue getString(final String string) {
        if (string == null) {
            return JSONNull.getInstance();
        }
        return new JSONString(string);
    }

    public static JSONValue getBoolean(final Boolean boolValue) {
        if (boolValue == null) {
            return JSONNull.getInstance();
        }
        return JSONBoolean.getInstance(boolValue);
    }

    public static JSONValue getNumber(final Number number) {
        if (number == null) {
            return JSONNull.getInstance();
        }
        return new JSONNumber(number.doubleValue());
    }

    public static JSONValue getChar(final Character character) {
        if (character == null) {
            return JSONNull.getInstance();
        }
        return new JSONString(new String(new char[]{character}));
    }

    public static JSONValue getDate(final Date date) {
        if (date == null) {
            return JSONNull.getInstance();
        }
        return new JSONNumber(date.getTime());
    }

    public static JSONValue getValue(final Object o) {
        if (o == null) {
            return JSONNull.getInstance();
        }
        final String typeName = o.getClass().getName();
        if (typeName.equals("java.lang.String")) {
            return getString((String) o);
        } else if (typeName.equals("java.lang.Boolean")) {
            return getBoolean((Boolean) o);
        } else if (typeName.equals("java.lang.Date")) {
            return getDate((Date) o);
        } else if (typeName.equals("java.lang.Character")) {
            return getChar((Character)o);
        } else {
            final Serializer serializer = GWT.create(Serializer.class);
            final ObjectSerializer ser = serializer.getObjectSerializer(typeName);
            if (ser != null) {
                return serializer.serializeToJson(o);
            }
        }
        return getString(o.toString());
    }
}
