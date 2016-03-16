package p2

import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency

import java.util.regex.Pattern
import java.util.zip.*
import org.gradle.api.*
import org.gradle.api.tasks.*

class ProcessOsgiBundlesTask extends DefaultTask {

    @Input
    List<BundleInfo> bundles

    @OutputDirectory
    File target

    @TaskAction
    void processOsgiBundles() {
        dependencies().each { ResolvedDependency dependency ->
            ResolvedArtifact jarArtifact = jarArtifact(dependency)
            if (jarArtifact) {
                BundleInfo bundleInfo = findBundleInfo(dependency.moduleGroup, dependency.moduleName)
                if (!bundleInfo) {
                    // if no bundle info is associated then we assume it's already a plugin and copy it as is
                    project.copy {
                        from jarArtifact.file
                        into target
                    }
                } else {
                    // update the plugin manifest then copy it
                    File jar = jarArtifact.file
                    Set<String> packageNames = packageNames(jar, bundleInfo.filteredPackagesPattern)
                    String fullVersion = "${bundleInfo.bundleVersion}.${'v' + new Date().format('yyyyMMddkkmm')}"
                    String manifest = manifestFor(bundleInfo.manifestTemplate, packageNames, bundleInfo.bundleVersion, fullVersion)

                    File manifestFile = project.file("${project.buildDir}/tmp/manifests/${bundleInfo.name.replace(':','.')}/META-INF/MANIFEST.MF")
                    manifestFile.parentFile.mkdirs()
                    manifestFile.text = manifest

                    File extraResources = project.file("${project.buildDir}/tmp/extraresources/${bundleInfo.name.replace(':','.')}")
                    extraResources.mkdirs()
                    project.copy {
                        from bundleInfo.resources
                        into extraResources
                    }

                    File osgiJar = new File(target, "osgi_${jar.name}")

                    project.ant.zip(destfile: osgiJar) {
                        zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')
                    }
                    project.ant.zip(update: 'true', destfile: osgiJar) {
                        fileset(dir: manifestFile.parentFile.parentFile)
                        if (bundleInfo.resources) {
                            fileset(dir: extraResources)
                        }
                    }
                }
            }
        }
    }

    Set<ResolvedDependency> dependencies() {
        project.getConfigurations().getByName(P2RepositoryPlugin.PLUGIN_CONFIGURATION_NAME).resolvedConfiguration
                .firstLevelModuleDependencies
    }

    ResolvedArtifact jarArtifact(ResolvedDependency dependency) {
         dependency.moduleArtifacts.find { it.extension == 'jar' }
    }

    BundleInfo findBundleInfo(String groupId, String artifactId) {
        bundles.find { it.name == groupId + ':' + artifactId }
    }

    String manifestFor(String manifestTemplate, Set<String> packageNames, String mainVersion, String fullVersion) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (!packageNames.isEmpty()) {
            String exportedPackages = packageNames.collect { " ${it};version=\"${mainVersion}\"" }.join(',\n')
            manifest.append "Export-Package:${exportedPackages}\n"
        }
        manifest.append "Bundle-Version: $fullVersion\n"
        manifest.toString()
    }

    Set<String> packageNames(File jar, String filteredPackagesPattern) {
        def result = [] as Set
        Pattern filteredPackages = Pattern.compile(filteredPackagesPattern)
        new ZipInputStream(new FileInputStream(jar)).withCloseable { zip ->
            ZipEntry e
            while (e = zip.nextEntry) {
                if (!e.directory && e.name.endsWith(".class")) {
                    int index = e.name.lastIndexOf('/')
                    if (index < 0) index = e.name.length()
                    String packageName = e.name.substring(0, index).replace('/', '.')
                    if (!packageName.matches(filteredPackages)) {
                        result.add(packageName)
                    }
                }
            }
        }
        result
    }
}
