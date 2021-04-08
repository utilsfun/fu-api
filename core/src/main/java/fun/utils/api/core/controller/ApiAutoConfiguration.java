package fun.utils.api.core.controller;

import apijson.JSON;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.common.MyJdbcTemplate;
import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiAutoConfiguration implements DisposableBean, InitializingBean {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Bean("fu-api.rest-template")
    RestTemplate getRestTemplate() {
        log.info("Initialize getRestTemplate");
        return new RestTemplate();
    }

    @Bean("fu-api.redisson-client")
    public RedissonClient getRedissonClient() throws IOException {
        log.info("Initialize getRedissonClient");
        Map<String,Object> configMap = (Map<String, Object>) apiProperties.getRedis().get("redisson");
        log.info(JSON.toJSONString(configMap));
        String yml = new ObjectMapper(new YAMLFactory()).writeValueAsString(configMap);
        Config config = Config.fromYAML(yml);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    @Bean("fu-api.redis-template")
    public RedisTemplate getRedisTemplate() throws IOException {
        log.info("Initialize getRedisTemplate");
        RedissonClient redissonClient = webApplicationContext.getBean("fu-api.redisson-client",RedissonClient.class);
        RedisConnectionFactory connectionFactory = new RedissonConnectionFactory(redissonClient);
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean("fu-api.data-source")
    public DataSource getDataSource() throws Exception {
        log.info("Initialize getDataSource");
        Map<String,Object> configMap = (Map<String, Object>)apiProperties.getDatasource().get("druid");
        log.info(JSON.toJSONString(configMap));
        return DruidDataSourceFactory.createDataSource(configMap);
    }

    @Bean("fu-api.jdbc-template")
    JdbcTemplate getJdbcTemplate() {
        log.info("Initialize getJdbcTemplate");
        DataSource dataSource = webApplicationContext.getBean("fu-api.data-source",DataSource.class);
        return new MyJdbcTemplate(dataSource);
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


    @Bean
    @ConditionalOnMissingBean(DoService.class)
    DoService doService(){
        log.info("Initialize DoService");
        return new DoService();
    }



    private final List<RequestMappingInfo> requestMappingInfoList = new ArrayList<>();


    //最后一个bean,用于执行config加载后的主过程
    @Bean
    public void main() throws NoSuchMethodException {

        log.info("Initialize main program");

        for (ApiProperties.Application appConfig : apiProperties.getApplications()) {

            AppBean appBean = new AppBean();
            webApplicationContext.getAutowireCapableBeanFactory().autowireBean(appBean);

            log.info("register application {} " , JSON.toJSONString(appConfig));

            ApiController apiController = new ApiController(appConfig, appBean);
            registerController(apiController,appConfig.getApiPath());

            DocumentController docController = new DocumentController(appConfig, appBean);
            registerController(docController,appConfig.getDocPath());

            DesignController designController = new DesignController(appConfig, appBean);
            registerController(designController,appConfig.getDesignPath());

            PublicController pubController = new PublicController(appConfig, appBean);
            registerController(pubController,appConfig.getPubPath());

//            ImageController imageController = new ImageController();
//            registerController(imageController,"/image");

        }
    }

    private void registerController(Object controller,String path) throws NoSuchMethodException {

        webApplicationContext.getAutowireCapableBeanFactory().autowireBean(controller);
        String pubPath = path + "/**";

        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(pubPath).build();
        Method method = controller.getClass().getDeclaredMethod("request", HttpServletRequest.class, HttpServletResponse.class);
        requestMappingHandlerMapping.registerMapping(requestMappingInfo,controller,method);

        requestMappingInfoList.add(requestMappingInfo);

        log.info("register {} Controller path:{}" ,controller.getClass().getSimpleName(), pubPath);

    }

    @Override
    public void destroy()  {

        log.info("Destroy ApiAutoConfiguration");

        for (RequestMappingInfo requestMappingInfo : requestMappingInfoList) {
            try {
                requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                log.info(MessageFormat.format("stop controller : {0} ",requestMappingInfo.toString())) ;
            } catch (Throwable e) {
                log.warn("something goes wrong when stopping controller:", e);
            }
        }

    }


    //执行顺序 Constructor > @Autowired > @postConstruct > afterPropertiesSet > @Bean (1,2,3,4 有顺序)

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initialize afterPropertiesSet");
    }

    @PostConstruct
    public void postConstruct(){
        log.info("Initialize postConstruct");
    }

    public ApiAutoConfiguration() {
        log.info("Initialize Constructor");
    }
}

