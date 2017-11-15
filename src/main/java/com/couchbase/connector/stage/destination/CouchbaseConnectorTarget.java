/*
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

import com.couchbase.connector.stage.connection.CouchbaseConnector;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.connector.stage.lib.Errors;

import com.streamsets.pipeline.api.Batch;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseTarget;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.api.ext.json.Mode;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.lib.generator.DataGeneratorException;
import com.streamsets.pipeline.lib.generator.DataGeneratorFactoryBuilder;
import com.streamsets.pipeline.lib.generator.DataGeneratorFactory;
import com.streamsets.pipeline.lib.generator.DataGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This target is a used to connect to a Couchbase NoSQL Database.
 */
public abstract class CouchbaseConnectorTarget extends BaseTarget {
    
    private CouchbaseConnector connector;
    
    private DataGeneratorFactory generatorFactory;
    
    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseConnectorTarget.class);

  /** {@inheritDoc} */
  @Override
  protected List<ConfigIssue> init() {
    // Validate configuration values and open any required resources.
    List<ConfigIssue> issues = super.init();
    
    //Connect to Couchbase DB
    LOG.info("Connecting to Couchbase " + getCouchbaseVersion() +  " with details: " + getURL() + " " + getBucket());
    
    //Check Couchbase Version
    if (getCouchbaseVersion() == CouchbaseVersionTypes.VERSION4)
        connector = CouchbaseConnector.getInstance(getURL(), getBucket(), getBucketPassword());
    else
        connector = CouchbaseConnector.getInstance(getURL(), getBucket(), getUserName(), getUserPassword());
    
    //Data Generator for JSON Objects to Couchbase
    DataGeneratorFactoryBuilder builder = new DataGeneratorFactoryBuilder(
        getContext(),
        DataFormat.JSON.getGeneratorFormat()
    );
    builder.setCharset(StandardCharsets.UTF_8);
    builder.setMode(Mode.MULTIPLE_OBJECTS);
    generatorFactory = builder.build();

    // If issues is not empty, the UI will inform the user of each configuration issue in the list.
    return issues;
  }

  /** {@inheritDoc} */
  @Override
  public void destroy() {
    // Clean up any open resources.
    super.destroy();
    connector.closeConnection();
    connector = null;
    
  }

  /** {@inheritDoc} */
  @Override
  public void write(Batch batch) throws StageException {
     
    Iterator<Record> batchIterator = batch.getRecords();
    
    //Create a list of JSON documents
    List<JsonDocument> documentList = new ArrayList<JsonDocument>();
    
    //Create a List of JSON Document for Batch Iterator
    while (batchIterator.hasNext()) {
      Record record = batchIterator.next();
              
      try {
        //Get JsonDocument from Record
        JsonDocument doc = getJsonDocument(record);
        //Add to list
        //System.out.println(doc.content().get("ID"));
        documentList.add(doc);
        
      } catch (Exception e) {
        switch (getContext().getOnErrorRecord()) {
          case DISCARD:
            break;
          case TO_ERROR:
            getContext().toError(record, Errors.SAMPLE_01, e.toString());
            break;
          case STOP_PIPELINE:
            throw new StageException(Errors.SAMPLE_01, e.toString());
          default:
            throw new IllegalStateException(
                Utils.format("Unknown OnError value '{}'", getContext().getOnErrorRecord(), e)
            );
        }
      }
    }
    
    if (documentList.size() > 0) {
        LOG.info("Writing BATCH with " + documentList.size() + " number of records. ");

        //Write Batch to Couchbase
        connector.bulkSet(documentList); //Not working for some reason
        //connector.writeToBucket(documentList);
    }
    
    
  }

  /**
   * Writes a single record to the destination.
   *
   * @param record the record to write to the destination.
   * @throws OnRecordErrorException when a record cannot be written.
   */
  private void write(Record record) throws OnRecordErrorException {
    try {
        //Generate data from the record object and create JsonObject from byte ARRAY String   
        //LOG.info("Here is the record: " + record);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        DataGenerator generator = generatorFactory.getGenerator(baos);
        generator.write(record);
        generator.close();
        JsonObject jsonObject = JsonObject.fromJson(new String(baos.toByteArray()));
        
        //LOG.info("DATA - " + jsonObject);
        
        //Either get key JSON or generate unique one
        Object keyObject = null;
        
        if (generateDocumentKey()) {
            UUID uuid = UUID.randomUUID();
            keyObject = uuid.toString();
        }
        else {
            keyObject = jsonObject.get(getDocumentKey());
            if (keyObject == null)
                throw new NullPointerException("Document Key is Null");
        }
        
        String keyString = keyObject.toString();
        
        //Write to Couchbase DB
        //LOG.info("Writing record with key - " + keyString + " - to Couchbase");
        //connector.writeToBucketBatch(keyString, jsonObject);
        connector.writeToBucket(keyString, jsonObject);
        
    } catch (NullPointerException ne) {
        LOG.error(ne.getMessage());
    } catch (IOException ioe) {
        LOG.error(ioe.getMessage());
    } catch (DataGeneratorException dge) {
        LOG.error(dge.getMessage());
    }
  }
 
  private JsonDocument getJsonDocument(Record record) {
      JsonDocument doc = null;
      
      try {
        //Generate data from the record object and create JsonObject from byte ARRAY String   
        //LOG.info("Here is the record: " + record);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        DataGenerator generator = generatorFactory.getGenerator(baos);
        generator.write(record);
        generator.close();
        JsonObject jsonObject = JsonObject.fromJson(new String(baos.toByteArray()));
        
        //LOG.info("DATA - " + jsonObject);
        
        //Either get key JSON or generate unique one
        Object keyObject = null;
        
        if (generateDocumentKey()) {
            UUID uuid = UUID.randomUUID();
            keyObject = uuid.toString();
        }
        else {
            keyObject = jsonObject.get(getDocumentKey());
            if (keyObject == null)
                throw new NullPointerException("Document Key is Null");
        }
        
        String keyString = keyObject.toString();
        
        doc = JsonDocument.create(keyString, jsonObject);
        
      } catch (NullPointerException ne) {
            LOG.error(ne.getMessage());
      } catch (IOException ioe) {
            LOG.error(ioe.getMessage());
      } catch (DataGeneratorException dge) {
        LOG.error(dge.getMessage());
      }
        
      return doc;
  }
  
   //Configuration get methods
  public abstract String getURL();
  
  public abstract String getUserName();
  
  public abstract String getUserPassword();
  
  public abstract String getBucketPassword();
  
  public abstract String getBucket();
  
  public abstract String getDocumentKey();
  
  public abstract boolean generateDocumentKey();
  
  public abstract CouchbaseVersionTypes getCouchbaseVersion();

}
