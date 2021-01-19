package fun.utils.api.core.controller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "fu-api")
public class ApiProperties {

    private List<Application> applications = new ArrayList<>();

    @Data
    public static class Application {
        private String path;
        private String executor;
        private RequestMappingInfo requestMappingInfo;
    }

}