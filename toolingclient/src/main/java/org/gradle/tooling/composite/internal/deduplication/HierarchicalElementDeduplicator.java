/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite.internal.deduplication;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * A generic name de-duplicator for hierarchical elements.
 *
 * Conflicting root elements are de-duplicated by appending a counter. Conflicting sub-elements are
 * de-duplicated by prepending their parent element names, separated by a dash.
 *
 * @param <T> the type of element to de-duplicate
 * @author Stefan Oehme
 */
public class HierarchicalElementDeduplicator<T> {

    private final NameDeduplicationStrategy<T> strategy;

    public HierarchicalElementDeduplicator(NameDeduplicationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    /**
     * Calculates a set of renamings for each duplicate name in the given set of elements.
     *
     * @param elements the elements with possibly duplicated names
     * @return a Map containing the new name for each element that has to be renamed
     */
    public Map<T, String> deduplicate(Collection<T> elements) {
        return new StatefulDeduplicator(elements).getNewNames();
    }

    /**
     * This inner class hides the fact that the actual de-duplication algorithm is stateful.
     */
    private class StatefulDeduplicator {

        private final List<T> elements;
        private final Multimap<String, T> elementsByName;
        private final Map<T, String> newNames;
        private final Map<T, T> prefixes;

        public StatefulDeduplicator(Collection<T> elements) {
            this.elements = Lists.newArrayList(elements);
            this.elementsByName = LinkedHashMultimap.create();
            this.newNames = Maps.newHashMap();
            this.prefixes = Maps.newHashMap();
        }

        public Map<T, String> getNewNames() {
            if (!this.elements.isEmpty() && this.newNames.isEmpty()) {
                calculateNewNames();
            }

            return ImmutableMap.copyOf(this.newNames);
        }

        private void calculateNewNames() {
            sortElementsByDepth();
            for (T element : this.elements) {
                this.elementsByName.put(getOriginalName(element), element);
                this.prefixes.put(element, getParent(element));
            }
            while (!getDuplicateNames().isEmpty()) {
                deduplicate();
            }
            simplifyNames();
        }

        private void deduplicate() {
            for (String duplicateName : getDuplicateNames()) {
                Collection<T> elementsToRename = this.elementsByName.get(duplicateName);
                Set<T> notYetRenamed = getNotYetRenamedElements(elementsToRename);
                boolean renameSuccessful = false;
                for (T element : notYetRenamed) {
                    renameSuccessful |= renameUsingParentPrefix(element);
                }
                if (!renameSuccessful) {
                    renameTiedElements(notYetRenamed, duplicateName);
                }
            }
        }

        private boolean renameUsingParentPrefix(T element) {
            T prefixElement = this.prefixes.get(element);
            if (prefixElement != null) {
                renameTo(element, getCurrentlyAssignedName(prefixElement) + "-" + getCurrentlyAssignedName(element));
                this.prefixes.put(element, getParent(prefixElement));
                return true;
            }
            return false;
        }

        private void renameTiedElements(Set<T> notYetRenamed, String duplicateName) {
            Map<T, String> renamedElements = HierarchicalElementDeduplicator.this.strategy.renameTiedElements(notYetRenamed, duplicateName);
            for (Entry<T, String> renamedElement : renamedElements.entrySet()) {
                renameTo(renamedElement.getKey(), renamedElement.getValue());
            }
        }

        private void renameTo(T element, String newName) {
            this.elementsByName.remove(getCurrentlyAssignedName(element), element);
            this.elementsByName.put(newName, element);
            this.newNames.put(element, newName);
        }

        private void simplifyNames() {
            Set<String> deduplicatedNames = this.elementsByName.keySet();
            for (T element : this.elements) {
                String simplifiedName = removeDuplicateWordsFromPrefix(getCurrentlyAssignedName(element), getOriginalName(element));
                if (!deduplicatedNames.contains(simplifiedName)) {
                    renameTo(element, simplifiedName);
                }
            }
        }

        private String removeDuplicateWordsFromPrefix(String deduplicatedName, String originalName) {
            String prefix = deduplicatedName.substring(0, deduplicatedName.lastIndexOf(originalName));
            if (prefix.isEmpty()) {
                return deduplicatedName;
            }

            Splitter splitter = Splitter.on('-').omitEmptyStrings();
            List<String> prefixParts = Lists.newArrayList(splitter.split(prefix));
            List<String> postfixParts = Lists.newArrayList(splitter.split(originalName));
            List<String> words = Lists.newArrayList();

            if (postfixParts.size() > 1) {
                String postfixHead = postfixParts.get(0);
                prefixParts.add(postfixHead);
                postfixParts.remove(postfixHead);
            }

            for (String prefixPart : prefixParts) {
                if (!prefixPart.equals(Iterables.getLast(words, null))) {
                    words.add(prefixPart);
                }
            }

            words.addAll(postfixParts);

            return Joiner.on('-').join(words);
        }

        private Set<String> getDuplicateNames() {
            Set<String> duplicates = Sets.newLinkedHashSet();
            for (Entry<String, Collection<T>> entry : this.elementsByName.asMap().entrySet()) {
                if (entry.getValue().size() > 1) {
                    duplicates.add(entry.getKey());
                }
            }
            return duplicates;
        }

        private Set<T> getNotYetRenamedElements(Collection<T> elementsToRename) {
            Set<T> notYetRenamed = Sets.newLinkedHashSet();
            for (T element : elementsToRename) {
                if (!hasBeenRenamed(element)) {
                    notYetRenamed.add(element);
                }
            }
            return notYetRenamed;
        }

        private String getOriginalName(T element) {
            return HierarchicalElementDeduplicator.this.strategy.getName(element);
        }

        private String getCurrentlyAssignedName(T element) {
            if (hasBeenRenamed(element)) {
                return this.newNames.get(element);
            } else {
                return getOriginalName(element);
            }
        }

        private T getParent(T parent) {
            return HierarchicalElementDeduplicator.this.strategy.getParent(parent);
        }

        private boolean hasBeenRenamed(T element) {
            return this.newNames.containsKey(element);
        }

        private void sortElementsByDepth() {
            Collections.sort(this.elements, new Comparator<T>() {

                @Override
                public int compare(T left, T right) {
                    return Ints.compare(getDepth(left), getDepth(right));
                }

                private int getDepth(T element) {
                    int depth = 0;
                    T parent = element;
                    while (parent != null) {
                        depth++;
                        parent = getParent(parent);
                    }
                    return depth;
                }
            });
        }
    }

}
