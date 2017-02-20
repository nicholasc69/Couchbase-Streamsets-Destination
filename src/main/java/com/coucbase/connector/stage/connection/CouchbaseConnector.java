/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coucbase.connector.stage.connection;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.connector.stage.destination.CouchbaseConnectorTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Nick Cadehead
 */
public class CouchbaseConnector {
    private Cluster cluster;
    private ClusterManager clusterManager;
    private Bucket bucket;
    
    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseConnectorTarget.class);
    
    private CouchbaseConnector(String urlString, String usernameString, String passwordString, String bucketString) {
        connectToCouchbaseServer(urlString, usernameString, passwordString, bucketString);
    }
    
    
    private void connectToCouchbaseServer(String urlString, String usernameString, String passwordString, String bucketString) {
        //Connect to Couchbase Cluster
        cluster = CouchbaseCluster.create(urlString);
        clusterManager = cluster.clusterManager(usernameString, passwordString);
        
        //Now lets open the bucket
        bucket = cluster.openBucket(bucketString);
       
    }
    
    public static CouchbaseConnector getInstance(String url, String username, String password, String bucket) {
        
        return new CouchbaseConnector(url, username, password, bucket);
    }
    
    public void writeToBucket(String documentKey, JsonObject jsonObject) {
        
        JsonDocument document = JsonDocument.create(documentKey, jsonObject);
        bucket.upsert(document);
    }
    
    public void writeToBucket(String jsonObject) {
        
        JsonStringDocument doc = JsonStringDocument.create(jsonObject);
        LOG.info("This is the JSON OBJECT - " + jsonObject);
        bucket.upsert(doc);
    }
}
