package p2

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class BundleInfo implements Serializable {
    final String name
    String bundleVersion
    List<String> resources = []
    String manifestTemplate

    BundleInfo(String name) {
        this.name = name
    }
    public void resource(File resource) {
        resources.add(resource.absolutePath)
    }
}
