package com.houjiang.goods.util.es;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
@Configuration
@RefreshScope
public class ElasticSearchConfig {


    /**
     * 协议
     */
    @Value("${houcloud.es.schema:http}")
    private String schema;
    /**
     * 集群地址，多个用“,”隔开；统一由 Nacos es.yaml 的 houcloud.es.uri 提供
     */
    @Value("${houcloud.es.uri}")
    private String address;
    /**
     * 账号；ES 未开启安全认证时留空
     */
    @Value("${houcloud.es.username:}")
    private String userName;
    /**
     * 密码；ES 未开启安全认证时留空
     */
    @Value("${houcloud.es.password:}")
    private String password;
    /**
     * 连接超时时间
     */
    @Value("${houcloud.es.connect-timeout:10000}")
    private int connectTimeout;
    /**
     * Socket 连接超时时间
     */
    @Value("${houcloud.es.socket-timeout:60000}")
    private int socketTimeout;
    /**
     * 获取连接的超时时间
     */
    @Value("${houcloud.es.connection-request-timeout:10000}")
    private int connectionRequestTimeout;
    /**
     * 最大连接数
     */
    @Value("${houcloud.es.max-conn-total:200}")
    private int maxConnectNum;
    /**
     * 最大路由连接数
     */
    @Value("${houcloud.es.max-conn-per-route:200}")
    private int maxConnectPerRoute;
    private RestHighLevelClient restHighLevelClient;

    /**
     * 批量操作的对象
     */
    private static BulkProcessor bulkProcessor;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 拆分地址
        List<HttpHost> hostLists = new ArrayList<>();
        String[] hostList = address.split(",");
        for (String addr : hostList) {
            String host = addr.split(":")[0];
            String port = addr.split(":")[1];
            hostLists.add(new HttpHost(host, Integer.parseInt(port), schema));
        }
        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);
        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            requestConfigBuilder.setSocketTimeout(socketTimeout);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
            return requestConfigBuilder;
        });
        // 异步连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
            // ES 未开启安全认证时账号为空，此时不注入 Basic Auth，避免发出空凭据被拒
            if (userName != null && !userName.trim().isEmpty()) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(userName, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder;
        });
        restHighLevelClient = new RestHighLevelClient(builder);
        return restHighLevelClient;
    }
    @PreDestroy
    public void clientClose() {
        try {
            this.restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
