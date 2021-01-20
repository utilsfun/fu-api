package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


@Slf4j
public class ApiService implements DisposableBean {

    @Getter
    private final ApiProperties properties;

    @Getter
    private final WebApplicationContext webApplicationContext;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public ApiService(WebApplicationContext wac,ApiProperties properties , RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        this.webApplicationContext = wac;
        this.properties = properties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        for (ApiProperties.Application app : properties.getApplications()) {

            String executorClass = StringUtils.defaultIfBlank(app.getExecutor(),ApiDefaultExecutor.class.getName());
            ApiExecutor executor = (ApiExecutor) Class.forName(executorClass).newInstance();
            ApiController controller = new ApiController(app,executor);
            wac.getAutowireCapableBeanFactory().autowireBean(executor);

            String path = app.getPath();
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(path + "/**").build();
            Method methodRequest = ApiController.class.getDeclaredMethod("request", HttpServletRequest.class, HttpServletResponse.class);
            requestMappingHandlerMapping.registerMapping(requestMappingInfo,controller,methodRequest);

            log.info("register Application Controller {} : {}" , path + "/**" , JSON.toJSONString(app,true));
            app.setRequestMappingInfo(requestMappingInfo);

        }

    }


    @Override
    public void destroy()  {

        for (ApiProperties.Application app : properties.getApplications()) {
            try {
                requestMappingHandlerMapping.unregisterMapping(app.getRequestMappingInfo());
                log.info("stop the api controller");
            } catch (Throwable e) {
                log.warn("something goes wrong when stopping api controller:", e);
            }
        }

    }

}