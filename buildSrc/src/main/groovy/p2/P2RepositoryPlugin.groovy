package p2

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.Zip

class P2RepositoryPlugin implements Plugin<Project> {

    static final String TASK_GROUP_NAME = 'p2'
    static final String TASK_NAME_DOWNLOAD_ECLIPSE_SDK = 'downloadEclipseSdk'
    static final String TASK_NAME_PROCESS_BUNDLES = 'processOsgiBundles'
    static final String TASK_NAME_CREATE_P2_REPOSITORY = 'createP2Repository'
    static final String TASK_NAME_COMPRESS_P2_REPOSITORY = 'createCompressedP2Repository'

    static final String PLUGIN_CONFIGURATION_NAME = 'plugin'

    static final String BUNDLES_STAGING_FOLDER = 'tmp/bundles'
    static final String P2_REPOSITORY_FOLDER = 'repository'

    private NamedDomainObjectContainer<BundleInfo> bundleInfos

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(BasePlugin.class)

        configureExtensions(project)
        configureConfigurations(project)

        addDownloadEclipseSdkTask(project)
        addProcessOsgiBundlesTask(project)
        addCreateP2RepositoryTask(project)
        addCompressP2RepositoryTask(project)
    }

    private void addDownloadEclipseSdkTask(Project project) {
        project.tasks.create(TASK_NAME_DOWNLOAD_ECLIPSE_SDK, DownloadEclipseSdkTask) {
            group = TASK_GROUP_NAME
            description = "Downloads an Eclipse SDK to perform P2 operations with."
        }
    }

    private void addProcessOsgiBundlesTask(Project project) {
        project.tasks.create(TASK_NAME_PROCESS_BUNDLES, ProcessOsgiBundlesTask) {
            group = TASK_GROUP_NAME

            dependsOn project.getConfigurations().getByName(PLUGIN_CONFIGURATION_NAME)
            project.afterEvaluate { bundles = bundleInfos.toList() }
            target = new File(project.buildDir, "$BUNDLES_STAGING_FOLDER/plugins")
        }
    }

    private void addCreateP2RepositoryTask(Project project) {
         project.tasks.create(TASK_NAME_CREATE_P2_REPOSITORY, CreateP2RepositoryTask) {
            group = TASK_GROUP_NAME
            dependsOn TASK_NAME_DOWNLOAD_ECLIPSE_SDK
            dependsOn TASK_NAME_PROCESS_BUNDLES

            bundleSourceDir = new File(project.buildDir, BUNDLES_STAGING_FOLDER)
            targetRepositoryDir = new File(project.buildDir, P2_REPOSITORY_FOLDER)
        }
    }

    private void addCompressP2RepositoryTask(Project project) {
        project.tasks.create(TASK_NAME_COMPRESS_P2_REPOSITORY, Zip) {
            group = TASK_GROUP_NAME
            dependsOn TASK_NAME_CREATE_P2_REPOSITORY

            from new File(project.buildDir, P2_REPOSITORY_FOLDER)
            archiveName = 'repository.zip'
        }
    }

    private void configureExtensions(Project project) {
        bundleInfos = project.container(BundleInfo)
        project.extensions.p2Repository = bundleInfos
    }

    private void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations()
        configurations.create(PLUGIN_CONFIGURATION_NAME)
            .setVisible(false)
            .setTransitive(false)
            .setDescription("Classpath for deployable plugin jars, not transitive")
    }
}
