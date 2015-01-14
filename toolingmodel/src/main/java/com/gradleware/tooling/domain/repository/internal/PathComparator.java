package com.gradleware.tooling.domain.repository.internal;

import java.util.Comparator;

/**
 * Compares two paths first by their depth and in case of equal depth lexicographically by their segments, starting with the left-most segment.
 *
 * @see org.gradle.tooling.internal.consumer.converters.TaskNameComparator
 */
final class PathComparator implements Comparator<String> {

    static final PathComparator INSTANCE = new PathComparator();

    private PathComparator() {
    }

    public int compare(String path1, String path2) {
        int depthDiff = getDepth(path1) - getDepth(path2);
        if (depthDiff != 0) {
            return depthDiff;
        }
        return compareSegments(path1, path2);
    }

    private int compareSegments(String path1, String path2) {
        int colon1 = path1.indexOf(':');
        int colon2 = path2.indexOf(':');
        if (colon1 > 0 && colon2 > 0) {
            int diff = path1.substring(0, colon1).compareTo(path2.substring(0, colon2));
            if (diff != 0) {
                return diff;
            }
        }
        return colon1 == -1 ? path1.compareTo(path2) : compareSegments(path1.substring(colon1 + 1), path2.substring(colon2 + 1));
    }

    private int getDepth(String taskName) {
        int counter = 0;
        for (char c : taskName.toCharArray()) {
            if (c == ':') {
                counter++;
            }
        }
        return counter;
    }

}
