# Kafka Setup aur Usage Guide — Journal Backend

Yeh file explain karta hai ki Kafka is project mein kaise use ho raha hai aur production AWS par kaise setup karte hain.

---

## Kafka Kya Hai? (Short intro)

Kafka ek **message broker** hai — jisko events/messages ko publish aur consume karne ke liye use karte hain. Yeh asynchronous communication enable karta hai aur system ko decouple karta hai.

Example:
- **Producer**: Jab user register karta hai, ek "user_register" event Kafka topic mein publish hota hai.
- **Consumer**: Background service us event ko consume karke welcome email bhej sakta hai ya notification create kar sakta hai.

---

## Current Setup — is Project mein Kafka Kaise Use Ho Raha Hai?

### 1) Kafka Topics

**Topic: `user_register`**
- **Kab trigger hota hai**: Jab user register karta hai (AuthController → RegisterProducer)
- **Kya data flow hota hai**: UserEvent (username, email, etc.)
- **Consumers**: 
  - `WelcomeConsumer` — "Thank you" message console print karta hai
  - `EmailConsumer` — Welcome email bhej sakta hai (implement karna pending)
  - `AdminNotificationConsumer` — Admin ko notify kar sakta hai

### 2) Producer: RegisterProducer.java

```
File: src/main/java/com/love/Backend/kafka/producer/RegisterProducer.java

- Kya karta hai: Jab new user register hota hai, event send karta hai Kafka "user_register" topic par
- Method: sendUserEvent(UserEvent event)
- Retry logic: Agar Kafka down ho to event database mein save karta hai (idempotency)
```

### 3) Consumers

```
a) WelcomeConsumer.java
   - Console mein "Thank you" message print karta hai
   - Group ID: welcome.msg

b) EmailConsumer.java
   - Email bhejne ke liye ready hai (currently incomplete)
   - Group ID: email.group

c) AdminNotificationConsumer.java
   - Admin ko notification dene ke liye
   - Group ID: admin.group
```

### 4) Current Configuration (application-dev.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092        # Kafka broker address
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer      # Events JSON ke roop mein send hote hain
    consumer:
      group-id: my-group                    # Consumer group ID
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
```

**Environment Variables** (jo set karne padh sakta hai):
- `KAFKA_BOOTSTRAP_SERVERS` — default: `localhost:9092`
- `KAFKA_CONSUMER_GROUP` — default: `my-group`

---

## Local Development — Kafka Chalana

### Option 1: Docker Compose Use Karo (Recommended)

```bash
# Kafka + Zookeeper + MongoDB + Redis sab chalao
cd Backend
docker-compose up -d

# Check karo ki Kafka chalega hai
docker logs <kafka-container-name>

# Stop karne ke liye
docker-compose down
```

### Option 2: Manual Install (Advanced)

```bash
# Download Kafka
wget https://archive.apache.org/dist/kafka/3.5.0/kafka_2.13-3.5.0.tgz
tar -xzf kafka_2.13-3.5.0.tgz
cd kafka_2.13-3.5.0

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (new terminal)
bin/kafka-server-start.sh config/server.properties

# Verify
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

## Kafka Testing — Locally

### 1) Topic Create Karo (optional — auto-create enabled hai)

```bash
docker exec kafka kafka-topics --create --topic user_register --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 2) Consumer Monitor Karo (real-time messages dekho)

```bash
docker exec kafka kafka-console-consumer --topic user_register --from-beginning --bootstrap-server localhost:9092
```

### 3) API Se Event Send Karo

```bash
# Register API call karo
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'

# Aap consumer terminal mein message dekh paoge
# "Thank you! testuser for registering our service!"
```

---

## AWS Production Setup — Kafka Kaise Chalega?

### Option 1: AWS MSK (Managed Streaming for Kafka) — Recommended

**Advantage**: Fully managed, auto-scaling, no infrastructure management.

#### Setup Steps:

1) AWS Console → MSK (Managed Streaming for Kafka) → Create cluster
2) Broker configurations:
   - Broker type: kafka.m5.large
   - Number of brokers: 3
   - Storage: 100 GB per broker
   - Public accessibility: Disable (EC2 same VPC mein ho)
3) Create cluster (~ 20 minutes lag jayega)
4) Security group: Port 9092 (Kafka) aur 2181 (Zookeeper) allow karo

**Environment Variables Set Karo (ECS Task/EC2/Lambda):**
```env
KAFKA_BOOTSTRAP_SERVERS=b-1.mykafka.xxx.kafka.us-east-1.amazonaws.com:9092,b-2.mykafka.xxx.kafka.us-east-1.amazonaws.com:9092,b-3.mykafka.xxx.kafka.us-east-1.amazonaws.com:9092
KAFKA_CONSUMER_GROUP=my-group
```

**Cost**: ~$300-400/month (production-grade)

---

### Option 2: Self-Managed Kafka on EC2 (Low-Cost)

**Advantage**: Full control, cheaper ($10-50/month).

#### Setup (AWS EC2 t2.micro — Free Tier eligible):

1) EC2 instance launch karo (Ubuntu 22.04, t2.micro, Free Tier)
2) SSH karo instance mein
3) Kafka install karo:

```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Java install
sudo apt-get install -y openjdk-17-jdk

# Kafka download
cd /opt
sudo wget https://archive.apache.org/dist/kafka/3.5.0/kafka_2.13-3.5.0.tgz
sudo tar -xzf kafka_2.13-3.5.0.tgz
sudo mv kafka_2.13-3.5.0 kafka
sudo chown -R ubuntu:ubuntu /opt/kafka

# Zookeeper start (background)
cd /opt/kafka
nohup bin/zookeeper-server-start.sh config/zookeeper.properties > /tmp/zk.log 2>&1 &

# Kafka start (background)
nohup bin/kafka-server-start.sh config/server.properties > /tmp/kafka.log 2>&1 &

# Verify
sleep 5
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

4) Security Group mein ports allow karo:
   - 9092 (Kafka)
   - 2181 (Zookeeper)
   - Inbound: App ka security group ya specific IP

5) Kafka config edit karo (`/opt/kafka/config/server.properties`):

```properties
# Add ya modify:
listeners=PLAINTEXT://0.0.0.0:9092
advertised.listeners=PLAINTEXT://<EC2_PUBLIC_IP_OR_PRIVATE_IP>:9092
```

6) Restart Kafka

**Environment Variables** (App ke liye):
```env
KAFKA_BOOTSTRAP_SERVERS=<EC2_PRIVATE_IP>:9092
KAFKA_CONSUMER_GROUP=my-group
```

---

### Option 3: Docker on EC2 (Medium Complexity)

1) EC2 instance launch karo
2) Docker + Docker Compose install karo
3) docker-compose.yml wahan push karo
4) `docker-compose up -d` run karo
5) Same networking setup

---

## Agar Kafka Down Ho to Kya Hota Hai?

**Current Implementation mein**:
- Producer event try karta hai send karna
- Agar fail ho (Kafka unavailable) → event database mein save hota hai (`ProcessedEventRepo`)
- Background process (scheduled job) later retry karta hai

```java
// RegisterProducer.java mein logic:
kafkaTemplate.send("user_register", event.getUserName(), event)
    .whenComplete((result, ex) -> {
        if(ex == null) {
            System.out.println("Event sent successfully");
        } else {
            System.out.println("Error event sending");
            kafkaProducerService.saveEventToDB(event); // Fallback: database mein save
        }
    });
```

**Recommendation**: Production mein DLQ (Dead Letter Queue) setup karo:
```yaml
spring:
  kafka:
    listener:
      ack-mode: manual
      poll-timeout: 3000
    retry:
      topic:
        enabled: true
        max:
          attempts: 3
```

---

## Monitoring Kafka on AWS

### CloudWatch Integration:
- MSK automatically logs bhejta hai CloudWatch mein
- Console → CloudWatch → Logs → `/aws/msk/...`

### Custom Monitoring:
```bash
# Consumer lag dekho
bin/kafka-consumer-groups.sh --bootstrap-server <BROKER_IP>:9092 --group my-group --describe

# Topics list
bin/kafka-topics.sh --list --bootstrap-server <BROKER_IP>:9092

# Topic describe
bin/kafka-topics.sh --describe --topic user_register --bootstrap-server <BROKER_IP>:9092
```

---

## Kafka Health Check — App mein

Add Kafka health endpoint (optional):

```java
@Component
public class KafkaHealthCheck {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    public boolean isKafkaHealthy() {
        try {
            ListenableFuture future = kafkaTemplate.send("health-check", "ping");
            future.get(5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## Summary Table

| Setup | Cost | Effort | Best For |
|-------|------|--------|----------|
| **Docker Compose (Local)** | Free | Low | Development |
| **AWS MSK** | $300-400/mo | Low | Production (managed) |
| **EC2 Self-Managed** | $10-50/mo | Medium | Low-cost production |
| **Docker on EC2** | $10-50/mo | Medium | Flexible production |

---

## Kaunsa Choose Karo Resume Project Ke Liye?

**Recommendation**: **AWS MSK (Free Tier eligible for first 2 months)**
- Production-grade
- Auto-scaling
- Fully managed (no ops headache)
- Resume mein likha sakta ho: "Managed Streaming for Kafka on AWS"

Ya phir **EC2 Self-Managed** (completely free):
- "Self-hosted Kafka on AWS EC2 with Zookeeper"
- Showcases DevOps knowledge

---

## Next Steps

1) Decide: MSK ya EC2?
2) AWS setup karo
3) `KAFKA_BOOTSTRAP_SERVERS` environment variable set karo
4) Test: registration API call karo aur message consume karo
5) Production app deploy karo with CI/CD

---

Last updated: May 16, 2026

