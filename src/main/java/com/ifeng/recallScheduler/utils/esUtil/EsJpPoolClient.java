package com.ifeng.recallScheduler.utils.esUtil;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;

/**
 * Created by lilg1 on 2018/5/11.
 */

@Component
public class EsJpPoolClient implements FactoryBean<TransportClient> {

    private static final Logger logger = LoggerFactory.getLogger(EsJpPoolClient.class);

    private Settings settings;
    private TransportClient client;


    @PostConstruct
    private void initEsClient() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        settings = Settings.builder().put("cluster.name", "jpc_es")
                .put("client.transport.ignore_cluster_name", true).build();

        long start = System.currentTimeMillis();
        try {
            logger.warn("start init jpClient...");
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.76"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.77"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.78"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.84"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.85"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("10.90.9.86"), 9300));
        } catch (Exception e) {
            logger.error("connect to jpc es cluster error: {}", e);
            e.printStackTrace();
        }
        logger.info("initJpEsClient init cost:{}", System.currentTimeMillis() - start);
    }

    public TransportClient getClient() {
        if (client == null) {
            initEsClient();
        }
        return client;
    }

    @Override
    public TransportClient getObject() throws Exception {
        if (client == null) {
            initEsClient();
        }
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
