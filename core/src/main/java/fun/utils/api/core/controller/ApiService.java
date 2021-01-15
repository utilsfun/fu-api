package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


@Slf4j
@Data
public class ApiService implements  DisposableBean {

    private final ApiProperties callerProperties;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public ApiService(ApiProperties callerProperties , RequestMappingHandlerMapping requestMappingHandlerMapping) throws  NoSuchMethodException {
        this.callerProperties = callerProperties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        for (ApiProperties.Application app : callerProperties.getApplications()) {

            String path = app.getPath();
            ApiController controller = new ApiController(app);
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(path + "/**").build();
            Method methodRequest = ApiController.class.getDeclaredMethod("request", HttpServletRequest.class, HttpServletResponse.class);
            requestMappingHandlerMapping.registerMapping(requestMappingInfo,controller,methodRequest);
            log.info("register Application Controller {} : {}" , path + "/**" , JSON.toJSONString(app,true));
            app.setRequestMappingInfo(requestMappingInfo);

        }

    }


    @Override
    public void destroy()  {

        for (ApiProperties.Application app : callerProperties.getApplications()) {
            try {
                requestMappingHandlerMapping.unregisterMapping(app.getRequestMappingInfo());
                log.info("stop the api controller");
            } catch (Throwable e) {
                log.warn("something goes wrong when stopping api controller:", e);
            }
        }

    }

}