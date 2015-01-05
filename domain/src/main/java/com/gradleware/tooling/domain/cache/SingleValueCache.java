package com.gradleware.tooling.domain.cache;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.EnumSet;
import java.util.concurrent.Callable;

/**
 * Cached mapping of a value.
 * <p>
 * todo (etst) implement proper exception handling
 * <p>
 * todo (etst) handle corner cases related to invalidate/put calls
 *
 * @param <V> the type of the value
 */
public final class SingleValueCache<V> {

    private final StateBoundValue<V> stateBoundValue;
    private final Object waitUntilLoaded = new Object();

    public SingleValueCache() {
        stateBoundValue = new StateBoundValue<V>();
    }

    /**
     * Gets the value, if the value has been loaded into the cache.
     *
     * @return the value if present, {@link Optional#absent()} otherwise
     */
    public Optional<V> getIfPresent() {
        synchronized (stateBoundValue) {
            switch (stateBoundValue.state) {
                case RESET:
                    return Optional.absent();
                case LOADING:
                    return Optional.absent();
                case LOADED:
                    return Optional.of(stateBoundValue.getValue());
                default:
                    throw new IllegalStateException("Unexpected CacheState: " + stateBoundValue.state);
            }
        }
    }

    public V get(Callable<? extends V> valueLoader) throws CacheException {
        Preconditions.checkArgument(valueLoader != null, "Value loader must not be null");

        StateBoundValue<V> previous = triggerLoadingIfNeeded();
        switch (previous.state) {
            case RESET:
                try {
                    // synchronously load the value through the value loader
                    @SuppressWarnings("ConstantConditions") V value = valueLoader.call();
                    if (value == null) {
                        throw new IllegalStateException("Loaded value must not be null");
                    }

                    // set the loaded value under certain conditions
                    boolean valueUpdateValid = false;
                    StateBoundValue<V> current;
                    synchronized (stateBoundValue) {
                        // set the loaded value iff while loading the value
                        // a) no value was put explicitly into the cache by some other thread (put API)
                        // b) the cache was not invalidated by some other thread (invalidate API)
                        if (stateBoundValue.modifier == Thread.currentThread()) {
                            stateBoundValue.set(CacheState.LOADED, value, Thread.currentThread());
                            valueUpdateValid = true;
                        }

                        current = stateBoundValue.copy();
                    }

                    // notify all waiting threads about the loaded and newly set value
                    if (valueUpdateValid) {
                        synchronized (waitUntilLoaded) {
                            waitUntilLoaded.notifyAll();
                        }
                    }

                    return current.getValue();
                } catch (Throwable t) {
                    t.printStackTrace();
                    synchronized (stateBoundValue) {
                        stateBoundValue.state = CacheState.RESET;
                        stateBoundValue.value = null;
                        stateBoundValue.modifier = Thread.currentThread();
                    }

                    throw new CacheException(t);
                }
            case LOADING:
                synchronized (waitUntilLoaded) {
                    try {
                        waitUntilLoaded.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return get(valueLoader);
            case LOADED:
                return previous.getValue();
            default:
                throw new IllegalStateException("Unexpected CacheState: " + stateBoundValue.state);
        }
    }

    private StateBoundValue<V> triggerLoadingIfNeeded() {
        synchronized (stateBoundValue) {
            StateBoundValue<V> previous = stateBoundValue.copy();
            switch (stateBoundValue.state) {
                case RESET:
                    stateBoundValue.set(CacheState.LOADING, null, Thread.currentThread());
                    return previous;
                case LOADING:
                    return previous;
                case LOADED:
                    return previous;
                default:
                    throw new IllegalStateException("Unexpected CacheState: " + stateBoundValue.state);
            }
        }
    }

    public void put(V value) {
        Preconditions.checkArgument(value != null, "Value must not be null");

        synchronized (stateBoundValue) {
            stateBoundValue.set(CacheState.LOADED, value, Thread.currentThread());
        }
    }

    public void invalidate() {
        synchronized (stateBoundValue) {
            stateBoundValue.set(CacheState.RESET, null, Thread.currentThread());
        }
    }

    /**
     * Encapsulates the complete state that makes up the cache at any point in time. This includes the actual cache life-cycle state, the cache value, and the last modifier of
     * those attributes.
     *
     * @param <T> the type of the cached value
     */
    private static final class StateBoundValue<T> {

        private CacheState state;
        private T value;
        private Thread modifier;

        private StateBoundValue() {
            this(CacheState.RESET, null, Thread.currentThread());
        }

        private StateBoundValue(CacheState state, T value, Thread modifier) {
            set(state, value, modifier);
        }

        private void set(CacheState state, T value, Thread modifier) {
            // validate invariants
            if (state == null) {
                throw new IllegalStateException("State must not be null");
            }
            if (EnumSet.of(CacheState.RESET, CacheState.LOADING).contains(state) && value != null) {
                throw new IllegalStateException("Value must be null in state: " + state);
            }
            if (state == CacheState.LOADED && value == null) {
                throw new IllegalStateException("Value must not be null in state: " + state);
            }
            if (modifier == null) {
                throw new IllegalStateException("Modifier must not be null");
            }

            // apply values if validation succeeded
            this.state = state;
            this.value = value;
            this.modifier = modifier;
        }

        private T getValue() {
            // validate current state permits getting the value
            if (state != CacheState.LOADED) {
                throw new IllegalStateException("Value is not available in state: " + state);
            }

            return value;
        }

        private StateBoundValue<T> copy() {
            StateBoundValue<T> copy = new StateBoundValue<T>();
            copy.set(state, value, modifier);
            return copy;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            StateBoundValue that = (StateBoundValue) other;
            return Objects.equal(state, that.state) &&
                    Objects.equal(value, that.value) &&
                    Objects.equal(modifier, that.modifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(state, value, modifier);
        }

        @Override
        public String toString() {
            return "StateBoundValue{" +
                    "state=" + state +
                    ", value=" + value +
                    ", modifier=" + modifier +
                    '}';
        }
    }

    /**
     * Enumerates the distinct life-cycle states that a cache can be in.
     */
    private enum CacheState {

        RESET, LOADING, LOADED

    }

}
