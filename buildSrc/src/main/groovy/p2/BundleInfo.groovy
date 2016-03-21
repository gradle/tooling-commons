package p2

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(excludes = 'resources')
class BundleInfo implements Serializable {
    final String name
    String bundleVersion
    String versionQualifier
    String manifestTemplate
    String filteredPackagesPattern
    transient Closure resources

    BundleInfo(String name) {
        this.name = name
    }

    void resources(Closure resources) {
        this.resources = resources
    }

}
