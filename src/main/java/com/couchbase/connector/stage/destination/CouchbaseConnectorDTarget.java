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
    label = "Couchbase Destination",
    description = "Couchbase Destination",
    icon = "couchbase.png",
    recordsByRef = true,
    onlineHelpRefUrl = ""
)

@ConfigGroups(value = Groups.class)
@GenerateResourceBundle

public class CouchbaseConnectorDTarget extends CocuhbaseConnectorTarget {

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "default",
      label = "URL",
      displayPosition = 10,
      group = "COUCHBASE"
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
      defaultValue = "default",
      label = "Username",
      displayPosition = 10,
      group = "COUCHBASE"
  )
  public String username;

  /** {@inheritDoc} */
  @Override
  public String getUsername() {
    return username;
  }
  
    @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "default",
      label = "Password",
      displayPosition = 10,
      group = "COUCHBASE"
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
      defaultValue = "default",
      label = "Bucket",
      displayPosition = 10,
      group = "COUCHBASE"
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
      defaultValue = "default",
      label = "Unique Document Key Field",
      displayPosition = 10,
      group = "COUCHBASE"
  )
  public String documentKey;

  /** {@inheritDoc} */
  @Override
  public String getDocumentKey() {
    return documentKey;
  }

}
