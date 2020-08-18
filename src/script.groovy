/**
 * Created by hatari on 07.07.17.
 */


def main() {
    List<String> versions = [
            "2.4", *(1..6).toList().collect { "2.4.$it" },
            "2.5", *(1..4).toList().collect { "2.5.$it" },
            "2.6.6",
            "2.7", *(1..18).toList().collect { "2.7.$it" },
            "3.0.1",
            "3.1", *(1..5).toList().collect { "3.1.$it" },
            "3.2", *(1..6).toList().collect { "3.2.$it" },
            *(0..4).toList().collect { "3.3.$it" },
            *(0..4).toList().collect { "3.4.$it" },
            *(0..3).toList().collect { "3.5.$it" },
            *(0..12).toList().collect { "3.6.$it" },
            *(0..9).toList().collect { "3.7.$it" },
            *(0..5).toList().collect { "3.8.$it" },
    ]

    AntBuilder ant = new AntBuilder()
    File buildFolder = new File("build")
    File outputFolder = new File("out")
    ant.mkdir(dir: buildFolder)
    ant.mkdir(dir: outputFolder)


    for (version in versions) {
        for (bit in ["", "amd64"]) {
            println("")
            String extension = version.substring(0, 1) == '3' && version.substring(2, 3).toInteger() >= 5 ? "exe" : "msi"

            String filename = "python-$version${bit ? (extension == "msi" ? "." : "-") + bit : ""}.$extension"
            println(filename)
            File installer = new File(buildFolder, filename)

            try {
                ant.get(dest: installer) {
                    url(url: "https://www.python.org/ftp/python/$version/$filename")
                }
            } catch (Exception) {
                println("$version failed")
                continue
            }

            File installDir = new File(buildFolder, "python-$version-${bit ? "64" : "32"}")
            if (extension == "msi") {
                ant.exec(executable: "msiexec") {
                    arg(line: "/i $installer /passive TARGETDIR=$installDir.absolutePath")
                }
            } else if (extension == "exe") {
                ant.mkdir(dir: installDir)
                ant.exec(executable: installer) {
                    arg(line: "/i /passive TargetDir=$installDir.absolutePath Include_launcher=0 InstallLauncherAllUsers=0 Shortcuts=0 AssociateFiles=0")
                }
            }

            File zip = new File(outputFolder, "python-${version}-${bit ? "64" : "32"}.zip")
            ant.zip(destfile: zip) {
                fileset(dir: installDir)
            }

            if (extension == "msi") {
                ant.exec(executable: "msiexec") {
                    arg(line: "/x $installer /passive TARGETDIR=$installDir.absolutePath")
                }
            } else if (extension == "exe") {
                ant.exec(executable: installer) {
                    arg(line: "/uninstall /passive TargetDir=$installDir.absolutePath")
                }
                ant.delete(dir: installDir)
            }
        }
    }
}


main()