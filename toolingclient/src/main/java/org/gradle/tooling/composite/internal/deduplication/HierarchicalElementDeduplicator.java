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
import com.google.common.collect.ImmutableMap;
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
 * @author Stefan Oehme
 */
public class HierarchicalElementDeduplicator<T> {

    private final NameDeduplicationStrategy<T> strategy;

    public HierarchicalElementDeduplicator(NameDeduplicationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    /**
     * Returns a Map containing an entry for each element that needs to be renamed.
     */
    public Map<T, String> deduplicate(Collection<T> elements) {
        return new StatefulDeduplicator(elements).getNewNames();
    }

    private class StatefulDeduplicator {

        private final List<T> elements;
        private Map<T, String> newNames;
        private Map<T, T> prefixes;

        public StatefulDeduplicator(Collection<T> elements) {
            this.elements = Lists.newArrayList(elements);
            this.newNames = Maps.newHashMap();
            this.prefixes = Maps.newHashMap();
        }

        public Map<T, String> getNewNames() {
            if (newNames.isEmpty()) {
                calculateNewNames();
            }

            return ImmutableMap.copyOf(newNames);
        }

        private void calculateNewNames() {
            sortElementsByDepth();
            for (T element : elements) {
                prefixes.put(element, getParent(element));
            }
            while (!getDuplicateNames().isEmpty()) {
                deduplicate();
            }
            simplifyNames();
        }

        private void deduplicate() {
            Multimap<String, T> elementsByName = getElementsByName();
            for (String duplicateName : getDuplicateNames()) {
                Collection<T> elementsToRename = elementsByName.get(duplicateName);
                Set<T> notYetRenamed = getNotYetRenamedElements(elementsToRename);
                if (notYetRenamed.size() > 1) {
                    for (T element : notYetRenamed) {
                        rename(element);
                    }
                } else {
                    for (T element : elementsToRename) {
                        if (!notYetRenamed.contains(element)) {
                            rename(element);
                        }
                    }
                    boolean deduplicationFailed = true;
                    for (T element : elementsToRename) {
                        deduplicationFailed &= getOriginalName(element).equals(duplicateName);
                    }
                    if (deduplicationFailed) {
                        for (T element : notYetRenamed) {
                            rename(element);
                        }
                    }
                }
            }
        }

        private void rename(T element) {
            T prefixElement = prefixes.get(element);
            if (prefixElement != null) {
                newNames.put(element, getOriginalName(prefixElement) + "-" + getCurrentlyAssignedName(element));
                prefixes.put(element, getParent(prefixElement));
            } else {
                int count = 0;
                while (true) {
                    count++;
                    String candidateName = getOriginalName(element) + String.valueOf(count);
                    if (!isNameTaken(candidateName)) {
                        newNames.put(element, candidateName);
                        break;
                    }
                }
            }
        }

        private void simplifyNames() {
            Set<String> deduplicatedNames = getElementsByName().keySet();
            for (T element : elements) {
                String simplifiedName = removeDuplicateWordsFromPrefix(getCurrentlyAssignedName(element), getOriginalName(element));
                if (!deduplicatedNames.contains(simplifiedName)) {
                    newNames.put(element, simplifiedName);
                }
            }
        }

        private String removeDuplicateWordsFromPrefix(String deduplicatedName, String originalName) {
            if (deduplicatedName.equals(originalName)) {
                return deduplicatedName;
            }

            String prefix = deduplicatedName.substring(0, deduplicatedName.lastIndexOf(originalName));
            if (prefix.isEmpty()) {
                return deduplicatedName;
            }
            List<String> prefixWordList = Lists.newArrayList(prefix.split("-"));
            List<String> postfixWordList = Lists.newArrayList(originalName.split("-"));
            if (postfixWordList.size() > 1) {
                prefixWordList.add(postfixWordList.get(0));
                postfixWordList = postfixWordList.subList(1, postfixWordList.size());
            }
            List<String> words = Lists.newArrayList();
            for (String prefixWord : prefixWordList) {
                if (words.isEmpty() || !words.get(words.size() - 1).equals(prefixWord)) {
                    words.add(prefixWord);
                }
            }
            words.addAll(postfixWordList);
            return Joiner.on('-').join(words);
        }

        private Multimap<String, T> getElementsByName() {
            Multimap<String, T> elementsByName = LinkedHashMultimap.create();
            for (T element : elements) {
                elementsByName.put(getCurrentlyAssignedName(element), element);
            }
            return elementsByName;
        }

        private Set<String> getDuplicateNames() {
            Multimap<String, T> elementsByName = getElementsByName();
            Set<String> duplicates = Sets.newLinkedHashSet();
            for (Entry<String, Collection<T>> entry : elementsByName.asMap().entrySet()) {
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

        private boolean isNameTaken(String name) {
            for (T element : elements) {
                if (getCurrentlyAssignedName(element).equals(name) && hasBeenRenamed(element)) {
                    return true;
                }
            }
            return false;
        }

        private String getOriginalName(T element) {
            return strategy.getName(element);
        }

        private String getCurrentlyAssignedName(T element) {
            if (hasBeenRenamed(element)) {
                return newNames.get(element);
            } else {
                return getOriginalName(element);
            }
        }

        private T getParent(T parent) {
            return strategy.getParent(parent);
        }

        private boolean hasBeenRenamed(T element) {
            return newNames.containsKey(element);
        }

        private void sortElementsByDepth() {
            Collections.sort(elements, new Comparator<T>() {

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
