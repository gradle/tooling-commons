package p2

import groovy.transform.EqualsAndHashCode
import org.gradle.api.file.CopySpec

@EqualsAndHashCode(excludes = 'resources')
class BundleInfo implements Serializable {
    final String name
    String bundleVersion
    String versionQualifier
    String manifestTemplate
    String filteredPackagesPattern
    transient CopySpec resources

    BundleInfo(String name) {
        this.name = name
    }
}
