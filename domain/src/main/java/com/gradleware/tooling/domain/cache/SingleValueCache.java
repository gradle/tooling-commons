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
        this.stateBoundValue = new StateBoundValue<V>();
    }

    /**
     * Gets the value, if the value has been loaded into the cache.
     *
     * @return the value if present, {@link Optional#absent()} otherwise
     */
    public Optional<V> getIfPresent() {
        synchronized (this.stateBoundValue) {
            switch (this.stateBoundValue.state) {
                case RESET:
                    return Optional.absent();
                case LOADING:
                    return Optional.absent();
                case LOADED:
                    return Optional.of(this.stateBoundValue.getValue());
                default:
                    throw new IllegalStateException("Unexpected CacheState: " + this.stateBoundValue.state);
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
                    synchronized (this.stateBoundValue) {
                        // set the loaded value iff while loading the value
                        // a) no value was put explicitly into the cache by some other thread (put API)
                        // b) the cache was not invalidated by some other thread (invalidate API)
                        if (this.stateBoundValue.modifier == Thread.currentThread()) {
                            this.stateBoundValue.set(CacheState.LOADED, value, Thread.currentThread());
                            valueUpdateValid = true;
                        }

                        current = this.stateBoundValue.copy();
                    }

                    // notify all waiting threads about the loaded and newly set value
                    if (valueUpdateValid) {
                        synchronized (this.waitUntilLoaded) {
                            this.waitUntilLoaded.notifyAll();
                        }
                    }

                    return current.getValue();
                } catch (Throwable t) {
                    t.printStackTrace();
                    synchronized (this.stateBoundValue) {
                        this.stateBoundValue.state = CacheState.RESET;
                        this.stateBoundValue.value = null;
                        this.stateBoundValue.modifier = Thread.currentThread();
                    }

                    throw new CacheException(t);
                }
            case LOADING:
                synchronized (this.waitUntilLoaded) {
                    try {
                        this.waitUntilLoaded.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return get(valueLoader);
            case LOADED:
                return previous.getValue();
            default:
                throw new IllegalStateException("Unexpected CacheState: " + this.stateBoundValue.state);
        }
    }

    private StateBoundValue<V> triggerLoadingIfNeeded() {
        synchronized (this.stateBoundValue) {
            StateBoundValue<V> previous = this.stateBoundValue.copy();
            switch (this.stateBoundValue.state) {
                case RESET:
                    this.stateBoundValue.set(CacheState.LOADING, null, Thread.currentThread());
                    return previous;
                case LOADING:
                    return previous;
                case LOADED:
                    return previous;
                default:
                    throw new IllegalStateException("Unexpected CacheState: " + this.stateBoundValue.state);
            }
        }
    }

    public void put(V value) {
        Preconditions.checkArgument(value != null, "Value must not be null");

        synchronized (this.stateBoundValue) {
            this.stateBoundValue.set(CacheState.LOADED, value, Thread.currentThread());
        }
    }

    public void invalidate() {
        synchronized (this.stateBoundValue) {
            this.stateBoundValue.set(CacheState.RESET, null, Thread.currentThread());
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
            if (this.state != CacheState.LOADED) {
                throw new IllegalStateException("Value is not available in state: " + this.state);
            }

            return this.value;
        }

        private StateBoundValue<T> copy() {
            StateBoundValue<T> copy = new StateBoundValue<T>();
            copy.set(this.state, this.value, this.modifier);
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
            return Objects.equal(this.state, that.state) &&
                    Objects.equal(this.value, that.value) &&
                    Objects.equal(this.modifier, that.modifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.state, this.value, this.modifier);
        }

        @Override
        public String toString() {
            return "StateBoundValue{" +
                    "state=" + this.state +
                    ", value=" + this.value +
                    ", modifier=" + this.modifier +
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
