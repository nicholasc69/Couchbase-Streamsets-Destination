/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.base.BaseEnumChooserValues;
import com.couchbase.connector.stage.destination.CouchbaseVersionTypes;

/**
 *
 * @author nickc
 */
public class CouchbaseVersionChooserValues extends BaseEnumChooserValues<CouchbaseVersionTypes> {
    
    public CouchbaseVersionChooserValues() {
        super(
            CouchbaseVersionTypes.VERSION4,
            CouchbaseVersionTypes.VERSION5);
    }
    
}
