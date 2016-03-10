package p2

import org.gradle.internal.os.OperatingSystem

 class Constants {

    static URL getEclipseSdkDownloadUrl() {
        OperatingSystem os =  OperatingSystem.current()
        String osInUrl
        String wsInUrl
        if (os.isWindows()) {
            osInUrl = 'win32'
            wsInUrl = 'win32'
        } else if (os.isLinux())  {
            osInUrl = 'linux'
            wsInUrl = 'gtk'
        } else if (os.isMacOsX()) {
            osInUrl = 'macosx'
            wsInUrl = 'cocoa'
        } else {
            throw new RuntimeException("Unsupported operating system: ${os.familyName}")
        }
        def archInUrl = OperatingSystem.current().nativePrefix == "x86" ? "" : "-x86_64";
        if (OperatingSystem.current().isWindows()) {
            return new URL("http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.4.2-201502041700/eclipse-SDK-4.4.2-win32${archInUrl}.zip&r=1")
        } else {
            return new URL("http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.4.2-201502041700/eclipse-SDK-4.4.2-${osInUrl}-${wsInUrl}${archInUrl}.tar.gz&r=1");
        }
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
