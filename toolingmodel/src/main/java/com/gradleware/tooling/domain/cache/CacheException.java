package com.gradleware.tooling.domain.cache;

/**
 * Exception thrown when attempting to access the cache resulted in an exception.
 */
public final class CacheException extends Exception {

    private static final long serialVersionUID = 1;

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

}
