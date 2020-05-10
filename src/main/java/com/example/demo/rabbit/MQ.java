package com.example.demo.rabbit;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class MQ {
    /**
     * 简单队列
     * autoDelete = "true"  没有生产者和消费者连接时自动删除
     *
     * @param msg
     */
/*    @RabbitListener(queuesToDeclare = @Queue(value = "simpleQueue", durable = "true"))
    public void simpleQueue(String msg) {
//        int i=2/0;
        System.out.println("接收  " + msg);
    }*/


    /**
     * 工作队列，多个消费者消费一个队列
     * <p>
     * AMQP默认实现消费者确认模式，原文如下
     * It's a common mistake to miss the basicAck and Spring AMQP helps to avoid this through its default configuration.
     * The consequences are serious. Messages will be redelivered when your client quits (which may look like random redelivery),
     * but RabbitMQ will eat more and more memory as it won't be able to release any unacked messages.
     * <p>
     * Fair dispatch vs Round-robin dispatching
     * 官网说： AMQP默认实现消费者fair转发，也就是能者多劳，原文如下（应该是说反了，默认的是250，但是是Round-robin dispatching）
     * However, "Fair dispatch" is the default configuration for Spring AMQP.
     * The AbstractMessageListenerContainer defines the value for DEFAULT_PREFETCH_COUNT to be 250.
     * If the DEFAULT_PREFETCH_COUNT were set to 1 the behavior would be the round robin delivery as described above.
     */
    //设置消费者的确认机制，并达到能者多劳的效果
    @Bean("workListenerFactory")
    public RabbitListenerContainerFactory myFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory =
                new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        //自动ack,没有异常的情况下自动发送ack
        //auto  自动确认,默认是auto
        //MANUAL  手动确认
        //none  不确认，发完自动丢弃
        containerFactory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        //拒绝策略,true回到队列 false丢弃，默认是true
        containerFactory.setDefaultRequeueRejected(true);

        //默认的PrefetchCount是250，采用Round-robin dispatching，效率低
        //setPrefetchCount 为 1，即可启用fair 转发
        containerFactory.setPrefetchCount(1);
        return containerFactory;
    }

    /**
     * 若不使用自定义containerFactory = "workListenerFactory"，默认的轮询消费效率低
     *
     * @param s
     */
    @RabbitListener(queuesToDeclare = @Queue("workQueue"), containerFactory = "workListenerFactory")
    public void workQueue1(String s) {
        System.out.println("workQueue 1  " + s);
    }

    @RabbitListener(queuesToDeclare = @Queue("workQueue"), containerFactory = "workListenerFactory")
    public void workQueue2(String s) throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("workQueue 2  " + s);
    }


    /**
     * 订阅模式  fanout
     */
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue,  //临时路由
                    exchange = @Exchange(value = "exchange1", type = ExchangeTypes.FANOUT))
    })
    public void fanout(String s) {
        System.out.println("订阅模式1    " + s);
    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue, exchange = @Exchange(value = "exchange1", type = ExchangeTypes.FANOUT))
    })
    public void fanout2(String s) {
        System.out.println("订阅模式2    " + s);
    }

    /**
     * 路由模式  DIRECT
     */
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue,  //临时路由
                    exchange = @Exchange(value = "exchange2", type = ExchangeTypes.DIRECT),
                    key = {"error", "info"}  //路由键
            )
    })
    public void router(String s) {
        System.out.println("路由模式1    " + s);
    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue,
                    exchange = @Exchange(value = "exchange2", type = ExchangeTypes.DIRECT),
                    key = {"error"}  //路由键
            )
    })
    public void router2(String s) {
        System.out.println("路由模式2    " + s);
    }

    /**
     * topic  topics
     */
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue,  //临时路由
                    exchange = @Exchange(value = "exchange3", type = ExchangeTypes.TOPIC),
                    key = {"user"}  //路由键
            )
    })
    public void topic(String s) {
        System.out.println("topic1......    " + s);
    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue,
                    exchange = @Exchange(value = "exchange3", type = ExchangeTypes.TOPIC),
                    key = {"user.#"}  //路由键
            )
    })
    public void topic2(String s) {
        System.out.println("topic2    " + s);
    }

}
