package net.charles.parser;

import com.google.gson.*;
import net.charles.annotations.DataKey;
import net.charles.annotations.Exclude;
import net.charles.logger.LoggerProvider;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public abstract class JeninParser {
    /**
     * <p>
     *     Default exclusion strategies used during serialization/deserialization of an object by {@link Gson}
     *     The default exclusion strategies for serialization, excludes any fields and/or class having the {@link Exclude} annotation,
     *     and fields with {@link DataKey} annotation that has {@link DataKey#include()} {@code false}
     *     As for deserialization, the default exclusion strategy does not skip any field and/or class
     * </p>
     */
    public final static ExclusionStrategy DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            DataKey key = fieldAttributes.getAnnotation(DataKey.class);
            return key != null && !key.include() || fieldAttributes.getAnnotation(Exclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return aClass.getAnnotation(Exclude.class) != null;
        }
    };

    public final static ExclusionStrategy DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    };
    private final Logger logger = initLogger();
    private Gson gson;

    /**
     * Public constructor that will build the {@link Gson} with the {@link #DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY} and {@link #DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY}
     */
    public JeninParser() {
        this(JeninParser.DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY, DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY);
    }

    /**
     * Public constructor to customize the exclusion strategies
     * @param serializationStrategy {@link GsonBuilder#addSerializationExclusionStrategy(ExclusionStrategy)} the strategy used while serializing objects into redis
     * @param deserializationStrategy {@link GsonBuilder#addDeserializationExclusionStrategy(ExclusionStrategy)} the strategy used while deserializing objects from redis
     */
    public JeninParser(ExclusionStrategy serializationStrategy, ExclusionStrategy deserializationStrategy) {
        gson = new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(serializationStrategy).addDeserializationExclusionStrategy(deserializationStrategy).create();
    }

    /**
     * Rebuilding the {@link Gson} via {@link #updateExclusionStrategy(ExclusionStrategy, ExclusionStrategy)} / {@link #updateExclusionStrategy(ExclusionStrategy...)}
     * @param builder the {@link GsonBuilder} used to build {@link Gson}
     */
    protected void rebuildGson(GsonBuilder builder) {
        this.gson = builder.create();
    }

    /**
     * Converts an object into the appropriate form {@link Map<String, String>} to fit the redis hashset
     * @param obj the object instance
     * @param <T> the object type
     * @return {@link Map<String, String>}
     * @throws IllegalAccessException
     */
    protected <T> Map<String, String> convertToHashSet(T obj) throws IllegalAccessException {
        final Map<String, String> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getAnnotation(DataKey.class) != null && !field.getAnnotation(DataKey.class).include() || field.getAnnotation(Exclude.class) != null) continue;
            field.setAccessible(true);
            if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                map.put(field.getName(), String.valueOf(field.get(obj)));
            } else {
                map.put(field.getName(), gson.toJson(field.get(obj)));
            }
        }
        return map;
    }

    /**
     * Converts the hashset object from redis into an object
     * @param map the redis hashset containing the hashed object
     * @param clazz the object class
     * @param <T> the object type
     * @return the object
     */
    protected <T> T convertToObject(Map<String, String> map, Class<T> clazz) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonObject.addProperty(entry.getKey(), entry.getValue());
        }
        return (T) getGson().fromJson(jsonObject, clazz);
    }

    /**
     * The Gson containing the exclusion strategies and other parameters
     * @return {@link Gson}
     */
    protected Gson getGson() {
        return gson;
    }

    /**
     * The Jenin Parser logger
     * @return {@link LoggerProvider#getLogger(String)}
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Updates the {@link Gson} serialization and deserialization exclusion strategies
     * @param serializationExclusionStrategy {@link ExclusionStrategy} the serialization exclusion strategy
     * @param deserializationExclusionStrategy {@link ExclusionStrategy} the deserialization exclusion strategy
     * @return {@link JeninParser}
     */
    public abstract JeninParser updateExclusionStrategy(ExclusionStrategy serializationExclusionStrategy, ExclusionStrategy deserializationExclusionStrategy);

    /**
     * Updates the {@link Gson} global serialization and deserialization exclusion strategies
     * @param exclusionStrategy {@link ExclusionStrategy} the strategies
     * @return {@link JeninParser}
     */
    public abstract JeninParser updateExclusionStrategy(ExclusionStrategy... exclusionStrategy);

    /**
     * Pushes the json string as a redis string
     * @param key the key to access the string
     * @param json the json string
     */
    public abstract void compactPush(String key, String json);

    /**
     * Converts the object into a json string and stores as a redis string
     * @param t the object
     * @param <T> the object type
     * @throws IllegalAccessException
     */
    public abstract <T> void compactPush(T t) throws IllegalAccessException;

    /**
     * Stores the object as a redis hash set
     * @param t the object
     * @param <T> the object type
     * @throws IllegalAccessException
     */
    public abstract <T> void push(T t) throws IllegalAccessException;

    /**
     * Stores the object as a redis hash set
     * @param key the key to access the hash set
     * @param obj the object
     */
    public abstract void push(String key, Map<String, String> obj);

    /**
     * <p>
     *     ==============================================
     *     Does not look up hash set, only redis strings
     *     ==============================================
     *
     *     Searches a redis string and converts it into an object
     * </p>
     * @param key the key to access the redis string
     * @param clazz the object class
     * @param <T> the object type
     * @return the object
     */
    public abstract <T> T compactSearch(String key, Class<T> clazz);

    /**
     * <p>
     *     ==============================================
     *     Does not look up hash set, only redis strings
     *     ==============================================
     *
     *     Searches a redis string and returns the value of the field
     * </p>
     * @param key the key to access the redis string
     * @param fieldName the json field name to access the value
     * @param clazz the class of the object
     * @return the value formatted into a string
     */
    public abstract String compactSearch(String key, String fieldName, Class<?> clazz);

    /**
     * <p>
     *     ==============================================
     *     Does not look up redis strings, only hash sets
     *     ==============================================
     *
     *     Searches a hash set and converts it into an object
     * </p>
     *
     * @param key the key to access the hash set
     * @param <T> the object type
     * @return the object
     */
    public abstract <T> T search(String key, Class<T> clazz);

    /**
     * <p>
     *     ==============================================
     *     Does not look up redis strings, only hash sets
     *     ==============================================
     *
     *     Returns an array of object that has an equal value in the selected field
     * </p>
     * @param fieldName the selected field
     * @param fieldValue the value of the field
     * @param <T> the objects type
     * @param <V> the value type
     * @return an array of objects
     */
    public abstract <T, V> T[] search(String fieldName, V fieldValue);

    /**
     * Configures the Parser's logger
     * @return {@link Logger}
     */
    private Logger initLogger() {
        LoggerProvider.setProvider(s -> {
            final ConsoleHandler consoleHandler = new ConsoleHandler() {{
                setLevel(Level.ALL);
                setFormatter(new Formatter() {
                    private static final String PATTERN = "[dd/MM/yyyy HH:mm:ss]";
                    @Override
                    public String format(final LogRecord record) {
                        return String.format("%1$s %2$-10s %3$-10s %4$s\n",
                                new SimpleDateFormat(PATTERN).format(new Date(record.getMillis())),
                                "[" + record.getLoggerName() + "]",
                                record.getLevel().getName(),
                                formatMessage(record));
                    }
                });
            }};
            final Logger logger = Logger.getLogger(s);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
            return logger;
        });
        return Logger.getLogger("JeninParser");
    }
}
