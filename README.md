#OrientDB 3.x JCA adapter 

For compatible Java EE 7+/JCA 1.7 servers.

## Overview

This JCA adapter provides OrientDB 3.x functionality to your Java EE or MicroProfile application.

- This adapter does not work with JTA transactions (as OrientDB itself does not)

- You can configure it as ODB client or ODB embedded server. Consult your sever documentation or how to set this JCA
configurations:
  
  - embeddedServerConfiguration: points to your XML server configuration file (if using embedded server)
  - orientdbHome: points to the directory where the DB files reside.

- You will also need to configure JNDI factory with url, user, password 

Here is an example configuration for deploying this adapter, where the OrientDB jars are deployed as shared library in [OpenLiberty](openliberty.io) server:

server.xml
```xml 
     <featureManager>
            <feature>jca-1.7</feature>           
        </featureManager>
    
        <library id="odbLib">
            <fileset dir="${server.config.dir}/odb" includes="*.jar" scanInterval="5s"/>
        </library>
        
    <resourceAdapter id="odbjca" location="${server.config.dir}jca/odbjca/odbjca-rar-${odbjca_version}.rar">
        <properties.odbjca embeddedServerConfiguration="${orientdb_home}/config/odb-server.xml" orientdbHome="${orientdb_home}"/>
        <classloader commonLibraryRef="odbLib"/>
    </resourceAdapter>

    <connectionFactory jndiName="eis/odbFactory">
        <properties.odbjca url="plocal:${database}"
                           username="admin"
                           password="admin"/>
    </connectionFactory>
    
    <application context-root="/"
                 type="war"
                 id="my_webapp"
                 location="my-webapp.war"
                 name="my web app">
            <classloader commonLibraryRef="odbLib" classProviderRef="odbjca"/>
     </application>
```

## How to build
 
This OrientDB JCA Adapter is build with Gradle :

> gradlew clean publishToMavenLocal

## How to deploy 
This OrientDB JCA Adapter is available from Maven central.
 The latest version is x.x.x=0.0.2 and is compiled against OrientDB 3.0.11.


Gradle:
> providedCompile "com.peruncs:orientdb-jca:x.x.x" 


Maven:
><dependency>
>   <groupId>com.peruncs</groupId>
>   <artifactId>orientdb-jca</artifactId>
>   <version>x.x.x</version>
></dependency>

There are 3 artifacts deployed in the Maven repo:
 - orientdb-jca-x.x.x.jar    - This is what you compile against.
 - orientdb-jca-x.x.x.rar    - The minimal resource adapter you need to deploy into you server. You need to provide the rest of the OrientDB jars into your server somehow.
 - orientdb-jca-x.x.x.rarall - The complete resource adapter you need to deploy into you server. Includes all the OrientDB jars. 
 
## How to use

Here is a typical usage example:

```
@RequestScoped
public class MyService

    @Inject
    private ODatabaseSession session;

    public String ping() {
        StringBuilder response = new StringBuilder("Accounts:\n");
        try (OResultSet rs = session.query("select from Account")) {
            while (rs.hasNext()) {
                OResult row = rs.next();
                String name = row.getProperty("name");
                response.append(name).append("\n");
            }
        }

        return response.toString();
    }
}
```
