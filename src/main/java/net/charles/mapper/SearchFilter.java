package net.charles.mapper;

public class SearchFilter<V> {
    private final String fieldName;
    private final V fieldValue;

    public SearchFilter(String fieldName, V fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public V getFieldValue() {
        return fieldValue;
    }
}
