package fun.utils.api.core.controller;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.cache.Cache;
import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.common.MyRestTemplate;
import fun.utils.api.core.persistence.SourceDO;
import fun.utils.api.core.script.GroovyService;
import fun.utils.api.core.services.DoService;
import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AppBean {

    @Autowired
    @Getter
    private DoService doService;

    @Getter
    private final RestTemplate restTemplate;

    @Resource(name = "fu-api.data-source-cache")
    @Getter
    private Cache<String, DataSource> dataSourceCache;

    @Resource(name = "fu-api.redisson-client-cache")
    @Getter
    private Cache<String, RedissonClient> redissonClientCache;

    @Autowired
    @Getter
    private WebApplicationContext webApplicationContext;

    @Getter
    private final GroovyService groovyService;

    public AppBean() {
        restTemplate = new MyRestTemplate();
        groovyService = new GroovyService();
    }

    public DataSource getDataSource(String applicationName,String databaseName) throws ExecutionException, ApiException {
        SourceDO sourceDO = doService.getDatabaseSourceDO(applicationName,databaseName);
        if (sourceDO == null){
            throw ApiException.resourceNotFondException("database:" + databaseName);
        }
        return getDataSource(sourceDO);
    }

    public RedissonClient getRedissonClient(String applicationName,String redisName) throws ExecutionException, ApiException, IOException {
        SourceDO sourceDO = doService.getRedisSourceDO(applicationName,redisName);
        if (sourceDO == null){
            throw ApiException.resourceNotFondException("redis:" + redisName);
        }
        return getRedissonClient(sourceDO);
    }

    public DataSource getDataSource(SourceDO sourceDO) throws ExecutionException {
        String key = sourceDO.getId() + "/" + sourceDO.getGmtModified().getTime();
        Map<String,Object> configMap = new Yaml().load(sourceDO.getConfig());
        return dataSourceCache.get(key,()-> DruidDataSourceFactory.createDataSource(configMap));
    }

    public RedissonClient getRedissonClient(SourceDO sourceDO) throws ExecutionException, IOException {
        String key = sourceDO.getId() + "/" + sourceDO.getGmtModified().getTime();
        Config config = Config.fromYAML(sourceDO.getConfig());
        return redissonClientCache.get(key,()-> Redisson.create(config));
    }

}
