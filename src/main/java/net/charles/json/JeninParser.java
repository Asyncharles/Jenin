package net.charles.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.charles.annotations.DataKey;
import net.charles.annotations.Exclude;
import net.charles.logger.LoggerProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private final Gson gson;
    private final Logger logger = initLogger();

    public JeninParser() {
        this(JeninParser.DEFAULT_SERIALIZATION_EXCLUSION_STRATEGY, DEFAULT_DESERIALIZATION_EXCLUSION_STRATEGY);
    }

    public JeninParser(ExclusionStrategy serializationStrategy, ExclusionStrategy deserializationStrategy) {
        gson = new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(serializationStrategy).addDeserializationExclusionStrategy(deserializationStrategy).create();
    }

    public Gson getGson() {
        return gson;
    }

    public Logger getLogger() {
        return logger;
    }

    public abstract void compactPush(String key, String json);

    public abstract <T> void compactPush(T t) throws IllegalAccessException, InstantiationException;

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
