/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import com.couchbase.client.core.event.CouchbaseEvent;
import com.couchbase.client.dcp.*;
import com.couchbase.client.dcp.events.StreamEndEvent;
import com.couchbase.client.dcp.message.DcpMutationMessage;
import com.couchbase.client.dcp.message.DcpSnapshotMarkerRequest;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.util.CharsetUtil;
import com.couchbase.client.java.document.json.JsonObject;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * @author nickc
 */
public class TestClient {
    public static void main(String[] args) throws Exception {

        // Connect to localhost and use the travel-sample bucket
        final Client client = Client.configure()
                .hostnames("localhost")
                .bucket("travel-sample")
                .build();

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

        // Collect the statistics on mutations
        final AtomicInteger numAirports = new AtomicInteger(0);

        client.dataEventHandler(new DataEventHandler() {
            @Override
            public void onEvent(ByteBuf event) {
                if (DcpMutationMessage.is(event)) {
                    // Using the Java SDKs JsonObject for simple access to the document
                    JsonObject content = JsonObject.fromJson(
                            DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8)
                    );
                    if (content.getString("type").equals("airport")
                            && content.getString("country").toLowerCase().equals("france")) {
                        numAirports.incrementAndGet();
                    }
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
                        System.out.println("Stream for partition 42 has ended (reason: " + streamEnd.reason() + ")");
                    }
                }
            }
        });
        


        // Connect the sockets
        client.connect().await();

        // Initialize the state (start now, never stop)
        client.initializeState(StreamFrom.BEGINNING, StreamTo.NOW).await();

        // Start streaming on all partitions
        client.startStreaming().await();

        // Sleep and wait until the DCP stream has caught up with the time where we said "now".
        while (true) {
            if (client.sessionState().isAtEnd()) {
                break;
            }
            Thread.sleep(500);
        }

        System.out.println("Number of Airports in France: " + numAirports.get());

        // Proper Shutdown
        client.disconnect().await();
    }
}
