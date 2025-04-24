import logging
from kafka import KafkaProducer, KafkaConsumer
import json
import time
import random
import threading

# 配置日志记录
logging.basicConfig(
    filename='kafka_script.log',
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

# Kafka 服务器地址，根据实际情况修改
bootstrap_servers = ['<YOUR-TEST-DATA-SERVER-PRIVATE-IP>:9092']
# 要发送消息的 Kafka 主题，根据实际情况修改
producer_topic = 'risk-events'
# 要监听的 Kafka 主题，根据实际情况修改
consumer_topic = 'risk-decisions'

# 创建 Kafka 生产者实例
# value_serializer 用于将 Python 对象序列化为 JSON 字节流
producer = KafkaProducer(
    bootstrap_servers=bootstrap_servers,
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# 创建 Kafka 消费者实例
consumer = KafkaConsumer(
    consumer_topic,
    bootstrap_servers=bootstrap_servers,
    auto_offset_reset='latest',
    value_deserializer=lambda m: json.loads(m.decode('utf-8'))
)


def produce_messages():
    try:
        # 用户ID列表
        user_array = ["高危用户001", "用户002", "用户003"]
        # amount列表（用于eventType=TRANSFER的事件）
        amount_array = [400, 1000, 120000]
        #while True:
        for _ in range(10):  # 发送 10 条消息示例
            # 构造包含毫秒级时间戳的 JSON 格式的消息内容
            message = {
                "userId": random.choice(user_array),
                "eventType": "TRANSFER",
                "amount": random.choice(amount_array),
                "eventTime": int(time.time()*1000)
            }

            # 发送消息到指定主题
            future = producer.send(producer_topic, message)
            # 阻塞等待，确保消息发送成功
            record_metadata = future.get(timeout=10)
            log_message = f"Message sent to topic: {record_metadata.topic}, partition: {record_metadata.partition}, offset: {record_metadata.offset}"
            logging.info(log_message)
            # 每条消息发送间隔(秒)
            time.sleep(2)

    except Exception as e:
        logging.error(f"An error occurred while sending messages: {e}")
    finally:
        # 关闭生产者连接
        producer.close()


def consume_messages():
    try:
        for message in consumer:
            log_message = f"Received message from topic {message.topic}: {message.value}"
            logging.info(log_message)
    except Exception as e:
        logging.error(f"An error occurred while consuming messages: {e}")
    finally:
        # 关闭消费者连接
        consumer.close()


if __name__ == "__main__":
    # 创建生产者线程
    producer_thread = threading.Thread(target=produce_messages)
    # 创建消费者线程
    consumer_thread = threading.Thread(target=consume_messages)

    # 启动生产者线程
    producer_thread.start()
    # 启动消费者线程
    consumer_thread.start()

    # 等待生产者线程结束
    producer_thread.join()
    # 消费者线程是一个无限循环，手动终止脚本时会结束    