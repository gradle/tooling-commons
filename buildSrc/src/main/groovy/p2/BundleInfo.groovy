package p2

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class BundleInfo implements Serializable {
    final String name
    String bundleVersion
    String versionQualifier
    List<String> resources = []
    String manifestTemplate
    String filteredPackagesPattern

    BundleInfo(String name) {
        this.name = name
    }

    public void resource(File resource) {
        resources.add(resource.absolutePath)
    }
}
