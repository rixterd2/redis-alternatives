redis-container:
	podman pull redis:8.0
	podman run -d -p 6379:6379 --name redis-redisson redis:8.0
valkey-container:
	podman pull valkey/valkey:8.0
	podman run -d -p 6379:6379 --name valkey-redisson valkey/valkey:8.0