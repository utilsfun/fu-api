package fun.utils.api.core.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.common.cache.Cache;
import fun.utils.api.core.common.MyRestTemplate;
import fun.utils.api.core.persistence.SourceDO;
import fun.utils.api.core.script.GroovyService;
import fun.utils.api.core.services.BeanService;
import fun.utils.api.core.services.DoService;
import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import javax.sql.DataSource;
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

    @Getter
    @Autowired
    private BeanService beans;

    public AppBean() {
        restTemplate = new MyRestTemplate();
        groovyService = new GroovyService();
    }

    public DataSource getDataSource(SourceDO sourceDO) throws ExecutionException {
        String key = sourceDO.getName() + "/" + sourceDO.getGmtModified().getTime();
        return dataSourceCache.get(key,()-> DruidDataSourceFactory.createDataSource((Map)new Yaml().load(sourceDO.getConfig()) ));
    }

    public RedissonClient getRedissonClient(SourceDO sourceDO) throws ExecutionException {
        String key = sourceDO.getName() + "/" + sourceDO.getGmtModified().getTime();
        return redissonClientCache.get(key,()-> Redisson.create(Config.fromYAML(sourceDO.getConfig())));
    }

}
