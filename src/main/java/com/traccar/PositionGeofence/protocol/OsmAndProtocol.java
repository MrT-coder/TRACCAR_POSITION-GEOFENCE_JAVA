package com.traccar.PositionGeofence.protocol;

import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.TrackerServer;
import com.traccar.PositionGeofence.config.Config;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
@Component
public class OsmAndProtocol extends BaseProtocol {


    public OsmAndProtocol(Config config) {
        addServer(new TrackerServer(config, getName(), false) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline, Config config) {
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder());
                pipeline.addLast(new HttpObjectAggregator(16384));
                pipeline.addLast(new OsmAndProtocolDecoder(OsmAndProtocol.this));
            }
        });
    }

}
