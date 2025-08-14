# SVNClientAdapter

- Build Status: [https://travis-ci.org/subclipse/svnclientadapter](https://travis-ci.org/subclipse/svnclientadapter)
- Check / Download the latest version: [https://bintray.com/subclipse/maven/svnclientadapter/_latestVersion](https://bintray.com/subclipse/maven/svnclientadapter/_latestVersion)  

SVNClientAdapter is a high-level Java API for Subversion.  You can write your code to the SVNClientAdapter Java interfaces and then at runtime run it with any of the three adapter implementations:

1. [JavaHL](http://subversion.apache.org/docs/)
2. [Packages](http://subversion.apache.org/packages.html) (including command-line)
3. [SVNKit](http://svnkit.com)

The first two implementation require that the native Subversion packages and libraries are installed as the Java
code will use those libraries.  In the case of JavaHL it talks to the native library via JNI and the command line
it of course executes by running the command line process and capturing the stdout.  The command line adapter is
not actively maintained and does not currently implement many of the newer methods added over the last several years.

SVNKit is a pure-Java reverse-engineering of the Subversion API. SVNKit implements the JavaHL interface and
SVNClientAdapter uses the library via this interface.  Refer to the SVNKit site for Terms and Conditions.

## Examples
A sample project is maintained in this repository.  It shows how to initialize and use the library.

## License
The source code for this project is licensed under Apache 2.0 license.



