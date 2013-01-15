package com.kfuntak.gwt.json.serialization.client;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class DeserializerHelper {
    public static <T> void fillMap(final Map<String, T> map, final JSONValue jsonValue, final DeserializationCallback<T> cb) {
        if (jsonValue != null && !(jsonValue instanceof JSONNull)) {
            if(!(jsonValue instanceof JSONObject)){
                throw new IncompatibleObjectException();
            }
            for(final String key:((JSONObject)jsonValue).keySet()){
                final JSONValue value = ((JSONObject)jsonValue).get(key);
                map.put(key, cb.deserialize(value));
            }
        }
    }

    public static <T> void fillCollection(final Collection<T> col, final JSONValue jsonValue, final DeserializationCallback<T> cb) {
        if (jsonValue != null && !(jsonValue instanceof JSONNull)) {
            if(!(jsonValue instanceof JSONArray)){
                throw new IncompatibleObjectException();
            }
            for(int i=0;i<((JSONArray)jsonValue).size();i++){
                final JSONValue item = ((JSONArray)jsonValue).get(i);
                col.add(cb.deserialize(item));
            }
        }
    }

    public static String getString(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONString)) {
            throw new JSONException();
        } else {
            final JSONString jsonString = (JSONString) value;
            return jsonString.stringValue();
        }
    }

    public static Character getChar(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONString)) {
            throw new JSONException();
        } else {
            final JSONString jsonString = (JSONString) value;
            try {
                return jsonString.stringValue().charAt(0);
            } catch (final IndexOutOfBoundsException e) {
                throw new JSONException();
            }
        }
    }

    public static Double getDouble(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return jsonNumber.doubleValue();
        }
    }

    public static Float getFloat(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return ((Double) jsonNumber.doubleValue()).floatValue();
        }
    }

    public static Integer getInt(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return ((Double) jsonNumber.doubleValue()).intValue();
        }
    }

    public static Long getLong(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return ((Double) jsonNumber.doubleValue()).longValue();
        }
    }

    public static Short getShort(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return ((Double) jsonNumber.doubleValue()).shortValue();
        }
    }

    public static Byte getByte(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONNumber)) {
            throw new JSONException();
        } else {
            final JSONNumber jsonNumber = (JSONNumber) value;
            return ((Double) jsonNumber.doubleValue()).byteValue();
        }
    }

    public static Boolean getBoolean(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONBoolean)) {
            throw new JSONException();
        } else {
            final JSONBoolean jsonBoolean = (JSONBoolean) value;
            return jsonBoolean.booleanValue();
        }
    }

    public static Date getDate(final JSONValue value) throws JSONException {
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        if (!(value instanceof JSONString || value instanceof JSONNumber)) {
            throw new JSONException();
        }
        if (value instanceof JSONString) {
            try {
                final long dateValue = Long.parseLong(((JSONString) value).stringValue());
                return new Date(dateValue);
            } catch (final NumberFormatException e) {
                throw new JSONException();
            }
        }
        return new Date(new Double(((JSONNumber) value).doubleValue()).longValue());
    }

    public static Object getValue(final JSONValue value) {
        if (value == null || value instanceof JSONNull) {
            return null;
        }

        if (value.isNumber() != null) {
            return getDouble(value);
        } else if (value.isBoolean() != null) {
            return getBoolean(value);
        } else if (value.isArray() != null) {
            return getArrayList(value);
        } else if (value.isString() != null) {
            return value.isString().stringValue();
        } else if (value.isObject() != null) {
            final JSONObject obj = value.isObject();
            if (obj.containsKey("class")) {
                return getObject(obj);
            } else {
                return getMap(obj);
            }
        }
        return value.toString();
    }

    private static Object getObject(final JSONObject obj) {
        final Serializer serializer = GWT.create(Serializer.class);
        return serializer.deSerialize(obj);
    }

    private static Object getMap(final JSONObject obj) {
        final Serializer serializer = GWT.create(Serializer.class);
        return serializer.deSerialize(obj, "java.util.ArrayList");
    }

    private static Object getArrayList(final JSONValue value) {
        final Serializer serializer = GWT.create(Serializer.class);
        return serializer.deSerialize(value, "java.util.HashMap");
    }

    public static Object getObject(final JSONValue value, final String elementClassName) {
        final Serializer serializer = GWT.create(Serializer.class);
        return serializer.deSerialize(value, elementClassName);
    }
}
