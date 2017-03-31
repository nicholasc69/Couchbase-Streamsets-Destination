/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;

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
  /**  
    @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "Administrator",
      label = "Username",
      displayPosition = 10,
      description = "Username of the Couchbase Administrator",
      group = "COUCHBASE_TARGET"
  )
  public String username;

  @Override
  public String getUsername() {
    return username;
  }
**/

    @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "Bucket",
      displayPosition = 10,
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
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "Password (for Bucket)",
      displayPosition = 10,
      description = "Password of the Couchbase Bucket",
      group = "COUCHBASE_TARGET"
  )
  public String password;

  /** {@inheritDoc} */
  @Override
  public String getPassword() {
    return password;
  }
   
  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "Unique Document Key Field",
      displayPosition = 10,
      description = "A field in the document/data which will be used as the unqiure document key in Couchbase",
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
      displayPosition = 10,
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
