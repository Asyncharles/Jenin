package net.charles.parser;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private final static ExclusionStrategy DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {
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

    private final static ExclusionStrategy DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {
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

    public JeninParser() {
        this(JeninParser.DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY, DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY);
    }

    public JeninParser(ExclusionStrategy serializationStrategy, ExclusionStrategy deserializationStrategy) {
        gson = new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(serializationStrategy).addDeserializationExclusionStrategy(deserializationStrategy).create();
    }

    protected void rebuildGson(GsonBuilder builder) {
        this.gson = builder.create();
    }

    protected <T> Map<String, String> convert(T obj) throws IllegalAccessException {
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

    protected Gson getGson() {
        return gson;
    }

    public Logger getLogger() {
        return logger;
    }

    public abstract JeninParser updateExclusionStrategy(ExclusionStrategy serializationExclusionStrategy, ExclusionStrategy deserializationExclusionStrategy);

    public abstract JeninParser updateExclusionStrategy(ExclusionStrategy... exclusionStrategy);

    public abstract void compactPush(String key, String json);

    public abstract <T> void compactPush(T t) throws IllegalAccessException;

    public abstract <T> void push(T t) throws IllegalAccessException;

    public abstract void push(String key, Map<String, String> obj);

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
