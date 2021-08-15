package net.charles.parser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;

public class StringAdapter<T> extends TypeAdapter<T> {

    @Override
    public void write(JsonWriter jsonWriter, T t) throws IOException {
        for (Field field : t.getClass().getDeclaredFields()) {
            try {
                jsonWriter.value(String.valueOf(field.get(t)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
