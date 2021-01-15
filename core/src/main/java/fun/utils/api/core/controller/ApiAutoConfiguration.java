package fun.utils.api.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnClass(ApiProperties.class)
public class ApiAutoConfiguration {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean(ApiService.class)
    ApiService apiService() throws  NoSuchMethodException {
        log.info("Initialize ApiService");
        return new ApiService(apiProperties,requestMappingHandlerMapping);
    }

}

