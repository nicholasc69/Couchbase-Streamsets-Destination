package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooserModel;

@StageDef(
    version = 1,
    label = "Couchbase",
    description = "Couchbase Destination",
    icon = "couchbase.png",
    recordsByRef = true,
    onlineHelpRefUrl = ""
)

@ConfigGroups(value = Groups.class)
@GenerateResourceBundle

public class CouchbaseConnectorDTarget extends CouchbaseConnectorTarget {

    @ConfigDef(
        required = true,
        type = ConfigDef.Type.STRING,
        defaultValue = "localhost:8091",
        label = "URL",
        displayPosition = 10,
        description = "The URL endpoint of the Couchbase NoSQL Database Cluster",
        group = "COUCHBASE_TARGET"
    )
    public String URL;

    /** {@inheritDoc} */
    @Override
    public String getURL() {
      return URL;
    }

      @ConfigDef(
        required = true,
        type = ConfigDef.Type.STRING,
        defaultValue = "",
        label = "Bucket",
        displayPosition = 20,
        description = "Couchbase Destination Bucket to ingesting data",
        group = "COUCHBASE_TARGET"
      )
    public String bucket;

    /** {@inheritDoc} */
    @Override
    public String getBucket() {
      return bucket;
    }

    @ConfigDef(
        required = true,
        type = ConfigDef.Type.MODEL,
        defaultValue = "VERSION4",
        label = "Couchbase NoSQL Database Version",
        description = "Specify the version of the Couchbase NoSQL Database",
        displayPosition = 30,
        group = "COUCHBASE_TARGET"
    )
    @ValueChooserModel(CouchbaseVersionChooserValues.class)
    public CouchbaseVersionTypes version = CouchbaseVersionTypes.VERSION4; //Default

      /** {@inheritDoc} */
    @Override
    public CouchbaseVersionTypes getCouchbaseVersion() {
      return version;
    }
  
    //Version 4 Config  

    @ConfigDef(
        required = true,
        type = ConfigDef.Type.STRING,
        defaultValue = "",
        label = "Password (for Bucket)",
        displayPosition = 40,
        description = "Password of the Couchbase Version 4 Bucket",
        dependsOn = "version",
        triggeredByValue = "VERSION4",
        group = "COUCHBASE_TARGET"
    )
    public String bucketPassword;

    /** {@inheritDoc} */
    @Override
    public String getBucketPassword() {
      return bucketPassword;
    }

    //Version 5 Config

    @ConfigDef(
        required = true,
        type = ConfigDef.Type.STRING,
        defaultValue = "",
        label = "Couchbase User Name",
        displayPosition = 40,
        description = "Specify a Couchbase user name for connecting the bucket",
        dependsOn = "version",
        triggeredByValue = "VERSION5",
        group = "COUCHBASE_TARGET"
    )
    public String userName;

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
      return userName;
    }
    
    @ConfigDef(
        required = true,
        type = ConfigDef.Type.STRING,
        defaultValue = "",
        label = "Couchbase User Password",
        displayPosition = 50,
        description = "Specify a password for the Couchbase user name",
        dependsOn = "version",
        triggeredByValue = "VERSION5",
        group = "COUCHBASE_TARGET"
    )
    public String userPassword;

    /** {@inheritDoc} */
    @Override
    public String getUserPassword() {
      return userPassword;
    }


    @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "Unique Document Key Field",
      displayPosition = 50,
      description = "A field in the document/data which will be used as the unique document key in Couchbase",
      group = "COUCHBASE_TARGET"
    )

    public String documentKey;

    /** {@inheritDoc} */
    @Override
    public String getDocumentKey() {
      return documentKey;
    }

    @ConfigDef(
      required = false,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "Generate unique Document Key",
      displayPosition = 60,
      description = "Generate a unique document key if document key field cannot be set",
      group = "COUCHBASE_TARGET"
    )

    public boolean generateDocumentKey;

    /** {@inheritDoc} */
    @Override
    public boolean generateDocumentKey() {
        return generateDocumentKey;
    }
}
