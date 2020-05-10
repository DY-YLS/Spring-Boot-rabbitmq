> **yls**   
>*2020/5/10*

# Spring Boot整合rabbitmq
> rabbitmq的基本概念和其它相关知识请自主去官网学习
>[rabbitmq官网](https://www.rabbitmq.com/#getstarted)，
>本文只介绍rabbitmq在springboot中如何使用

## 添加依赖包
```xml
        <!--rabbitmq客户端 start-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
            <version>2.2.7.RELEASE</version>
        </dependency>
        <!--rabbitmq客户端 end-->
```
## 添加配置文件 `application.yml`
```yaml
spring:
  rabbitmq:        #rabbit连接配置信息
    host: 39.97.234.52
    port: 5672
    username: admin
    password: admin
    virtual-host:  /vhost_1
```

## rabbitmq五种模式的使用
#### 1. 简单队列
1. 创建消费者
```java
@Component
public class MQ {
    /**
     * 简单队列
     * autoDelete = "true"  表示没有生产者和消费者连接时自动删除
     * durable = "true"  表示队列持久化，默认就是 true
     * @param msg
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "simpleQueue",autoDelete = "true", durable = "true"))
    public void simpleQueue(String msg) {
        System.out.println("接收  " + msg);
    }
}
```
2. 创建生产者
```java
@SpringBootTest
public class RabbitTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Test
    public void simpleQueue() {
        rabbitTemplate.convertAndSend("simpleQueue", "this is simpleQueue")
        System.out.println("simple success");
    }
}
```
#### 2. 工作队列,实现了能者多劳
1. 创建消费者
```java
@Component
public class MQ {
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
}
```

2. 创建生产者
```java
@SpringBootTest
public class RabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void workQueue() {
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("workQueue", i);
        }
        System.out.println("workQueue success");
    }
}
```
#### 3. 订阅模式
1. 创建消费者
```java
@Component
public class MQ {

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
}
```

2. 创建生产者
```java
@SpringBootTest
public class RabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void fanOut() {
        rabbitTemplate.convertAndSend("exchange1", "", "fan out......");
    }
}
```
#### 4. 路由模式
1. 创建消费者
```java
@Component
public class MQ {
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
}
```

2. 创建生产者
```java
@SpringBootTest
public class RabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void router() {
        rabbitTemplate.convertAndSend("exchange2", "info", "router");
        System.out.println("router");
    }
}
```

#### 5. 主题模式
1. 创建消费者
```java
@Component
public class MQ {
            /**
             * topic  topics
             */
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
```

2. 创建生产者
```java
@SpringBootTest
public class RabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void topic() {
        rabbitTemplate.convertAndSend("exchange3", "user.name", "hhh");
        System.out.println("topic");
    }
}
```
## 默认消息是持久化的，也可以设置不持久化，以简单队列示例
```java
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
}
```

## 如何设置生产者消息确认，避免消息发送失败而丢失
1. 在配置文件中添加
```yaml
spring:
  rabbitmq:        #rabbit连接配置信息
    publisher-returns: true             #开启消息从 交换机----》队列发送失败的回调
    publisher-confirm-type: correlated  #开启消息从 生产者----》交换机的回调
```
2. 添加配置类
```java
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
    }
}
```






