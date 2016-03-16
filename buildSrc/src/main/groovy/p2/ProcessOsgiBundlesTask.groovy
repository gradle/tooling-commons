package p2

import org.gradle.api.artifacts.ResolvedArtifact

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
        allBundles().each { bundle ->
            if (!bundle.manifestTemplate) {
                // if no bundle info is associated then we assume it's already a plugin and copy it as is
                project.copy {
                    from bundle.location
                    into target
                }
            } else {
                // update the plugin manifest then copy it
                File jar = new File(bundle.location)
                Set<String> packageNames = packageNames(jar)
                String fullVersion = "${bundle.bundleVersion}.${'v' + new Date().format('yyyyMMddkkmm')}"
                String manifest = manifestFor(bundle.manifestTemplate, packageNames, bundle.bundleVersion, fullVersion)

                File manifestFile = project.file("${project.buildDir}/tmp/manifests/${bundle.name.replace(':','.')}/META-INF/MANIFEST.MF")
                manifestFile.parentFile.mkdirs()
                manifestFile.text = manifest

                File extraResources = project.file("${project.buildDir}/tmp/extraresources/${bundle.name.replace(':','.')}")
                extraResources.mkdirs()
                project.copy {
                    from bundle.resources
                    into extraResources
                }

                File osgiJar = new File(target, "osgi_${jar.name}")

                project.ant.zip(destfile: osgiJar) {
                    zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')
                }
                project.ant.zip(update: 'true', destfile: osgiJar) {
                    fileset(dir: manifestFile.parentFile.parentFile)
                    if (bundle.resources) {
                        fileset(dir: extraResources)
                    }
                }
            }
        }
}

List<BundleInfo> allBundles() {
    List<BundleInfo> result = []
    project.getConfigurations().getByName(P2RepositoryPlugin.PLUGIN_CONFIGURATION_NAME).resolvedConfiguration.firstLevelModuleDependencies.each {
        ResolvedArtifact artifact = it.moduleArtifacts.find { it.extension == 'jar' }
        if (artifact) {
            File jar = artifact.file
            String bundleName = bundleInfoName(it.moduleGroup, it.moduleName)
            BundleInfo bundleInfo = bundles.find { it.name == bundleName }
            if (!bundleInfo) {
                bundleInfo = new BundleInfo(name)
            } else {
                bundleInfo = BundleInfo.from(bundleInfo)
            }
            bundleInfo.location = jar.absolutePath
            result.add(bundleInfo)
        }
    }
    result
}

String bundleInfoName(String groupId, String artifactId) {
    groupId + ':' + artifactId
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

    Set<String> packageNames(File jar) {
        def result = [] as Set
        new ZipInputStream(new FileInputStream(jar)).withCloseable { zip ->
            ZipEntry e
            while (e = zip.nextEntry) {
                if (!e.directory && e.name.endsWith(".class")) {
                    int index = e.name.lastIndexOf('/')
                    if (index < 0) index = e.name.length()
                    String packageName = e.name.substring(0, index).replace('/', '.')
                    result.add(packageName)
                }
            }
        }
        result
    }
}
