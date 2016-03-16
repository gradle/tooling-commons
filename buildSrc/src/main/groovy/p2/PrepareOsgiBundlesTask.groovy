package p2

import java.util.zip.*
import org.gradle.api.*
import org.gradle.api.tasks.*

class PrepareOsgiBundlesTask extends DefaultTask {

    @Input
    List<BundleInfo> bundles

    @InputDirectory
    File source

    @OutputDirectory
    File target

    @TaskAction
    void prepareOsgiBundles() {
        source.eachFileMatch(~/^.*\.jar$/) { File jar ->
            BundleInfo bundle = bundles.find { jar.name.contains(it.name) }
            if (!bundle) {
                // if no bundle info is associated then we assume it's already a plugin and copy it as is
                project.copy {
                    from jar
                    into target
                }
            } else {
                // update the plugin manifest then copy it
                Set<String> packageNames = getPackageNames(jar)
                String fullVersion = "${bundle.bundleVersion}.${'v' + new Date().format('yyyyMMddkkmm')}"
                String manifest = manifestFor(bundle.manifestTemplate, packageNames, bundle.bundleVersion, fullVersion)

                File manifestFile = project.file("${project.buildDir}/tmp/manifests/${bundle.name}/META-INF/MANIFEST.MF")
                manifestFile.parentFile.mkdirs()
                manifestFile.text = manifest

                File extraResources = project.file("${project.buildDir}/tmp/extraresources/${bundle.name}")
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

    String manifestFor(String manifestTemplate, Set<String> packageNames, String mainVersion, String fullVersion) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (!packageNames.isEmpty()) {
            String exportedPackages = packageNames.collect { " ${it};version=\"${mainVersion}\"" }.join(',\n')
            manifest.append "Export-Package:${exportedPackages}\n"
        }
        manifest.append "Bundle-Version: $fullVersion\n"
        manifest.toString()
    }

    Set<String> getPackageNames(File jar) {
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
