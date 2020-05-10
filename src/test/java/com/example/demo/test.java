package com.example.demo;

import com.example.demo.controller.MongoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
