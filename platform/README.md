# platform
- grafana stack

- start grafana stack with `docker compose up`
```shell
docker compose up
```

## Read Kafka Messages

### messages
```sh
docker compose exec -it kafka bash
kafka-console-consumer --bootstrap-server localhost:29092 --topic test --from-beginning --max-messages 10

#Read all message with header and timestamp
kafka-console-consumer --bootstrap-server localhost:29092 --topic test --from-beginning --property print.headers=true --property print.timestamp=true 
```