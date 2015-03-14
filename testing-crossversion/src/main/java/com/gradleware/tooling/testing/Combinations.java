/*
 * Copyright 2015 the original author or authors.
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

package com.gradleware.tooling.testing;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Calculates all the combinations of the elements from different lists.
 * <p/>
 * Inspired by http://www.megustaulises.com/2012/12/cross-product-using-recursion.html
 *
 * @author Etienne Studer
 */
public final class Combinations {

    private Combinations() {
    }

    /**
     * Returns all combinations for the given lists.
     *
     * @param lists the lists whose elements to combine, must not be null
     * @return all the combinations, never null
     */
    public static ImmutableList<List<Object>> getCombinations(List<?>... lists) {
        Preconditions.checkNotNull(lists);

        if (lists.length == 0) {
            return ImmutableList.of();
        }

        ImmutableMap.Builder<Integer, List<?>> listsMappedByDepth = ImmutableMap.builder();
        for (int i = 0; i < lists.length; i++) {
            listsMappedByDepth.put(i, lists[i]);
        }

        return getCombinationsRecursive(listsMappedByDepth.build(), 0, new Object[(lists.length)]);
    }

    private static ImmutableList<List<Object>> getCombinationsRecursive(Map<Integer, List<?>> lists, int depth, Object[] current) {
        ImmutableList.Builder<List<Object>> result = ImmutableList.builder();
        Collection<?> listAtCurrentDepth = lists.get(depth);
        for (Object element : listAtCurrentDepth) {
            current[depth] = element;
            if (depth < lists.size() - 1) {
                result.addAll(getCombinationsRecursive(lists, depth + 1, current));
            } else {
                result.add(Lists.newArrayList(current)); // use ArrayList to support null values
            }
        }
        return result.build();
    }

}
