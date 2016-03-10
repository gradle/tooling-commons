package p2

import java.util.zip.*
import org.gradle.api.*
import org.gradle.api.tasks.*

class PrepareOsgiBundlesTask extends DefaultTask {

    @InputDirectory
    File source

    @OutputDirectory
    File target

    @TaskAction
    void prepareOsgiBundles() {
        source.eachFileMatch(~/^.*\.jar$/) { File jar ->
            String key = project.p2Repository.bundleInfoMap.keySet().find { jar.name.contains(it) }
            if (!key) {
                // if no bundle info is associated then we assume it's already a plugin and copy it as is
                project.copy {
                    from jar
                    into target
                }
            } else {
                // update the plugin manifest then copy it
                BundleInfo bundle = project.p2Repository.bundleInfoMap[key]
                Set<String> packageNames = getPackageNames(jar)
                String fullVersion = "${bundle.bundleVersion}.${'v' + new Date().format('yyyyMMddkkmm')}"
                String manifest = manifestFor(bundle.manifestTemplate, packageNames, bundle.bundleVersion, fullVersion)

                File manifestFile = project.file("${project.buildDir}/tmp/manifests/${key}/META-INF/MANIFEST.MF")
                manifestFile.parentFile.mkdirs()
                manifestFile.text = manifest

                File osgiJar = new File(target, "osgi_${jar.name}")

                project.ant.zip(destfile: osgiJar) {
                    zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')
                }
                project.ant.zip(update: 'true', destfile: osgiJar) {
                    fileset(dir: manifestFile.parentFile.parentFile)
                    if (bundle.resources) {
                        fileset(dir: bundle.resources)
                    }
                }
            }
        }
    }

    String manifestFor(String manifestTemplate, Set<String> packageNames, String mainVersion, String fullVersion) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (packageNames.size() == 1) {
            manifest.append "Export-Package: ${packageNames[0]};version=\"${mainVersion}\"\n"
        } else if (packageNames.size() > 1) {
            manifest.append "Export-Package: ${packageNames[0]};version=\"${mainVersion}\",\n"
            for(i in 1..<(packageNames.size()-1)) {
                manifest.append " ${packageNames[i]};version=\"${mainVersion}\",\n"
            }
            manifest.append " ${packageNames[-1]};version=\"${mainVersion}\"\n"
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
