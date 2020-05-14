package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsWebTestClientConfigurationCustomizer;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ElasticTest {

    @Autowired
    @Qualifier("RestHighLevelClient")
    private RestHighLevelClient client;

/*    static RestHighLevelClient client = null;
    @BeforeAll
    public static void before() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }*/

    @Test
    public void test1() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        IndexRequest indexRequest = new IndexRequest("posts")
                .id("1").source(jsonMap);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index);
    }

    /**
     * 添加数据
     * @throws IOException
     */
    @Test
    public void index() throws IOException {
        UserTest userTest = new UserTest();
        userTest.setName("董28");
        userTest.setSex("男");
        //由于客户端不支持自定义实体对象类作为添加数据的参数，所以提前先将对象转化为Map对象
        Map map = entityToMap(userTest);
        System.out.println(map);
        IndexRequest indexRequest = new IndexRequest("posts")
                .source(map);
        //异步
        client.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {

            @Override
            public void onResponse(IndexResponse indexResponse) {
                System.out.println(indexResponse);
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    //新建
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    //修改
                }
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                    //
                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                        String reason = failure.reason();
                    }
                }

            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("exception...");
            }
        });
        //同步
        /*IndexRequest indexRequest = new IndexRequest("posts")
                .id("3").source(map);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index);*/

    }

    /**
     * 通过索引index,id查询
     * @throws IOException
     */
    @Test
    public void get() throws IOException {
        GetRequest getRequest = new GetRequest("posts", "1");
        /*FetchSourceContext fetchSourceContext = new FetchSourceContext(true, new String[]{"sex"}, Strings.EMPTY_ARRAY);
        getRequest.fetchSourceContext(fetchSourceContext)*/
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            System.out.println(getResponse);
            System.out.println(getResponse.getId());
            System.out.println(getResponse.getSource());
            System.out.println(getResponse.getSourceAsMap());
            UserTest userTest1 = entityConvert(getResponse.getSourceAsString(), UserTest.class);
            System.out.println(userTest1);
            UserTest userTest2 = entityConvert(getResponse.getSource(), UserTest.class);
            System.out.println(userTest2);
        }
    }

    @Test
    public void update() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("posts", "8");
        updateRequest.doc("sex", "女");
        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update);
    }

    @Test
    public void delete() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("posts");
        DeleteResponse delete = client.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 通过字段值查询
     * @throws IOException
     */
    @Test
    public void search() throws IOException {
        SearchRequest searchRequest = new SearchRequest("posts");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //通过匹配某一字段查询
//        searchSourceBuilder.query(QueryBuilders.termQuery("name","董"));
        //选出指定结果字段
        searchSourceBuilder.fetchSource(new String[]{"id"},Strings.EMPTY_ARRAY);
        //对结果排序
//        searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.DESC));
        //聚集结果
        searchSourceBuilder.aggregation(
                AggregationBuilders
                        .max("maxValue")//命名
                        .field("id"));//指定聚集的字段
        //查询所有
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //从0开始
//        searchSourceBuilder.from(2).size(2);

        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(search);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UserTest {
        private int id;
        private String name;
        private String sex;
        private Others others = new Others("132@qq.com", "199");
    }

    @Data
    @AllArgsConstructor
    private static class Others {
        private String email;
        private String phone;
    }

    /**
     * 实体对象转化为map
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    static Map entityToMap(Object object) throws JsonProcessingException {
        Map map = entityConvert(object, HashMap.class);
        return map;
    }

    /**
     * 一个对象转化为另一个有相同属性的对象
     * @param object
     * @param clazz
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    static <T> T entityConvert(Object object, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String s;
        if (object instanceof String) {
            s = String.valueOf(object);
        } else {
            s = objectMapper.writeValueAsString(object);
        }
        T t = objectMapper.readValue(s, clazz);
        return t;
    }
}
