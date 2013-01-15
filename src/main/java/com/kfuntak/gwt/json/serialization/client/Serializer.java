package com.kfuntak.gwt.json.serialization.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class Serializer {

    private static Map<String, ObjectSerializer> SERIALIZABLE_TYPES;

    private static Map<String, ObjectSerializer> serializableTypes() {
        if (SERIALIZABLE_TYPES == null) {
            SERIALIZABLE_TYPES = new HashMap<String, ObjectSerializer>();
        }
        return SERIALIZABLE_TYPES;
    }

    protected void addObjectSerializer(final String name, final ObjectSerializer obj) {
        serializableTypes().put(name, obj);
    }

    protected ObjectSerializer getObjectSerializer(final String name) {
        if (name.equals("java.util.ArrayList")) {
            return new ArrayListSerializer();
        } else if (name.equals("java.util.HashMap")) {
            return new HashMapSerializer();
        }

        if(serializableTypes().containsKey(name)){
            return serializableTypes().get(name);
        } else {
            throw new SerializationException("Can't find object serializer for " + name);
        }
    }

    private String extractClassName(final JSONValue jsonValue) {
        final JSONObject obj = jsonValue.isObject();
        if (obj != null) {
            if (obj.containsKey("class") && obj.get("class").isString() != null) {
                return obj.get("class").isString().stringValue();
            }
        }
        return null;
    }

    protected Serializer() {
    }

    public String serialize(final Object pojo) {
        return serializeToJson(pojo).toString();
    }

    public JSONValue serializeToJson(final Object pojo) {
        if (pojo == null) {
            return null;
        }

        final String name = pojo.getClass().getName();
        return getObjectSerializer(name).serializeToJson(pojo);
    }

    public Object deSerialize(final JSONValue jsonValue, String className) throws JSONException {
        final String serializeClassName = extractClassName(jsonValue);
        if(serializeClassName != null && !serializeClassName.equals(className)){
            className = serializeClassName;
        }

        if (className == null) {
            throw new IllegalArgumentException("Json string must contain \"class\" key.");
        }

        return getObjectSerializer(className).deSerialize(jsonValue);
    }

    public Object deSerialize(final String jsonString, final String className) throws JSONException {
        final JSONValue jsonValue = JSONParser.parseLenient(jsonString);
        return deSerialize(jsonValue, className);
    }

    public Object deSerializeArray(final String jsonString, final String className) throws JSONException {
        final JSONValue jsonValue = JSONParser.parseLenient(jsonString);
        return new ArrayListSerializer(className).deSerialize(jsonValue);
    }

    public Object deSerializeArray(final JSONValue jsonValue, final String className) throws JSONException {
        return new ArrayListSerializer(className).deSerialize(jsonValue);
    }

    public Object deSerializeMap(final String jsonString, final String className) throws JSONException {
        final JSONValue jsonValue = JSONParser.parseLenient(jsonString);
        return new HashMapSerializer(className).deSerialize(jsonValue);
    }

    public Object deSerializeMap(final JSONValue jsonValue, final String className) throws JSONException {
        return new HashMapSerializer(className).deSerialize(jsonValue);
    }


    public Object deSerialize(final String jsonString) {
        return deSerialize(jsonString, null);
    }

    public Object deSerialize(final JSONValue jsonValue) throws JSONException {
        return deSerialize(jsonValue, null);
    }

    public static <T> T marshall(final String data, final String typeString) {
        return marshall(data, typeString, null);
    }

    public static <T> T marshall(final String data) {
        return marshall(data, null, null);
    }

    public static <T> T marshall(final String data, final String typeString, final T defaultValue) {
        if(GWT.isClient() && data != null && !data.isEmpty()){
            final Serializer serializer = new Serializer();
            @SuppressWarnings("unchecked")
			final T object = (T)serializer.deSerialize(data, typeString);
            if (object == null) {
                return defaultValue;
            } else {
                return object;
            }

        }
        return defaultValue;
    }

    public static <T> T marshall(final String data, final T defaultValue) {
		return marshall(data, null, defaultValue);
    }

    public static String marshall(final Object object, final String defaultValue) {
        if (GWT.isClient() && object != null) {
            final Serializer serializer = new Serializer();
            return serializer.serialize(object);
        }
        return defaultValue;
    }

    public static String marshall(final Object object) {
        return marshall(object, "");
    }
}
