WORKSPACE_DIR="/Users/vbazhmin/IdeaProjects/ARCHITECTURE/redis-alternatives/exploration-tests"

redis-container:
	docker pull redis:8.0
	docker run -d -p 6379:6379 --name redis-benchmark -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis --memory=256m redis:8.0 redis-server /opt/redis/redis-in-memory.conf
valkey-container:
	docker pull valkey/valkey:8.0
	docker run -d -p 6379:6379 --name valkey-benchmark -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey --memory=256m valkey/valkey:8.0 valkey-server /opt/valkey/valkey-in-memory.conf
dragonfly-container:
	docker pull docker.dragonflydb.io/dragonflydb/dragonfly:latest
	docker run -d -p 6379:6379 --name dragonfly-benchmark -v $(WORKSPACE_DIR)/docker/opt/dragonfly:/opt/dragonfly docker.dragonflydb.io/dragonflydb/dragonfly:latest dragonfly --flagfile=/opt/dragonfly/dragonfly.flagfile