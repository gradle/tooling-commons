package com.gradleware.tooling.domain.generic;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link HierarchicalModel } interface.
 */
public final class DefaultHierarchicalModel<FIELDS> implements HierarchicalModel<FIELDS> {

    private final Comparator<? super HierarchicalModel<FIELDS>> comparator;
    private final ModelValueContainer<FIELDS> valueHolder;
    private final List<DefaultHierarchicalModel<FIELDS>> children;
    private DefaultHierarchicalModel<FIELDS> parent;

    public DefaultHierarchicalModel() {
        this(Ordering.allEqual());
    }

    public DefaultHierarchicalModel(Comparator<? super HierarchicalModel<FIELDS>> comparator) {
        this.comparator = Preconditions.checkNotNull(comparator);
        this.valueHolder = new ModelValueContainer<FIELDS>();
        this.children = Lists.newArrayList();
        this.parent = null;
    }

    @Override
    public boolean isPresent(ModelField<?, FIELDS> field) {
        return this.valueHolder.isPresent(field);
    }

    @Override
    public <V> V get(ModelField<V, FIELDS> field) {
        return this.valueHolder.get(field);
    }

    public <V> void put(ModelField<V, FIELDS> field, V value) {
        this.valueHolder.put(field, value);
    }

    @Override
    public HierarchicalModel<FIELDS> getParent() {
        return this.parent;
    }

    public void setParent(DefaultHierarchicalModel<FIELDS> parent) {
        this.parent = parent;
    }

    @Override
    public ImmutableList<HierarchicalModel<FIELDS>> getChildren() {
        return ImmutableList.<HierarchicalModel<FIELDS>>copyOf(sort(this.children));
    }

    public void addChild(DefaultHierarchicalModel<FIELDS> child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public ImmutableList<HierarchicalModel<FIELDS>> getAll() {
        return filter(Predicates.alwaysTrue());
    }

    @Override
    public ImmutableList<HierarchicalModel<FIELDS>> filter(Predicate<? super HierarchicalModel<FIELDS>> predicate) {
        ImmutableList.Builder<HierarchicalModel<FIELDS>> matches = ImmutableList.builder();
        addToMatches(this, matches, predicate);
        return sort(matches.build());
    }

    private void addToMatches(HierarchicalModel<FIELDS> node, ImmutableList.Builder<HierarchicalModel<FIELDS>> matches, Predicate<? super HierarchicalModel<FIELDS>> predicate) {
        if (predicate.apply(node)) {
            matches.add(node);
        }
        for (HierarchicalModel<FIELDS> child : node.getChildren()) {
            addToMatches(child, matches, predicate);
        }
    }

    @Override
    public Optional<HierarchicalModel<FIELDS>> tryFind(Predicate<? super HierarchicalModel<FIELDS>> predicate) {
        ImmutableList<HierarchicalModel<FIELDS>> descendants = getAll();
        return Iterables.tryFind(descendants, predicate);
    }

    private <E extends HierarchicalModel<FIELDS>> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

}
