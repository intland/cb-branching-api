val cbHome: String by project

plugins {
    `java-library`
    idea
}

repositories {
    jcenter()
}

dependencies {
    compileOnly(fileTree("${cbHome}/tomcat/webapps/cb/WEB-INF/lib") { include("*.jar") })
    compileOnly(fileTree("${cbHome}/tomcat/lib") { include("*.jar") })
}
