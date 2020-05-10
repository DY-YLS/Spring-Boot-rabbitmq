package com.example.demo.ServiceImpl;

import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Test
    void add() throws JsonProcessingException {
        Comment comment = new Comment();
        comment.setUserId("1");
        comment.setContent("hhhhhhhhhhhhh");
        comment.setDestinationId("3");

        Comment comment1 = new Comment();
        comment1.setUserId("4");

        List list = new ArrayList<>();
        list.add(comment1);
        list.add(comment1);

        /*String s = new ObjectMapper().writeValueAsString(comment);
        System.out.println(s);
        list.add(s);
        list.add(s);*/
        comment.setReply(list);
        commentService.add(comment);
    }

    @Test
    void find() throws JsonProcessingException {
        commentService.find();
    }
}