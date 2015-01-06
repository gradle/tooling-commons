package com.gradleware.tooling.domain.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.gradleware.tooling.domain.BuildInvocationsContainer;
import org.gradle.tooling.internal.consumer.converters.TaskNameComparator;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.TaskSelector;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Converts a {@link org.gradle.tooling.model.GradleProject} to a {@link com.gradleware.tooling.domain.BuildInvocationsContainer}.
 */
public final class BuildInvocationsContainerFactory {

    @SuppressWarnings("RedundantStringConstructorCall")
    private static final String NULL_STRING = new String(); // ensure unique instance to use it as a null-string placeholder

    private BuildInvocationsContainerFactory() {
    }

    public static BuildInvocationsContainer createFrom(GradleProject project) {
        ImmutableMultimap<String, GradleTask> tasks = buildProjectTasksRecursively(project, ArrayListMultimap.<String, GradleTask>create());
        ImmutableMultimap<String, TaskSelector> taskSelectors = buildTaskSelectorsRecursively(project, ArrayListMultimap.<String, TaskSelector>create());
        ImmutableMap<String, BuildInvocations> buildInvocationsMap = buildBuildInvocationsMappingRecursively(tasks, taskSelectors, Maps.<String, BuildInvocations>newHashMap());
        return BuildInvocationsContainer.from(buildInvocationsMap);
    }

    private static ImmutableMap<String, BuildInvocations> buildBuildInvocationsMappingRecursively(Multimap<String, GradleTask> projectTasks,
                                                                                                  Multimap<String, TaskSelector> taskSelectors,
                                                                                                  Map<String, BuildInvocations> mapping) {
        Preconditions.checkState(projectTasks.keySet().size() == taskSelectors.keySet().size());
        Preconditions.checkState(projectTasks.keySet().containsAll(taskSelectors.keySet()) && taskSelectors.keySet().containsAll(projectTasks.keySet()));

        for (String projectPath : projectTasks.keySet()) {
            ImmutableSortedSet<GradleTask> projectTasksOfProject = ImmutableSortedSet.orderedBy(TaskComparator.INSTANCE).addAll(projectTasks.get(projectPath)).build();
            ImmutableSortedSet<TaskSelector> taskSelectorsOfProject = ImmutableSortedSet.orderedBy(TaskSelectorComparator.INSTANCE).addAll(taskSelectors.get(projectPath)).build();
            mapping.put(projectPath, SyntheticBuildInvocations.from(projectTasksOfProject, taskSelectorsOfProject));
        }

        return ImmutableMap.copyOf(mapping);
    }

    private static ImmutableMultimap<String, GradleTask> buildProjectTasksRecursively(GradleProject project, Multimap<String, GradleTask> projectTasksPerProject) {
        // add tasks of the current project
        DomainObjectSet<? extends GradleTask> projectTasks = project.getTasks();
        for (GradleTask projectTask : projectTasks) {
            projectTasksPerProject.put(project.getPath(), SyntheticGradleTask.from(projectTask));
        }

        // recurse into child projects and add their tasks
        DomainObjectSet<? extends GradleProject> childProjects = project.getChildren();
        for (GradleProject childProject : childProjects) {
            buildProjectTasksRecursively(childProject, projectTasksPerProject);
        }

        // return the tasks grouped by project path
        return ImmutableMultimap.copyOf(projectTasksPerProject);
    }

    @SuppressWarnings("StringEquality")
    private static ImmutableMultimap<String, TaskSelector> buildTaskSelectorsRecursively(GradleProject project, Multimap<String, TaskSelector> taskSelectorsPerProject) {
        // add task selectors of the current project
        TreeBasedTable<String, String, String> aggregatedTasksWithDescription = TreeBasedTable.create(Ordering.usingToString(), new TaskNameComparator());
        Set<String> publicTasks = Sets.newLinkedHashSet();
        collectAllTasksRecursively(project, aggregatedTasksWithDescription, publicTasks);
        for (String selectorName : aggregatedTasksWithDescription.rowKeySet()) {
            SortedMap<String, String> pathsAndDescriptions = aggregatedTasksWithDescription.row(selectorName);
            String description = pathsAndDescriptions.get(pathsAndDescriptions.firstKey()); // description from project task with smallest path
            Set<String> fqnTaskNames = pathsAndDescriptions.keySet();
            taskSelectorsPerProject.put(project.getPath(), SyntheticTaskSelector.from(
                    selectorName,
                    String.format("%s in %s and subprojects.", selectorName, project.toString()),
                    description != NULL_STRING ? description : null,
                    publicTasks.contains(selectorName),
                    fqnTaskNames));
        }

        // recurse into child projects and add their task selectors
        DomainObjectSet<? extends GradleProject> childProjects = project.getChildren();
        for (GradleProject childProject : childProjects) {
            buildTaskSelectorsRecursively(childProject, taskSelectorsPerProject);
        }

        // return the task selectors grouped by project path
        return ImmutableMultimap.copyOf(taskSelectorsPerProject);
    }

    private static void collectAllTasksRecursively(GradleProject project, Table<String, String, String> tasksWithDescription, Collection<String> publicTasks) {
        for (GradleTask task : project.getTasks()) {
            // 1) store the path since the task selectors keep all the paths of the tasks they select
            // 2) store the description first by task name and then by path
            //      this allows to later fish out the description of the task whose name matches the selector name and
            //      whose path is the smallest for the given task name (the first entry of the table column)
            //      store null description as empty string to avoid that Guava chokes
            tasksWithDescription.put(task.getName(), task.getPath(), task.getDescription() != null ? task.getDescription() : NULL_STRING);

            // visible tasks are specified by Gradle as those that have a non-empty group
            if (isPublic(task)) {
                publicTasks.add(task.getName());
            }
        }

        for (GradleProject childProject : project.getChildren()) {
            collectAllTasksRecursively(childProject, tasksWithDescription, publicTasks);
        }
    }

    private static boolean isPublic(GradleTask task) {
        try {
            return task.isPublic();
        } catch (Exception e) {
            // expected exception for Gradle versions < 2.1
            // treat those tasks as being public
            return true;
        }
    }

    private static final class TaskComparator implements Comparator<GradleTask> {

        private static final TaskComparator INSTANCE = new TaskComparator();

        private TaskComparator() {
        }

        @Override
        public int compare(GradleTask one, GradleTask two) {
            return one.getPath().compareTo(two.getPath());
        }

    }

    private static final class TaskSelectorComparator implements Comparator<TaskSelector> {

        private static final TaskSelectorComparator INSTANCE = new TaskSelectorComparator();

        private TaskSelectorComparator() {
        }

        @Override
        public int compare(TaskSelector one, TaskSelector two) {
            return one.getName().compareTo(two.getName());
        }

    }

}
