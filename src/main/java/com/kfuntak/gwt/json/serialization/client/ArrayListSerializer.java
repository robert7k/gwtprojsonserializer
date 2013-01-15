package com.kfuntak.gwt.json.serialization.client;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONValue;

public class ArrayListSerializer extends AbstractObjectSerializer {
    String elementClassName = null;
    public ArrayListSerializer(final String className) {
        elementClassName = className;
    }

    public ArrayListSerializer() {}

    @Override
	public JSONValue serializeToJson(final Object pojo) {
        if(!(pojo instanceof Collection)){
            throw new IllegalArgumentException();
        }
        final Collection<?> list = (Collection<?>)pojo;
        final JSONArray jsonList = new JSONArray();
        int index = 0;
        for (final Object item : list) {
            jsonList.set(index++, SerializerHelper.getValue(item));
        }

        return jsonList;
    }

    @Override
    public Object deSerialize(final JSONValue jsonValue, final String className) throws JSONException {
        final JSONArray jsonArray = jsonValue.isArray();
        if (jsonArray == null) {
            throw new IllegalArgumentException("Json value was not an array");
        }
        final ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONValue value = jsonArray.get(i);
            if(elementClassName != null) {
                list.add(DeserializerHelper.getObject(value, className));
            } else {
                list.add(DeserializerHelper.getValue(value));
            }
        }

        return list;
    }

    @Override
	public Object deSerialize(final JSONValue jsonValue) throws JSONException {
        return deSerialize(jsonValue, elementClassName);
    }
}
