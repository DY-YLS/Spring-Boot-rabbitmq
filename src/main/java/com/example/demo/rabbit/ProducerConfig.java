package com.example.demo.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ProducerConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void pre() {
        /**
         *
         *  消息发送到交换机的回调
         *
         *   public void confirm(CorrelationData correlationData, boolean b, String s) {
         *
         * 		System.out.println("消息唯一标识："+correlationData);
         * 		System.out.println("确认结果："+ b);
         * 		System.out.println("失败原因："+ s);
         *    }
         *
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("setConfirmCallback-------------------");
            System.out.println("correlationData:  " + correlationData);
            System.out.println(ack);
            System.out.println(cause);
            if (ack) {
                System.out.println("发送成功");
            } else {
                System.out.println("发送失败");
                // 可以记录下来，也可以重新发送消息。。。
            }

        });

        /**
         *
         * 消息从交换机发送到队列的回调，只有发送失败时才会回调
         *  public void returnedMessage(Message message, int i, String s, String s1, String s2) {
         * 		System.out.println("消息主体 message : "+message);
         * 		System.out.println("消息主体 message : "+ i);
         * 		System.out.println("描述："+ s);
         * 		System.out.println("消息使用的交换器 exchange : "+ s1);
         * 		System.out.println("消息使用的路由键 routing : "+ s2);
         *    }
         */
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            System.out.println("setReturnCallback---------------------");
            System.out.println("消息主体 message : " + message);
            System.out.println("响应码 replyCode: " + replyCode);
            System.out.println("响应内容 replyText：" + replyText);
            System.out.println("消息使用的交换器 exchange : " + exchange);
            System.out.println("消息使用的路由键 routeKey : " + routingKey);

            //也可以重新发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, new String(message.getBody()));
            System.out.println("重新发送消息： -----" + new String(message.getBody()));
        });

        /**
         * 网上都说必须设置rabbitTemplate.setMandatory(true),才能触发ReturnCallback回调，
         * 我尝试了一下，并不需要设置为true,交换机发送消息给队列失败时，也能触发回调
         */
        //rabbitTemplate.setMandatory(true);
    }
}
