package net.charles.parser;

public class SearchFilter<V, C> {
    private final String fieldName;
    private final V fieldValue;
    private final Class<C> aClass;

    public SearchFilter(String fieldName, V fieldValue, Class<C> aClass) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.aClass = aClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public V getFieldValue() {
        return fieldValue;
    }

    public Class<C> getType() {
        return aClass;
    }
}
