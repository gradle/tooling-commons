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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gradle.tooling.model.HierarchicalElement;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A generic name de-duplicator for hierarchical elements.
 *
 * Conflicting root elements are de-duplicated by appending a counter.
 * Conflicting sub-elements are de-duplicated by prepending their parent element names, separated by a dash.
 *
 * @author Stefan Oehme
 */
public class HierarchicalElementDeduplicator {

    public void deduplicate(Collection<RenamableElement> elements) {
        Map<HierarchicalElement, HierarchicalElement> prefixes = Maps.newLinkedHashMap();
        for (RenamableElement element : elements) {
            prefixes.put(element.getOriginal(), element.getOriginal().getParent());
        }
        while (!getDuplicateNames(elements).isEmpty()) {
            deduplicate(elements, prefixes);
        }
        simplifyNames(elements);
    }

    private void deduplicate(Collection<RenamableElement> elements, Map<HierarchicalElement, HierarchicalElement> prefixes) {
        Multimap<String, RenamableElement> elementsByName = getElementsByName(elements);
        for (String duplicateName : getDuplicateNames(elements)) {
            Collection<RenamableElement> elementsToRename = elementsByName.get(duplicateName);
            Set<RenamableElement> notYetRenamed = getNotYetRenamedElements(elementsToRename);
            if (notYetRenamed.size() > 1) {
                for (RenamableElement element : notYetRenamed) {
                    rename(element, prefixes, elements);
                }
            } else {
                for (RenamableElement element : elementsToRename) {
                    if (!notYetRenamed.contains(element)) {
                        rename(element, prefixes, elements);
                    }
                }
                boolean deduplicationFailed = true;
                for (RenamableElement element : elementsToRename) {
                    deduplicationFailed &= element.getName().equals(duplicateName);
                }
                if (deduplicationFailed) {
                    for (RenamableElement element : notYetRenamed) {
                        rename(element, prefixes, elements);
                    }
                }
            }
        }
    }

    private void rename(RenamableElement element, Map<HierarchicalElement, HierarchicalElement> prefixes, Collection<RenamableElement> elements) {
        HierarchicalElement prefixElement = prefixes.get(element.getOriginal());
        if (prefixElement != null) {
            element.renameTo(prefixElement.getName() + "-" + element.getName());
            prefixes.put(element.getOriginal(), prefixElement.getParent());
        } else {
            int count = 0;
            while (true) {
                count++;
                String candidateName = element.getName() + String.valueOf(count);
                if (!isNameTaken(candidateName, elements)) {
                    element.renameTo(candidateName);
                    break;
                }
            }
        }
    }

    private void simplifyNames(Collection<RenamableElement> elements) {
        Set<String> deduplicatedNames = getElementsByName(elements).keySet();
        for (RenamableElement element : elements) {
            String simplifiedName = removeDuplicateWordsFromPrefix(element.getName(), element.getOriginal().getName());
            if (!deduplicatedNames.contains(simplifiedName)) {
                element.renameTo(simplifiedName);
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

    private boolean isNameTaken(String name, Collection<RenamableElement> elements) {
        for (RenamableElement element : elements) {
            if (element.getName().equals(name) && element.hasBeenRenamed()) {
                return true;
            }
        }
        return false;
    }

    private Multimap<String, RenamableElement> getElementsByName(Collection<RenamableElement> elements) {
        Multimap<String, RenamableElement> elementsByName = LinkedHashMultimap.create();
        for (RenamableElement element : elements) {
            elementsByName.put(element.getName(), element);
        }
        return elementsByName;
    }

    private Set<String> getDuplicateNames(Collection<RenamableElement> elements) {
        Multimap<String, RenamableElement> elementsByName = getElementsByName(elements);
        Set<String> duplicates = Sets.newLinkedHashSet();
        for (Entry<String, Collection<RenamableElement>> entry : elementsByName.asMap().entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }

    private Set<RenamableElement> getNotYetRenamedElements(Collection<RenamableElement> elementsToRename) {
        Set<RenamableElement> notYetRenamed = Sets.newLinkedHashSet();
        for (RenamableElement element : elementsToRename) {
            if (!element.hasBeenRenamed()) {
                notYetRenamed.add(element);
            }
        }
        return notYetRenamed;
    }
}
