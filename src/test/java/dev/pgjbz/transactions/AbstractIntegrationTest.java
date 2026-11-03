package dev.pgjbz.transactions;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractIntegrationTest {

	@Container 
	@ServiceConnection
	static PostgreSQLContainer postgresContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));
	

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:8.10-alpine")).withExposedPorts(6379);

	@Autowired
    private CacheManager cacheManager;

	@BeforeEach 
	void clearCaches() {
		cacheManager.getCacheNames()
			.forEach(cacheName -> cacheManager.getCache(cacheName).clear());
	}
}
