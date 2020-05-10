package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * AMQP 默认消息是持久化的，但只有在队列也是持久化时才有作用，原文如下：
     * Messages are persistent by default with Spring AMQP.
     * Note the queue the message will end up in needs to be durable as well,
     * otherwise the message will not survive a broker restart as a non-durable queue does not itself survive a restart.
     * <p>
     * MessageProperties类中源码如下：
     * static {
     * DEFAULT_DELIVERY_MODE = MessageDeliveryMode.PERSISTENT;
     * DEFAULT_PRIORITY = 0;
     * }
     * <p>
     * 如何设置消息不持久化？
     * 设置消息不持久化，默认是持久化的，这里只为记录如何设置消息不持久化，一般不设置
     * 发送消息时，添加 MessagePostProcessor即可，这里使用 lambda 表达式
     * (message) -> {
     * message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
     * return message;
     * }
     * <p>
     * 完整示例如下：
     * rabbitTemplate.convertAndSend("simpleQueue", "this is simpleQueue",
     * (message) -> {
     * message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
     * return message;
     * });
     */
    @Test
    public void simpleQueue() {
        rabbitTemplate.convertAndSend("simpleQueue", "this is simpleQueue",
                (message) -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
                    return message;
                });
        System.out.println("simple success");
    }

    @Test
    public void workQueue() {
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("workQueue", i);
        }
        System.out.println("workQueue success");
    }

    @Test
    public void fanOut() {
        rabbitTemplate.convertAndSend("exchange1", "", "fan out......");
    }

    @Test
    public void router() {
        rabbitTemplate.convertAndSend("exchange2", "info", "router");
        System.out.println("router");
    }

    @Test
    public void topic() {
        rabbitTemplate.convertAndSend("exchange3", "user.name", "hhh");
        System.out.println("topic");
    }
}
