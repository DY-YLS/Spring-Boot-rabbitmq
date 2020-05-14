package com.example.demo;

import com.example.demo.controller.MongoController;
import com.sun.org.apache.xpath.internal.operations.String;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.util.Collections;

@SpringBootTest
class test {
    @Autowired
    private MongoController mongoController;

    @Test
    void test3() {
        mongoController.test1();
    }

    @Test
    void test4() {
        System.out.println(4);
    }

}
