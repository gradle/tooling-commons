package p2

import org.gradle.api.file.CopySpec

class BundleInfo {
    String name
    String bundleVersion
    CopySpec resources
    String manifestTemplate

    BundleInfo(String name) {
        this.name = name
    }
}
