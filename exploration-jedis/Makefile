redis-container:
	podman pull redis:8.0
	podman run -d -p 6379:6379 --name redis-jedis redis:8.0
valkey-container:
	podman pull valkey/valkey:8.0
	podman run -d -p 6379:6379 --name valkey-jedis valkey/valkey:8.0