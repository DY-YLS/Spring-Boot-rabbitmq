package com.example.demo.entity;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Comment {

    //1.若该留言是对某一个主题下面的留言，该字段表示主题的id
    //2.若该留言是对另一个留言的回复，该字段表示对所回复用户的id
    private String destinationId;

    //留言所在的深度
    //0：对某主题的留言
    //1：对主题留言的回复
    //2：对回复的回复
    private int level = 0;

    //留言者的id
    private String userId;

    //留言的内容
    private String content;

    //留言的日期
    private String dateTime;

    //评论的回复内容,默认为空数组
    //String 为Comment对象的json格式
    private List<String> reply = Lists.newArrayList();
}
