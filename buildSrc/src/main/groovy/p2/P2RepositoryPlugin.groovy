package p2

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class P2RepositoryPlugin implements Plugin<Project> {

        static class Extension {
            Map<String, BundleInfo> bundleInfoMap = [:]

            void bundleInfo(String jarName, Closure closure) {
                def pluginInfo = new BundleInfo()
                closure.delegate = pluginInfo
                closure()
                bundleInfoMap.put(jarName, pluginInfo)
            }
        }

        static final String TASK_GROUP_NAME = 'p2'
        static final String TASK_NAME_DOWNLOAD_ECLIPSE_SDK = 'downloadEclipseSdk'
        static final String TASK_NAME_COPY_BUNDLES = 'copyBundles'
        static final String TASK_NAME_PROCESS_BUNDLES = 'processBundles'
        static final String TASK_NAME_CREATE_P2_REPOSITORY = 'createP2Repository'
        static final String TASK_NAME_COMPRESS_P2_REPOSITORY = 'compressP2Repository'

        static final String PLUGIN_CONFIGURATION_NAME = 'plugin'

        static final String JARS_STAGING_FOLDER = 'tmp/jars'
        static final String BUNDLES_STAGING_FOLDER = 'tmp/bundles'
        static final String P2_REPOSITORY_FOLDER = 'repository'

        @Override
        public void apply(Project project) {
            project.getPluginManager().apply(BasePlugin.class)
            project.extensions.create('p2Repository', Extension)

            configureConfigurations(project)

            addTaskDownloadEclipseSdk(project)
            addTaskCopyBundles(project)
            addTaskProcessBundles(project)
            addTaskCreateP2Repository(project)
            addTaskCompressP2Repository(project)
        }

        private def addTaskDownloadEclipseSdk(Project project) {
            project.tasks.create(TASK_NAME_DOWNLOAD_ECLIPSE_SDK, DownloadEclipseSdkTask) {
                description = "Downloads an Eclipse SDK to perform P2 operations with."
            }
        }

    private def addTaskCopyBundles(Project project) {
       project.tasks.create(TASK_NAME_COPY_BUNDLES, Copy) {
            group = TASK_GROUP_NAME

            from project.getConfigurations().getByName(PLUGIN_CONFIGURATION_NAME)
            into new File(project.buildDir, "$JARS_STAGING_FOLDER/plugins")
        }
    }

    private def addTaskProcessBundles(Project project) {
        project.tasks.create(TASK_NAME_PROCESS_BUNDLES, PrepareOsgiBundlesTask) {
            group = TASK_GROUP_NAME
            dependsOn TASK_NAME_COPY_BUNDLES

            source = new File(project.buildDir, "$JARS_STAGING_FOLDER/plugins")
            target = new File(project.buildDir, "$BUNDLES_STAGING_FOLDER/plugins")
        }
    }

    private def addTaskCreateP2Repository(Project project) {
         project.tasks.create(TASK_NAME_CREATE_P2_REPOSITORY, CreateP2RepositoryTask) {
            group = TASK_GROUP_NAME
            dependsOn TASK_NAME_PROCESS_BUNDLES

            bundleSourceDir = new File(project.buildDir, BUNDLES_STAGING_FOLDER)
            targetRepositoryDir = new File(project.buildDir, P2_REPOSITORY_FOLDER)
        }
    }

    private def addTaskCompressP2Repository(Project project) {
        project.tasks.create(TASK_NAME_COMPRESS_P2_REPOSITORY, Zip) {
            group = TASK_GROUP_NAME
            dependsOn TASK_NAME_CREATE_P2_REPOSITORY
            from new File(project.buildDir, P2_REPOSITORY_FOLDER)
            archiveName = 'repository.zip'
        }
    }

    private void configureConfigurations(final Project project) {
        ConfigurationContainer configurations = project.getConfigurations()
        configurations.create(PLUGIN_CONFIGURATION_NAME)
            .setVisible(false)
            .setTransitive(false)
            .setDescription("Classpath for deployable plugin jars, not transitive")
    }
}
