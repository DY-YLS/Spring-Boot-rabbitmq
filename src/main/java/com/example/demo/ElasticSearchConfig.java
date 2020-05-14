package com.example.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.yml")//配置文件地址，可以自定义
@ConfigurationProperties("elasticsearch")//属性前缀
public class ElasticSearchConfig {
    //    @Value("${elasticsearch.hostList}")
    private String hostList;//配置文件中的属性

    public String getHostList() {
        return hostList;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    @Bean(value = "RestHighLevelClient", destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        //通过逗号分割节点
        String[] split = hostList.split(",");
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            //通过冒号分离出每一个节点的ip,port
            String[] split1 = split[i].split(":");
            //这里http写固定了，只为测试使用，可以通过读取配置文件赋值的方式优化
            httpHosts[i] = new HttpHost(split1[0], Integer.parseInt(split1[1]), "http");
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}
