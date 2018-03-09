/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.couchbase.connector.stage.destination;

import com.streamsets.pipeline.api.base.BaseEnumChooserValues;

/**
 *
 * @author nickc
 */
public class CouchbaseDocumentKeyChooserValues extends BaseEnumChooserValues<CouchbaseDocumentKeyTypes> {
        
    public CouchbaseDocumentKeyChooserValues() {
        super(
                CouchbaseDocumentKeyTypes.FIELD,
                CouchbaseDocumentKeyTypes.CUSTOM_DOC_KEY);
    }
}
