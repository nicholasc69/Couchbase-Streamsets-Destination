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
package com.couchbase.connector.stage.origin;

import com.couchbase.connector.stage.connection.CouchbaseConnector;
import com.couchbase.client.core.event.CouchbaseEvent;
import com.couchbase.client.dcp.Client;
import com.couchbase.client.dcp.ControlEventHandler;
import com.couchbase.client.dcp.message.DcpMutationMessage;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.deps.io.netty.util.CharsetUtil;
import com.couchbase.client.dcp.DataEventHandler;
import com.couchbase.client.dcp.StreamFrom;
import com.couchbase.client.dcp.StreamTo;
import com.couchbase.client.dcp.SystemEventHandler;
import com.couchbase.client.dcp.events.StreamEndEvent;
import com.couchbase.client.dcp.message.DcpSnapshotMarkerRequest;
import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseSource;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source is an example and does not actually read from anywhere.
 * It does however, generate generate a simple record with one field.
 */
public abstract class CouchbaseConnectorSource extends BaseSource {

  /**
   * Gives access to the UI configuration of the stage provided by the {@link SampleDSource} class.
   */
    //public abstract String getConfig();
  
    private CouchbaseConnector connector;
    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseConnectorSource.class);
    private Client client = null;
    private String lastSourceOffset = "";
    
  @Override
  protected List<ConfigIssue> init() {
    // Validate configuration values and open any required resources.
    List<ConfigIssue> issues = super.init();
    
    //Connect to Couchbase DB
    LOG.info("Connecting to Couchbase with details: " + getURL() + " " + getBucket() + " " + getUsername());
    //connector = CouchbaseConnector.getInstance(getURL(), getUsername(), getPassword(), getBucket());
    client = Client.configure()
            .hostnames(getURL())
            .bucket(getBucket())
            .build();
    

    // If issues is not empty, the UI will inform the user of each configuration issue in the list.
    return issues;
  }

  /** {@inheritDoc} */
  @Override
  public void destroy() {
    // Clean up any open resources.
    super.destroy();
    connector = null;
    LOG.info("Closing connection to Couchbase");
    
  }
  
  /** {@inheritDoc} */
  /*
  @Override
  public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
    // Offsets can vary depending on the data source. Here we use an integer as an example only.
    LOG.info("Starting to process Couchbase JSON objects");
    
    String nextSourceOffset = lastSourceOffset;

    
    N1qlQueryResult result = connector.queryBucket(getDocumentType());
    Iterator<N1qlQueryRow> rowItr = result.iterator();
    
    
    int numRecords = 0;
    int recordSize = result.allRows().size();
    
    int maxBatch = Math.min(maxBatchSize, recordSize);
    

    // TODO: As the developer, implement your logic that reads from a data source in this method.

    // Create records and add to batch. Records must have a string id. This can include the source offset
    // or other metadata to help uniquely identify the record itself.
    try {
        while (rowItr.hasNext()) {
          Record record = processN1qlRow(rowItr.next(), nextSourceOffset);
          batchMaker.addRecord(record);
          nextSourceOffset = record.getHeader().getAttribute("medallion");
          ++numRecords;
          LOG.info("Added record  - " + numRecords + " " + nextSourceOffset); 
         
        }
    } catch (Exception e) {
        throw new StageException(Errors.SAMPLE_00, e.toString(), e);
    }
    

    return nextSourceOffset;
  }
  */
  
  @Override
  public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
      
    long nextSourceOffset = 0;
    lastSourceOffset = lastSourceOffset == null ? "" : lastSourceOffset;
    if (!lastSourceOffset.equals("")) {
      nextSourceOffset = Long.parseLong(lastSourceOffset);
    }
      
      final BatchMaker bm = batchMaker;
      
      int recordCount = 0;
      
      client.dataEventHandler(new DataEventHandler() {
            @Override
            public void onEvent(ByteBuf event) {
                if (DcpMutationMessage.is(event)) {
                    // Using the Java SDKs JsonObject for simple access to the document
                    JsonObject content = JsonObject.fromJson(
                            DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8)
                    );
                    LOG.info("Creating record with contence - " + content);
                    Record record = processN1qlRow(content);
                    bm.addRecord(record);
                }
                event.release();
            }
        });
      
      //Just needed to complete Client
        // Don't do anything with control events in this example
        client.controlEventHandler(new ControlEventHandler() {
            @Override
            public void onEvent(ByteBuf event) {
                if (DcpSnapshotMarkerRequest.is(event)) {
                    client.acknowledgeBuffer(event);
                }
                event.release();
            }
        });
        
        client.systemEventHandler(new SystemEventHandler() {
            @Override
            public void onEvent(CouchbaseEvent event) {
                if (event instanceof StreamEndEvent) {
                    StreamEndEvent streamEnd = (StreamEndEvent) event;
                    if (streamEnd.partition() == 42) {
                        LOG.info("Stream for partition 42 has ended (reason: " + streamEnd.reason() + ")");
                    }
                }
            }
        });
        
         // Connect the sockets
        client.connect().await();

        // Initialize the state (start now, never stop)
        client.initializeState(StreamFrom.BEGINNING, StreamTo.INFINITY).await();
        /*
        try {
            // Sleep and wait until the DCP stream has caught up with the time where we said "now".
            while (true) {
                if (client.sessionState().isAtEnd()) {
                    break;
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            throw new StageException(Errors.SAMPLE_00, e.toString(), e);
        }
        */
        
        // Start streaming on all partitions
        client.startStreaming().await();
        
        //client.disconnect().await();
        
        ++nextSourceOffset;
      
      return lastSourceOffset;
  }
  
  private Record processN1qlRow(N1qlQueryRow row, String lastSourceOffset) {
      
    JsonObject parentObject = row.value();
    
    JsonObject fieldsObject = parentObject.getObject(getDocumentType());
    
      
    Record record = getContext().createRecord(fieldsObject.toString() + lastSourceOffset);
      
    //Get Field Names from JSON
    Set<String> fieldNames = fieldsObject.getNames();
    Iterator<String> fieldNamesInt = fieldNames.iterator();
    
    
    //Loop throught and get names and create a row
    List<Field> rowList = new ArrayList<>();
    while (fieldNamesInt.hasNext()) {
        //Get field names and values
        String fieldName = fieldNamesInt.next();
        String fieldValue = fieldsObject.get(fieldName).toString();
        //System.out.println(fieldName + " - " + fieldValue);
        
        //Add the record
        Map<String, Field> map = new HashMap<>();
        map.put(fieldName, Field.create(fieldValue));
        
        Map<String, Field> cell = new HashMap<>();
        cell.put("header", Field.create(fieldName));
        cell.put("value", Field.create(fieldValue));
        rowList.add(Field.create(cell));
           
    }
    
    record.set(Field.create(rowList)); 
    return record;  
      
  }
  
  private Record processN1qlRow(JsonObject row) {
      
    JsonObject parentObject = row;
    
    JsonObject fieldsObject = row;
    LOG.info("JSON Object - " + fieldsObject);
    Record record = getContext().createRecord(fieldsObject.toString());
    
      
    //Get Field Names from JSON
    Set<String> fieldNames = fieldsObject.getNames();
    Iterator<String> fieldNamesInt = fieldNames.iterator();
    
    
    //Loop throught and get names and create a row
    List<Field> rowList = new ArrayList<>();
    while (fieldNamesInt.hasNext()) {
        //Get field names and values
        String fieldName = fieldNamesInt.next();
        String fieldValue = fieldsObject.get(fieldName).toString();
        //System.out.println(fieldName + " - " + fieldValue);
        
        //Add the record
        Map<String, Field> map = new HashMap<>();
        map.put(fieldName, Field.create(fieldValue));
        
        Map<String, Field> cell = new HashMap<>();
        cell.put("header", Field.create(fieldName));
        cell.put("value", Field.create(fieldValue));
        rowList.add(Field.create(cell));
           
    }
    
    record.set(Field.create(rowList)); 
    return record;  
      
  }
  
  
  //Configuration get methods
  public abstract String getURL();
  
  public abstract String getUsername();
  
  public abstract String getPassword();
  
  public abstract String getBucket();
  
  public abstract String getDocumentType();
  
  public abstract int getMaximumBatchSize();

}
