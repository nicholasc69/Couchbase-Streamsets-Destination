/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.Label;

/**
 * Couchbase Document Key Emuns for Couchbase Destination Configuration Tab
 * @author nickc
 */
public enum CouchbaseDocumentKeyTypes implements Label {
    FIELD("Field in Record"),
    CUSTOM_DOC_KEY("Custom Document Key");
    
    private final String label;

    CouchbaseDocumentKeyTypes(String label) {
        this.label = label;
    }
    
    @Override
    public String getLabel() {
        return this.label;
    }
    
}
