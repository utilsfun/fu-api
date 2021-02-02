package fun.utils.api.core.controller;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "fu-api")
public class ApiProperties {

    private List<Application> applications = new ArrayList<>();

    private Map<String,Object> datasource;
    private Map<String,Object> redis;

    @Data
    public static class Application {

        private String path;
        private String name;
        private String docPath;

        public String getPath(){
            return StringUtils.defaultIfBlank(path,name);
        }

        public String getName(){
            return StringUtils.defaultIfBlank(name,path);
        }

        public String getDocPath(){
            return StringUtils.defaultIfBlank(docPath,"doc/" + getName());
        }

    }

}