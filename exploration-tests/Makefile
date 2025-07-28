WORKSPACE_DIR="/Users/vbazhmin/IdeaProjects/ARCHITECTURE/redis-alternatives/exploration-tests"

redis-container:
	docker pull redis:8.0
	docker run -d -p 6379:6379 --name redis-benchmark -v $(WORKSPACE_DIR)/docker/opt/redis:/opt/redis --memory=256m redis:8.0 redis-server /opt/redis/redis.conf
valkey-container:
	docker pull valkey/valkey:8.0
	docker run -d -p 6379:6379 --name valkey-benchmark -v $(WORKSPACE_DIR)/docker/opt/valkey:/opt/valkey --memory=256m valkey/valkey:8.0 valkey-server /opt/valkey/valkey.conf