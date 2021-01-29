package fun.utils.api.core.controller;

import apijson.JSON;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.util.TypeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.common.MyJdbcTemplate;
import fun.utils.api.core.services.ApiService;
import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiAutoConfiguration implements InitializingBean {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    RestTemplate getRestTemplate() {
        log.info("Initialize getRestTemplate");
        return new RestTemplate();
    }

    @Bean("fu-api.redis")
    public RedisTemplate getRedisTemplate() throws IOException {
        log.info("Initialize getRedisTemplate");
        Map<String,Object> configMap = (Map<String, Object>) apiProperties.getRedis().get("redisson");
        log.info(JSON.toJSONString(configMap));
        String yml = new ObjectMapper(new YAMLFactory()).writeValueAsString(configMap);
        Config config = Config.fromYAML(yml);
        RedissonClient redissonClient = Redisson.create(config);
        RedisConnectionFactory connectionFactory = new RedissonConnectionFactory(redissonClient);
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }


    @Bean("fu-api.datasource")
    public DataSource getDataSource() throws Exception {
        log.info("Initialize getDataSource");
        Map<String,Object> configMap = (Map<String, Object>)apiProperties.getDatasource().get("druid");
        log.info(JSON.toJSONString(configMap));
        return DruidDataSourceFactory.createDataSource(configMap);
    }

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    JdbcTemplate getJdbcTemplate() {
        log.info("Initialize getJdbcTemplate");
        DataSource dataSource = webApplicationContext.getBean("fu-api.datasource",DataSource.class);
        return new MyJdbcTemplate(dataSource);
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean(ApiService.class)
    ApiService apiService() throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        log.info("Initialize ApiService");
        return new ApiService(webApplicationContext,apiProperties,requestMappingHandlerMapping);
    }

    @Bean
    @ConditionalOnMissingBean(DoService.class)
    DoService doService(){
        log.info("Initialize DoService");
        return new DoService();
    }

    @Bean("fu-api.data-source-cache")
    public Cache<String, DataSource> getDataSourceCache() {
        log.info("Initialize getDataSourceCache");
        Cache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(120, TimeUnit.SECONDS).build();
        return dataSourceCache;
    }

    @Bean("fu-api.redisson-client-cache")
    public Cache<String, RedissonClient> getRedissonClientCache() {
        log.info("Initialize getRedissonClientCache");
        Cache<String, RedissonClient> redissonClientCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(120, TimeUnit.SECONDS).build();
        return redissonClientCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}

