# codeBeamer Branching API Extension
Does add endpoints to create and list branches to the Swagger API.
Verified to be compatible with with codeBeamer 10.1-SP6 and 20.11.

## Installation
1. Copy cb-branching-api.jar found in releases to `<codeBeamer>/tomcat/webapps/cb/WEB-INF/lib`
2. Restart codeBeamer

## Usage
After installation the new endpoints can be found in the Swagger UI at <codeBeamer>/v3/swagger/editor.spr under the tag "Branches".
You can try the endpoint by adjusting the examples provided in the Swagger UI.

## Development
Set variable cbHome e.g. in `%HOMEPATH%/.gradle/gradle.properties` to point to your local codeBeamer installation.

Use `gradlew eclipse` or `gradlew idea` to create project configuration for your IDE.

Use gradlew assemble to build the .jar file that can be deployed into codeBeamer.
