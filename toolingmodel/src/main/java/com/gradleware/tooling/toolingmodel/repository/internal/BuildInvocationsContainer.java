package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Holds the {@code Model&lt;BuildInvocationFields&gt;} for a given set of projects. Each project is identified by its unique full path.
 *
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, Model&lt;BuildInvocationFields&gt;&gt;</code>.
 */
public final class BuildInvocationsContainer {

    @SuppressWarnings("RedundantStringConstructorCall")
    private static final String NULL_STRING = new String(); // ensure unique instance to use it as a null-string placeholder

    private final ImmutableSortedMap<String, OmniBuildInvocations> buildInvocationsPerProject;

    private BuildInvocationsContainer(SortedMap<String, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    /**
     * A {@code Map} of {@code OmniBuildInvocations} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    public ImmutableSortedMap<String, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    /**
     * Converts a {@code Map} of {@link BuildInvocations} to a {@link BuildInvocationsContainer}.
     *
     * @param buildInvocations the build invocations to convert
     * @return the build invocations container
     */
    public static BuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocations) {
        ImmutableSortedMap.Builder<String, OmniBuildInvocations> buildInvocationsMap = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        for (String projectPath : buildInvocations.keySet()) {
            buildInvocationsMap.put(projectPath, DefaultOmniBuildInvocations.from(buildInvocations.get(projectPath), projectPath));
        }
        return new BuildInvocationsContainer(buildInvocationsMap.build());
    }

    /**
     * Converts a {@link GradleProject} to a {@link BuildInvocationsContainer}.
     *
     * @param project the Gradle project to convert
     * @param enforceAllTasksPublic if set to true {@code true}, all tasks should be made public
     * @return the build invocations container
     */
    public static BuildInvocationsContainer from(GradleProject project, boolean enforceAllTasksPublic) {
        ImmutableMultimap<String, OmniProjectTask> tasks = buildProjectTasksRecursively(project, ArrayListMultimap.<String, OmniProjectTask>create(), enforceAllTasksPublic);
        ImmutableMultimap<String, OmniTaskSelector> taskSelectors = buildTaskSelectorsRecursively(project, ArrayListMultimap.<String, OmniTaskSelector>create(), enforceAllTasksPublic);
        ImmutableSortedMap<String, OmniBuildInvocations> buildInvocationsMap = buildBuildInvocationsMapping(tasks, taskSelectors);
        return new BuildInvocationsContainer(buildInvocationsMap);
    }

    private static ImmutableSortedMap<String, OmniBuildInvocations> buildBuildInvocationsMapping(Multimap<String, OmniProjectTask> projectTasks,
                                                                                                 Multimap<String, OmniTaskSelector> taskSelectors) {
        Preconditions.checkState(taskSelectors.keySet().containsAll(projectTasks.keySet()), "Task selectors are always configured for all projects");

        ImmutableSortedMap.Builder<String, OmniBuildInvocations> mapping = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        for (String projectPath : taskSelectors.keySet()) {
            ImmutableList<OmniProjectTask> projectTasksOfProject = ImmutableSortedSet.orderedBy(TaskComparator.INSTANCE).addAll(projectTasks.get(projectPath)).build().asList();
            ImmutableList<OmniTaskSelector> taskSelectorsOfProject = ImmutableSortedSet.orderedBy(TaskSelectorComparator.INSTANCE).addAll(taskSelectors.get(projectPath)).build().asList();
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(projectTasksOfProject, taskSelectorsOfProject));
        }
        return mapping.build();
    }

    private static ImmutableMultimap<String, OmniProjectTask> buildProjectTasksRecursively(GradleProject project, Multimap<String, OmniProjectTask> tasksPerProject, boolean enforceAllTasksPublic) {
        // add tasks of the current project
        for (GradleTask task : project.getTasks()) {
            tasksPerProject.put(project.getPath(), DefaultOmniProjectTask.from(task, enforceAllTasksPublic));
        }

        // recurse into child projects and add their tasks
        for (GradleProject childProject : project.getChildren()) {
            buildProjectTasksRecursively(childProject, tasksPerProject, enforceAllTasksPublic);
        }

        // return the tasks grouped by project path
        return ImmutableMultimap.copyOf(tasksPerProject);
    }

    @SuppressWarnings("StringEquality")
    private static ImmutableMultimap<String, OmniTaskSelector> buildTaskSelectorsRecursively(GradleProject project, Multimap<String, OmniTaskSelector> taskSelectorsPerProject, boolean enforceAllTasksPublic) {
        // add task selectors of the current project
        TreeBasedTable<String, String, String> aggregatedTasksWithDescription = TreeBasedTable.create(Ordering.usingToString(), PathComparator.INSTANCE);
        Set<String> publicTasks = Sets.newLinkedHashSet();
        collectAllTasksRecursively(project, aggregatedTasksWithDescription, publicTasks, enforceAllTasksPublic);
        for (String selectorName : aggregatedTasksWithDescription.rowKeySet()) {
            SortedMap<String, String> pathsAndDescriptions = aggregatedTasksWithDescription.row(selectorName);
            String description = pathsAndDescriptions.get(pathsAndDescriptions.firstKey()); // description from project task with smallest path
            SortedSet<String> fqnTaskNames = ImmutableSortedSet.orderedBy(PathComparator.INSTANCE).addAll(pathsAndDescriptions.keySet()).build();

            OmniTaskSelector taskSelector = DefaultOmniTaskSelector.from(
                    selectorName,
                    description != NULL_STRING ? description : null,
                    project.getPath(),
                    publicTasks.contains(selectorName),
                    fqnTaskNames);

            taskSelectorsPerProject.put(project.getPath(), taskSelector);
        }

        // recurse into child projects and add their task selectors
        DomainObjectSet<? extends GradleProject> childProjects = project.getChildren();
        for (GradleProject childProject : childProjects) {
            buildTaskSelectorsRecursively(childProject, taskSelectorsPerProject, enforceAllTasksPublic);
        }

        // return the task selectors grouped by project path
        return ImmutableMultimap.copyOf(taskSelectorsPerProject);
    }

    private static void collectAllTasksRecursively(GradleProject project, Table<String, String, String> tasksWithDescription, Collection<String> publicTasks, boolean enforceAllTasksPublic) {
        for (GradleTask task : project.getTasks()) {
            // convert to OmniProjectTask to have the version-specific logic and default-values applied
            OmniProjectTask projectTask = DefaultOmniProjectTask.from(task, enforceAllTasksPublic);

            // 1) store the path since the task selectors keep all the paths of the tasks they select
            // 2) store the description first by task name and then by path
            //      this allows to later fish out the description of the task whose name matches the selector name and
            //      whose path is the smallest for the given task name (the first entry of the table column)
            //      store null description as empty string to avoid that Guava chokes
            tasksWithDescription.put(projectTask.getName(), projectTask.getPath(), projectTask.getDescription() != null ? projectTask.getDescription() : NULL_STRING);

            // visible tasks are specified by Gradle as those that have a non-empty group
            if (projectTask.isPublic()) {
                publicTasks.add(task.getName());
            }
        }

        for (GradleProject childProject : project.getChildren()) {
            collectAllTasksRecursively(childProject, tasksWithDescription, publicTasks, enforceAllTasksPublic);
        }
    }

    private static final class TaskComparator implements Comparator<OmniProjectTask> {

        private static final TaskComparator INSTANCE = new TaskComparator();

        @Override
        public int compare(OmniProjectTask o1, OmniProjectTask o2) {
            return PathComparator.INSTANCE.compare(o1.getName(), o2.getName());
        }

    }

    private static final class TaskSelectorComparator implements Comparator<OmniTaskSelector> {

        private static final TaskSelectorComparator INSTANCE = new TaskSelectorComparator();

        @Override
        public int compare(OmniTaskSelector o1, OmniTaskSelector o2) {
            return PathComparator.INSTANCE.compare(o1.getName(), o2.getName());
        }

    }
}
