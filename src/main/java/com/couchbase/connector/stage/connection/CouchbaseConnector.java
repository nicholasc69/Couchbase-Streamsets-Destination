/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coucbase.connector.stage.connection;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.time.Delay;
import com.couchbase.client.dcp.util.retry.RetryBuilder;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.connector.stage.destination.CocuhbaseConnectorTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;


/**
 *
 * @author Nick Cadehead
 * @version 1.1
 * 
 * CouchbaseConnector is singleton class that manages all connection requirements to a
 * Couchbase bucket. 
 * 
 * CouchbaseConnector handles all CRUD operations for the Couchbase Destination.
 */
public class CouchbaseConnector {
    private Cluster cluster;
    private Bucket bucket;
    
    private int batchSize;
    private int recordCounter = 0;
    
    List<JsonDocument> documentList = new ArrayList<JsonDocument>();
   
    
    private static final Logger LOG = LoggerFactory.getLogger(CocuhbaseConnectorTarget.class);
    
    
     /**
     * CouchbaseConnector                           
     * <p>
     * Constructor methods which takes standard connection parameters for a Couchbase Cluster
     * <p>
     *
     * @param  urlString URL Endpoint to the Couchbase Cluster.          
     * @param  bucketString Couchbase Bucket Name
     * @param  passwordString Couchbase Bucket password
     */
    private CouchbaseConnector(String urlString, String passwordString, String bucketString) {
        connectToCouchbaseServer(urlString, passwordString, bucketString);
       
    }
    
    private void connectToCouchbaseServer(String urlString, String usernameString, String passwordString, String bucketString) {
        //Connect to Couchbase Cluster
        cluster = CouchbaseCluster.create(urlString);
        clusterManager = cluster.clusterManager(usernameString, passwordString);
        
        //Now lets open the bucket
        bucket = cluster.openBucket(bucketString);

    }
    
    private void connectToCouchbaseServer(String urlString, String passwordString, String bucketString) {
        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                .build();
        
        //Init Couchbase
        cluster  = CouchbaseCluster.create(env, urlString);
        
        bucket = cluster.openBucket(bucketString, passwordString);
        
    }
    
    public static CouchbaseConnector getInstance(String url, String username, String password, String bucket) {
        
        return new CouchbaseConnector(url, username, password, bucket);
    }

    
    public static CouchbaseConnector getInstance(String url, String password, String bucket) {
        
        return new CouchbaseConnector(url, password, bucket);
    }
    
        public static CouchbaseConnector getInstance(String url, String password, String bucket, int batchSize) {
        
        return new CouchbaseConnector(url, password, bucket, batchSize);
    }
        
     /**
     * writeToBucket 
     * <p>
     * writeToBucket synchronously upserts JSON documents into Couchbase
     * <p>
     *
     * @param  documentKey Unique key of the JSON Document.          
     * @param  jsonObject The JSON Object i.e document body.
     */    
    
    public void writeToBucket(String documentKey, JsonObject jsonObject) {
        
        JsonDocument document = JsonDocument.create(documentKey, jsonObject);
        bucket.upsert(document);
    }
    
    public void writeToBucket(String jsonObject) {
        
        JsonStringDocument doc = JsonStringDocument.create(jsonObject);
        LOG.info("Upserting JSON Document - " + jsonObject);
        bucket.upsert(doc);
    }
    
     /**
     * writeToBucketAsync 
     * <p>
     * writeToBucket asynchronously upserts JSON documents into Couchbase.
     * NB!!!! Work in progress.
     * <p>
     *
     * @param  documentKey Unique key of the JSON Document.          
     * @param  jsonObject The JSON Object i.e document body.
     */ 
    
    public void writeToBucketAsync(String documentKey, JsonObject jsonObject) {
        JsonDocument doc = JsonDocument.create(documentKey, jsonObject);
        //LOG.info("Upserting JSON Document - " + jsonObject);
        
        documentList.add(doc);
        bulkSet(documentList);
        documentList = new ArrayList<JsonDocument>();
               
    }
    
     /**
     * bulkSet 
     * <p>
     * bulkSet asynchronously bulk upserts JSON documents into Couchbase.
     * NB!!!! Work in progress.
     * <p>
     *
     * @param  documentKey Unique key of the JSON Document.          
     * @param  jsonObject The JSON Object i.e document body.
     */ 
    public void bulkSet(List<JsonDocument> docs) {
        final AsyncBucket asyncBucket = bucket.async();
        LOG.info("Writing Batch to Couchbase");
        Observable
                .from(docs)
                .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public Observable<JsonDocument> call(JsonDocument document) {
                        return asyncBucket.upsert(document)
                                .retryWhen(RetryBuilder
                                    .anyOf(BackpressureException.class)
                                    .delay(Delay.exponential(TimeUnit.MILLISECONDS, 100))
                                    .max(10)
                                    .build());
                    }
                })
                .last()
                .toBlocking()
                .single();
    }
    
    public N1qlQueryResult queryBucket(String documentType) {
    
        // Perform a N1QL Query
        N1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT * FROM " + documentType));
    
        return result;
    }
    
    public boolean closeConnection() {
        return bucket.close();
    }
             
}
