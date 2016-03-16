package p2

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class BundleInfo implements Serializable {
    final String name
    String location
    String bundleVersion
    List<String> resources = []
    String manifestTemplate

    BundleInfo(String name) {
        this.name = name
    }

    public void resource(File resource) {
        resources.add(resource.absolutePath)
    }

    static BundleInfo from(BundleInfo source) {
        BundleInfo result = new BundleInfo(source.name)
        result.location = source.location
        result.bundleVersion = source.bundleVersion
        result.resources = source.resources
        result.manifestTemplate = source.manifestTemplate
        result
    }
}
