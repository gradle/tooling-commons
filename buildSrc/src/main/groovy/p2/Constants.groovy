package p2

import org.gradle.internal.os.OperatingSystem

 class Constants {

    static URL getEclipseSdkDownloadUrl() {
        def os = OperatingSystem.current()
        def arch = System.getProperty("os.arch").contains("64") ? "64" : "32"
        def downloadUrl
        if (os.windows) {
            downloadUrl = "http://builds.gradle.org:8000/eclipse/sdk/eclipse-sdk-4.4.2-windows-${arch}.zip"
        } else if (os.macOsX) {
            downloadUrl = "http://builds.gradle.org:8000/eclipse/sdk/eclipse-sdk-4.4.2-macosx-${arch}.tar.gz"
        } else if (os.linux) {
            downloadUrl = "http://builds.gradle.org:8000/eclipse/sdk/eclipse-sdk-4.4.2-linux-${arch}.tar.gz"
        } else {
            throw new RuntimeException("Unsupported operating system: ${os.familyName}")
        }
        return new URL(downloadUrl)
    }

    static File getEclipseSdkArchive() {
        new File(eclipseSdkDir, OperatingSystem.current().isWindows() ? 'eclipse-sdk.zip' : 'eclipse-sdk.tar.gz')
    }

    static File getEclipseSdkDir() {
        new File(targetPlatformsDir, 'eclipse-sdk')
    }

    static File getEclipseSdkExe() {
        OperatingSystem os = OperatingSystem.current()
        def path = os.isLinux() ? "eclipse/eclipse" :
            os.isWindows() ? "eclipse/eclipse.exe" :
            os.isMacOsX() ? "eclipse/Eclipse.app/Contents/MacOS/eclipse" :
            null
        new File(eclipseSdkDir, path)
    }

    static File getTargetPlatformsDir() {
        new File(System.getProperty('user.home'), '.tooling/eclipse/targetPlatforms')
    }

}
