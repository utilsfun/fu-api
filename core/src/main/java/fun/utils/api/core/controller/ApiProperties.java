package fun.utils.api.core.controller;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "fu-api")
public class ApiProperties {

    private List<Application> applications = new ArrayList<>();

    private Map<String, Object> datasource;
    private Map<String, Object> redis;

    @Data
    public static class Application {

        private String name;
        private String apiPath;
        private String docPath;
        private String editPath;
        private String designPath;

        public String getApiPath() {
            return StringUtils.defaultIfBlank(apiPath, getName() + "/api" );
        }

        public String getDocPath() {
            return StringUtils.defaultIfBlank(docPath,  getName() + "/document" );
        }

        public String getDesignPath() {
            return StringUtils.defaultIfBlank(editPath,  getName() + "/design" );
        }

        public String getPubPath() {
            return StringUtils.defaultIfBlank(designPath,  getName() + "/public" );
        }
    }

}