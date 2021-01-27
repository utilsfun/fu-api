package fun.utils.api.core.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.common.MyJdbcTemplate;
import fun.utils.api.core.services.ApiService;
import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnClass(ApiProperties.class)
public class ApiAutoConfiguration {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean("fu-api.datasource")
    @ConfigurationProperties(prefix = "fu-api.datasource")
    public DataSource mainDataSource() {
        log.info("Initialize mainDataSource");
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    @Resource(name = "fu-api.datasource")
    DataSource dataSource;

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    JdbcTemplate mainJdbcTemplate() {
        log.info("Initialize mainJdbcTemplate");
        return new MyJdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    RestTemplate mainRestTemplate() {
        log.info("Initialize mainJdbcTemplate");
        return new RestTemplate();
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

    @Bean("fu-api.datasource-cache")
    @ConfigurationProperties(prefix = "fu-api.datasource-cache")
    public Cache<String, DataSource> getDataSourceCache() {
        log.info("Initialize datasourceCache");
        Cache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(60, TimeUnit.SECONDS).build();
        return dataSourceCache;
    }

}

