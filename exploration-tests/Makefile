WORKSPACE_DIR="/Users/vbazhmin/IdeaProjects/ARCHITECTURE/redis-alternatives/exploration-tests"

redis-container:
	docker pull redis:8.0
	docker run -d -p 6379:6379 --name redis-benchmark -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis redis:8.0 redis-server /opt/redis/redis-in-memory.conf
redis-cluster:
	docker pull redis:8.0
	docker run -d -p 6479:6379 --name redis-cluster-1 -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis redis:8.0 redis-server /opt/redis/redis-cluster.conf
	docker run -d -p 6579:6379 --name redis-cluster-2 -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis redis:8.0 redis-server /opt/redis/redis-cluster.conf
	docker run -d -p 6679:6379 --name redis-cluster-3 -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis redis:8.0 redis-server /opt/redis/redis-cluster.conf
    # Wait until all containers are running
	while [ "$$(docker inspect -f '{{.State.Running}}' redis-cluster-1)" != "true" ] || \
          [ "$$(docker inspect -f '{{.State.Running}}' redis-cluster-2)" != "true" ] || \
          [ "$$(docker inspect -f '{{.State.Running}}' redis-cluster-3)" != "true" ]; do \
        echo "Waiting for containers to be running..."; \
        sleep 1; \
    done
    # Get IPs and create cluster
	REDIS_1_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-cluster-1); \
	REDIS_2_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-cluster-2); \
	REDIS_3_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-cluster-3); \
	dockepr exec -it redis-cluster-1 redis-cli --cluster create "$$REDIS_1_IP:6379" "$$REDIS_2_IP:6379" "$$REDIS_3_IP:6379" --cluster-replicas 0
valkey-container:
	docker pull valkey/valkey:8.0
	docker run -d -p 6379:6379 --name valkey-benchmark -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey valkey/valkey:8.0 valkey-server /opt/valkey/valkey-in-memory.conf
valkey-cluster:
	docker pull valkey/valkey:8.0
	docker run -d -p 6479:6379 --name valkey-cluster-1 -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey valkey/valkey:8.0 valkey-server /opt/valkey/valkey-cluster.conf
	docker run -d -p 6579:6379 --name valkey-cluster-2 -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey valkey/valkey:8.0 valkey-server /opt/valkey/valkey-cluster.conf
	docker run -d -p 6679:6379 --name valkey-cluster-3 -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey valkey/valkey:8.0 valkey-server /opt/valkey/valkey-cluster.conf
    # Wait until all containers are running
	while [ "$$(docker inspect -f '{{.State.Running}}' valkey-cluster-1)" != "true" ] || \
          [ "$$(docker inspect -f '{{.State.Running}}' valkey-cluster-2)" != "true" ] || \
          [ "$$(docker inspect -f '{{.State.Running}}' valkey-cluster-3)" != "true" ]; do \
        echo "Waiting for containers to be running..."; \
        sleep 1; \
    done
    # Get IPs and create cluster
	VALKEY_1_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' valkey-cluster-1); \
	VALKEY_2_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' valkey-cluster-2); \
	VALKEY_3_IP=$$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' valkey-cluster-3); \
	docker exec -it valkey-cluster-1 valkey-cli --cluster create "$$VALKEY_1_IP:6379" "$$VALKEY_2_IP:6379" "$$VALKEY_3_IP:6379" --cluster-replicas 0
dragonfly-container:
	docker pull docker.dragonflydb.io/dragonflydb/dragonfly:latest
	docker run -d -p 6379:6379 --name dragonfly-benchmark -v $(WORKSPACE_DIR)/docker/opt/dragonfly:/opt/dragonfly docker.dragonflydb.io/dragonflydb/dragonfly:latest dragonfly --flagfile=/opt/dragonfly/dragonfly.flagfile