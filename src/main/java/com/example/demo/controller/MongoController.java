package com.example.demo.controller;

import com.example.demo.entity.Mongo;
import com.google.common.collect.Lists;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class MongoController {
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/mongo")
    public void test1() {

        System.out.println("this is mongoController test1()");
        Mongo mongo = new Mongo();
        ArrayList list = Lists.newArrayList("h", "hh", "hhh", new Date(), mongo);
        mongo.setId(1);
        mongo.setName("杨露生");
        mongo.setList(list);

//        mongoTemplate.remove(new Query(Criteria.where("id").is(1)),"c2");

        Query query = new Query(Criteria.where("name").is("杨露生"));
        Update update = new Update();
        update.set("list", list);
        UpdateResult c2 = mongoTemplate.upsert(query, update, Mongo.class, "c2");
        System.out.println(c2);


//        mongoTemplate.save(mongo,"c2");


        List<Object> objects = mongoTemplate.find(new Query(Criteria.where("name").regex("毛毛").and("_id").is("5e552d1b65b0f0123cc0b3b8")), Object.class, "c1");
        objects.forEach(o -> System.out.println(o));
    }
}
