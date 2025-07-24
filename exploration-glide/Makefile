redis-container:
	# does not work with redis above 7.2
valkey-container:
	podman pull valkey/valkey:8.0
	podman run -d -p 6379:6379 --name valkey-glide valkey/valkey:8.0