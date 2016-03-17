package p2

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

class DownloadEclipseSdkTask extends DefaultTask {

    @OutputDirectory
    File downloadLocation = Constants.eclipseSdkDir

    @TaskAction
    def downloadEclipseSdk() {
        // download the archive to the sdk dir
        File sdkArchive = Constants.eclipseSdkArchive
        project.logger.info("Download Eclipse SDK from '${Constants.eclipseSdkDownloadUrl}' to '${sdkArchive.absolutePath}'")
        project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: sdkArchive)

        // extract the downloaded archive to the same location
        project.logger.info("Extract '$sdkArchive' to '$sdkArchive.parentFile.absolutePath'")
        if (OperatingSystem.current().isWindows()) {
            project.ant.unzip(src: sdkArchive, dest: sdkArchive.parentFile, overwrite: true)
        } else {
            project.ant.untar(src: sdkArchive, dest: sdkArchive.parentFile, compression: "gzip", overwrite: true)
        }

        // make the sdk exe executable
        project.logger.info("Set '${Constants.eclipseSdkExe}' executable")
        Constants.eclipseSdkExe.setExecutable(true)
    }
}
