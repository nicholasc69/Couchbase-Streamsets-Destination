#Building Couchbase Connector for Streamset

To build the *Couchbase-Streamset-Connector* the following is required on the system:

* GIT
* Apache Maven 3.3.9
* Java 1.8


1. Get the project from GitHub.

    * Clone the project from GitHub using: https://github.com/nicholasc69/CouchbaseConnector.git

2. Build and package the Couchbase Connector

    * Build the Couchbase Connector using: mvn clean package

3. Copy the tar to the Streamset User Lib Directory

    * Copy the generated tar from target to the `<STREAMSET_DIR>/user-libs`
    *  Extract the contents of the tar in the user lib directory: `tar xvf "COUCHBASE_CONNECTOR.tar.gz"`

4. Give AllSecurityPermissions to user lib directory

   * The connector requires AllSecurityPermission in Java in order to function. Edit the following StreamSets configuration file to give security permissions.
    
   * Open the following security policy file: `<STREAMSET_DIR>/etc/sdc-security.policy`
   
   Scroll down until you find the following entry:
    
        //User stage libraries code base:
        grant codebase "file://${sdc.dist.dir}/user-libs/-" {
            permission java.util.PropertyPermission "*", "read,write";
            permission java.lang.RuntimePermission "accessDeclaredMembers,modifyThread";
            permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
            permission java.io.FilePermission "${sdc.dist.dir}/user-libs/streamsets-datacollector-dev-lib/lib/*", "read";
            permission java.net.SocketPermission "connect, resolve";
        };

    Comment out the above grant block and add the following:
    
        grant codebase "file://${sdc.dist.dir}/user-libs/-" {
            permission java.security.AllPermission;
        };


Restart StreamSets Data Collector. In the pipeline builder screen a Couchbase Destination is avaliable in Destinations section of the stage library 