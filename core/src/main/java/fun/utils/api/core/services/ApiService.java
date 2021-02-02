package fun.utils.api.core.services;

import com.alibaba.fastjson.JSON;
import fun.utils.api.core.controller.ApiController;
import fun.utils.api.core.controller.ApiProperties;
import fun.utils.api.core.controller.AppBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ApiService implements DisposableBean {

    @Getter
    private final ApiProperties properties;

    @Getter
    private final WebApplicationContext webApplicationContext;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final List<RequestMappingInfo> requestMappingInfoList = new ArrayList<>();

    public ApiService(WebApplicationContext webApplicationContext,ApiProperties properties,RequestMappingHandlerMapping requestMappingHandlerMapping) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        this.webApplicationContext = webApplicationContext;
        this.properties = properties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        for (ApiProperties.Application app : properties.getApplications()) {

            AppBean appBean = new AppBean();
            webApplicationContext.getAutowireCapableBeanFactory().autowireBean(appBean);

            ApiController controller = new ApiController(app, appBean);
            webApplicationContext.getAutowireCapableBeanFactory().autowireBean(controller);

            String apiPath = app.getPath() + "/**";
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(apiPath).build();
            Method methodRequest = ApiController.class.getDeclaredMethod("apiRequest", HttpServletRequest.class, HttpServletResponse.class);
            requestMappingHandlerMapping.registerMapping(requestMappingInfo,controller,methodRequest);
            requestMappingInfoList.add(requestMappingInfo);

            String docPath = app.getDocPath() + "/**";
            RequestMappingInfo requestDocMappingInfo = RequestMappingInfo.paths(docPath).build();
            Method methodDocRequest = ApiController.class.getDeclaredMethod("docRequest", HttpServletRequest.class, HttpServletResponse.class);
            requestMappingHandlerMapping.registerMapping(requestDocMappingInfo,controller,methodDocRequest);
            requestMappingInfoList.add(requestDocMappingInfo);


            log.info("register Application Controller api:{} doc:{} : {}" , apiPath ,docPath, JSON.toJSONString(app,true));


        }

    }


    @Override
    public void destroy()  {

        for (RequestMappingInfo requestMappingInfo : requestMappingInfoList) {
            try {
                requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                log.info("stop the api controller");
            } catch (Throwable e) {
                log.warn("something goes wrong when stopping api controller:", e);
            }
        }

    }

}