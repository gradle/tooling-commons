package p2

import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.gradle.api.file.CopySpec;
import org.gradle.util.ConfigureUtil;;

@EqualsAndHashCode(excludes = 'resources')
class BundleInfo implements Serializable {
    final String name
    String bundleVersion
    String versionQualifier
    String manifestTemplate
    String filteredPackagesPattern
    transient CopySpec resources

    BundleInfo(String name, Project project) {
        this.name = name
        this.resources = project.copySpec()
    }

    void resources(Closure closure) {
        ConfigureUtil.configure(closure, resources)
    }

}
