/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Label;

/**
 * Couchbase Version Emuns for Couchbase Destination Configuration Tab
 * @author nickc
 */
@GenerateResourceBundle
public enum CouchbaseVersionTypes implements Label {
    VERSION4("Version 4"),
    VERSION5("Version 5");
    
    private final String label;

    CouchbaseVersionTypes(String label) {
        this.label = label;
    }
    
    @Override
    public String getLabel() {
        return this.label;
    }
}
