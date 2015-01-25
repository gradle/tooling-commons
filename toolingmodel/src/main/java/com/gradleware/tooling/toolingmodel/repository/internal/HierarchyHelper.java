package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.toolingmodel.HierarchicalModel;
import org.gradle.api.specs.Spec;

import java.util.Comparator;
import java.util.List;

/**
 * Helper class to manage hierarchies.
 */
final class HierarchyHelper<T extends HierarchicalModel<T>> {

    private T current;
    private T parent;
    private final List<T> children;
    private final Comparator<? super T> comparator;

    HierarchyHelper(T current, Comparator<? super T> comparator) {
        this.current = current;
        this.children = Lists.newArrayList();
        this.comparator = Preconditions.checkNotNull(comparator);
    }

    T getParent() {
        return this.parent;
    }

    void setParent(T parent) {
        this.parent = parent;
    }

    ImmutableList<T> getChildren() {
        return sort(this.children);
    }

    void addChild(T child) {
        this.children.add(child);
    }

    ImmutableList<T> getAll() {
        ImmutableList.Builder<T> all = ImmutableList.builder();
        addRecursively(this.current, all);
        return sort(all.build());
    }

    private void addRecursively(T node, ImmutableList.Builder<T> nodes) {
        nodes.add(node);
        for (T child : node.getChildren()) {
            addRecursively(child, nodes);
        }
    }

    private <E extends T> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

    ImmutableList<T> filter(Spec<? super T> predicate) {
        return FluentIterable.from(getAll()).filter(toPredicate(predicate)).toList();
    }

    Optional<T> tryFind(Spec<? super T> predicate) {
        return Iterables.tryFind(getAll(), toPredicate(predicate));
    }

    private static <T> Predicate<? super T> toPredicate(final Spec<? super T> spec) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return spec.isSatisfiedBy(input);
            }
        };
    }

}
