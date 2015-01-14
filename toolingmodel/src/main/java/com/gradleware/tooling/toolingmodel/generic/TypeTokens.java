package com.gradleware.tooling.toolingmodel.generic;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.SortedSet;

/**
 * Utility class to create complex {@link TypeToken} instances.
 *
 * @see com.google.common.reflect.TypeToken
 */
public final class TypeTokens {

    public static <T> TypeToken<Model<T>> domainObjectToken(Class<T> fieldAggregatorType) {
        return new TypeToken<Model<T>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, TypeToken.of(fieldAggregatorType));
    }

    public static <T> TypeToken<List<Model<T>>> domainObjectListToken(Class<T> fieldAggregatorType) {
        return new TypeToken<List<Model<T>>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, TypeToken.of(fieldAggregatorType));
    }

    public static <T> TypeToken<List<T>> listToken(Class<T> elementType) {
        return new TypeToken<List<T>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, TypeToken.of(elementType));
    }

    public static <T> TypeToken<SortedSet<T>> sortedSetToken(Class<T> elementType) {
        return new TypeToken<SortedSet<T>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, TypeToken.of(elementType));
    }

    private TypeTokens() {
    }

}
