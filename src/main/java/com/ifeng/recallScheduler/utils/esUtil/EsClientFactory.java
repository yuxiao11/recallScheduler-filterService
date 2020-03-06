package com.ifeng.recallScheduler.utils.esUtil;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Created by geyl on 2017/7/5.
 */
public class EsClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(EsClientFactory.class);

    private static Settings settings;
    private static TransportClient client;

    static {
        initEsClient();
    }

    private static void initEsClient() {
        settings = Settings.builder().put("cluster.name", "recom-es-cluster-ssd").build();
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.29.145"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.30.145"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.31.144"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.32.144"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.14.10"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.14.9"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.80.85.139"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.80.156"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.80.156"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.81.156"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.81.156"), 9301))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.82.156"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.82.156"), 9301));
        } catch (Exception e) {
            logger.error("connect to es cluster error: {}",e);
            e.printStackTrace();
        }
    }

    public static TransportClient getClient() {
        if (client == null) {
            logger.error("get es client error, client is null");
            return null;
        } else {
            return client;
        }
    }

    public static Settings getSettings() {
        if (client == null) {
            logger.error("get es setting error, client is null");
            return null;
        } else {
            return settings;
        }
    }
}