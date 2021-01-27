package fun.utils.api.core.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.cache.Cache;
import fun.utils.api.core.common.MyRestTemplate;
import fun.utils.api.core.persistence.SourceDO;
import fun.utils.api.core.script.GroovyService;
import fun.utils.api.core.services.DoService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;

public class AppBean {

    @Autowired
    @Getter
    private DoService doService;

    @Getter
    private final RestTemplate restTemplate;

    @Resource(name = "fu-api.datasource-cache")
    @Getter
    private Cache<String, DataSource> dataSourceCache;

    @Autowired
    @Getter
    private WebApplicationContext webApplicationContext;

    @Getter
    private final GroovyService groovyService;

    public AppBean() {
        restTemplate = new MyRestTemplate();
        groovyService = new GroovyService();
    }

    public DataSource getDataSource(SourceDO sourceDO) throws ExecutionException {
        String key = sourceDO.getName() + "/" + sourceDO.getGmtModified().getTime();
        return dataSourceCache.get(key,()-> DruidDataSourceFactory.createDataSource(sourceDO.getConfig()));
    }

}
