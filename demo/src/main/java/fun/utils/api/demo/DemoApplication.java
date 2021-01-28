package fun.utils.api.demo;


import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@ComponentScan(value="fun.utils.api.core.controller")
@Configuration
@Slf4j
public class DemoApplication {

    @Bean("my")
    Map<String,Object> my(){
        log.info("Initialize bean my");
        Map<String,Object> target = new HashMap<>();
        target.put("cc",new Long("888"));
        return target;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}

