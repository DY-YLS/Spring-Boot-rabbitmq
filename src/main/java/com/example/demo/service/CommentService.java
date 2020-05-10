package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CommentService {
    void add(Comment comment);

    void find() throws JsonProcessingException;
}
