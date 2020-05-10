package com.example.demo.ServiceImpl;

import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.utils.LocalDateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void add(Comment comment) {
        String dateTime = LocalDateTimeUtil.format(LocalDateTime.now());
        comment.setDateTime(dateTime);
        String comment1 = "comment";
        Comment save = mongoTemplate.save(comment, comment1);
        System.out.println("----");
        System.out.println(save);
    }

    @Override
    public void find() throws JsonProcessingException {
        List<Comment> comment = mongoTemplate.findAll(Comment.class, "comment");
        String s = comment.get(0).getReply().get(0);
        Comment c1 = new ObjectMapper().readValue(s, Comment.class);
        System.out.println(c1.getContent());
//        System.out.println(comment);
    }
}
