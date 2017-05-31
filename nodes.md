# web3j-maven-plugin nodes

## How to release
The release was done based on this tutorial: [Distribute Project Artifacts in Maven Central with Nexus OSS](http://www.sonatype.org/nexus/2014/10/08/distribute-project-artifacts-in-maven-central-with-nexus-oss/)

The official guide is located [here](http://central.sonatype.org/pages/ossrh-guide.html) and for signing the jar, [this](http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/) was very helpfully


### Preparation
#### Unix
Following the tutorial above

#### Windows
* Install gpg [Gpg4win](https://www.gnupg.org/download/index.de.html)
* Install git [git for windows](https://git-for-windows.github.io)
* Install Maven [Binary zip](https://maven.apache.org/download.cgi)
Add the binaries to the PATH variables. Hint: The gpg installation only add the path ```...\GnuPG\pub```` to the Environment variables.

### Configuration
Add following to the maven settings file (`.m2/settings.xml`):
 ```xml
   <servers>
     <server>
       <id>ossrh</id>
       <username>jira_id</username>
       <password>jira_pwd</password>
     </server>
   </servers>
 ```
 
 ```xml
   <profiles>
     <profile>
       <id>ossrh</id>
       <activation>
         <activeByDefault>true</activeByDefault>
       </activation>
       <properties>
         <gpg.executable>gpg2</gpg.executable>
         <gpg.passphrase>the_pass_phrase</gpg.passphrase>
       </properties>
     </profile>
   </profiles>
 ```


### Releasing
On Windows I could not managed it connect with ssh to github (upload with git and ssh worked, but not with the maven release plugin). So the https connection is used inside the `pom.xml`
```xml    
<scm>
  <url>https://github.com/web3j/${project.artifactId}.git</url>
</scm>  
```

with that in place, the release can be done as follow
```sh
mvn clean deploy
mvn release:clean release:prepare -Dusername=github_user_id -Dpassword=gitbub_passwird
mvn release:perform
```
