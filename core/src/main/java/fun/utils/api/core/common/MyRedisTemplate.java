package fun.utils.api.core.common;

import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class MyRedisTemplate extends RedisTemplate {
    public MyRedisTemplate(RedissonClient redissonClient) {
        this.setConnectionFactory(new RedissonConnectionFactory(redissonClient));
    }
}
