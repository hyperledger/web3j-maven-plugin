# web3j-maven-plugin
[![Build Status](https://travis-ci.org/web3j/web3j-maven-plugin.svg?branch=master)](https://travis-ci.org/web3j/web3j-maven-plugin)
[![codecov.io](https://codecov.io/github/web3j/web3j-maven-plugin/coverage.svg?branch=master)](https://codecov.io/github/web3j/web3j-maven-plugin?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

web3j maven plugin is used to create java classes based on the solidity contract files.

## Usage
The base configuration for the plugin will take the solidity files from `src/main/resources` and generates the java classes into the folder `src/main/java`.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.web3j</groupId>
            <artifactId>web3j-maven-plugin</artifactId>
            <version>0.1.1</version>
            <configuration>
                <soliditySourceFiles/>
            </configuration>
        </plugin>
    </plugins>
</build>
```

to run the plugin execute the goal `generate-sources`
```bash
mvn web3j:generate-sources
```


## Configuration
The are several variable to select the solidity source files, define a source destination path or change the package name.

| Name                   | Format                                         | Default value       |
| -----------------------|------------------------------------------------| --------------------|
| `<packageName/>`       | valid java pacakge name                        | `org.web3j.model`   |
| `<sourceDestination/>` | relativ or absolut path                        | `src/main/java`     |
| `<soliditySourceFiles>`| Standard maven [fileset](https://maven.apache.org/shared/file-management/fileset.html)  | `<soliditySourceFiles>`<br>`  <directory>src/main/resources</directory>`<br>`  <includes>`<br>`    <include>**/*.sol</include>`<br>`  </includes>`<br>`</soliditySourceFiles>`   |

## Example

`pom.xml` `<plugin>` configuration:

```xml
<plugin>
    <groupId>org.web3j</groupId>
    <artifactId>web3j-maven-plugin</artifactId>
    <version>0.1.1</version>
    <configuration>
        <packageName>com.zuehlke.blockchain.model</packageName>
        <sourceDestination>src/main/java/generated</sourceDestination>
        <soliditySourceFiles>
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*.sol</include>
            </includes>
        </soliditySourceFiles>
    </configuration>
</plugin>
```

`mvn web3j:generate-sources` output
```
[INFO] --- web3j-maven-plugin:0.1.1:generate-sources (default-cli) @ hotel-showcase ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.061 s
[INFO] Finished at: 2017-06-01T10:42:45+02:00
[INFO] Final Memory: 16M/261M
[INFO] ------------------------------------------------------------------------

Process finished with exit code 0
```

## Releases

### 0.1.1
 * Initial Release