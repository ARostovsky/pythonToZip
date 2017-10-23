/**
 * Created by hatari on 07.07.17.
 */


def main() {
    List<String> versions = [
//            "2.4",
//            "2.4.1",
//            "2.4.2",
//            "2.4.3",
//            "2.4.4",
//            "2.4.5",
//            "2.4.6",
//            "2.5",
//            "2.5.1",
//            "2.5.2",
//            "2.5.3",
//            "2.5.4",
//            "2.6.6",
//            "2.7",
//            "2.7.1",
//            "2.7.2",
//            "2.7.3",
//            "2.7.4",
//            "2.7.5",
//            "2.7.6",
//            "2.7.7",
//            "2.7.8",
//            "2.7.9",
//            "2.7.10",
//            "2.7.11",
//            "2.7.12",
//            "2.7.13",
            "2.7.14",
//            "3.0.1",
//            "3.1",
//            "3.1.1",
//            "3.1.2",
//            "3.1.3",
//            "3.1.4",
//            "3.1.5",
//            "3.2",
//            "3.2.1",
//            "3.2.2",
//            "3.2.3",
//            "3.2.4",
//            "3.2.5",
//            "3.2.6",
//            "3.3.0",
//            "3.3.1",
//            "3.3.2",
//            "3.3.3",
//            "3.3.4",
//            "3.3.5",
//            "3.4.0",
//            "3.4.1",
//            "3.4.2",
//            "3.4.3",
//            "3.4.4",
//            "3.5.0",
//            "3.5.1",
//            "3.5.2",
//            "3.5.3",
//            "3.6.0",
//            "3.6.1",
//            "3.6.2",
            "3.6.3"
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