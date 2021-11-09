# Release
--------------------------------------------------------------------------------

\[[Up](README.md)\] \[[Top](#top)\]

--------------------------------------------------------------------------------

## Table of Content

1. [Documentation Update](#documentation-update)
1. [Post Process](#post-process)

## Documentation Update

* Update [THIRD-PARTY.txt](THIRD-PARTY.txt) and license downloads either
manually or by running if you are using Maven and Java:

    ```bash
    $ mvn -Pdocs-third-party generate-resources
    ```
  
  Your plugin root POM has to contain the following configuration to make this work:
  
  ```xml
    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>license-maven-plugin</artifactId>
            <version>2.0.0</version>
            <configuration>
              <cleanLicensesOutputDirectory>true</cleanLicensesOutputDirectory>
              <errorRemedy>ignore</errorRemedy>
              <excludedGroups>^com\.coremedia\.</excludedGroups>
              <excludeTransitiveDependencies>true</excludeTransitiveDependencies>
              <includeTransitiveDependencies>false</includeTransitiveDependencies>
              <licensesOutputFile>${docs.directory}/third-party-licenses/licenses.xml</licensesOutputFile>
              <licensesOutputDirectory>${docs.directory}/third-party-licenses</licensesOutputDirectory>
              <outputDirectory>${docs.directory}</outputDirectory>
              <sortByGroupIdAndArtifactId>true</sortByGroupIdAndArtifactId>
            </configuration>
          </plugin>
        </plugins>
      </pluginManagement>
    </build>
    
    <profiles>
      <profile>
        <id>docs-third-party</id>
        <!--
          Will create generated resources for docs/ folder.
        -->
        <build>
          <plugins>
            <plugin>
              <groupId>org.codehaus.mojo</groupId>
              <artifactId>license-maven-plugin</artifactId>
              <executions>
                <execution>
                  <id>generate-docs-licenses</id>
                  <goals>
                    <goal>aggregate-add-third-party</goal>
                    <goal>aggregate-download-licenses</goal>
                  </goals>
                  <phase>generate-resources</phase>
                </execution>
              </executions>
            </plugin>
          </plugins>
        </build>
      </profile>
    </profiles>
  ```

* Update badges at main workspace `README.md`.

* Describe the changes since the last release in `CHANGELOG.md`.

* If this release is not compatible with older CMCC versions, create a new
maintenance branch for those versions.  Branch from the state of the last release,
and name the branch after the oldest compatible CMCC version, e.g. `cmcc-10-2010`.

* Once the state that is to be released, use GitHub's release functionality to
create the tag and the release. And, don't forget to add some details about the
latest and greatest changes.

## Post Process

* Review GitHub issues and possibly adjust state.
* Close and possibly rename the milestone.
