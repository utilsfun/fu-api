package fun.utils.api.core.controller;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.api.core.common.MyJdbcTemplate;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.common.WebUtils;
import fun.utils.jsontemplate.beans.ApiJsonBean;
import fun.utils.jsontemplate.beans.JdbcTemplateBean;
import fun.utils.jsontemplate.core.GroovyConverter;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@Slf4j
public class DesignController extends BaseController {


    private final String classPath = "classpath:fu-api/design/";


    public DesignController(ApiProperties.Application appConfig, AppBean appBean) {
        super(appConfig, appBean);
    }


    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws Exception {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();
        String uri = StringUtils.substringAfter(uc.getPath(), appConfig.getDesignPath()).replaceFirst("^/", "");

        String applicationName = appConfig.getName();
        ApplicationDO applicationDO = doService.getApplicationDO(applicationName);
        JSONObject input = WebUtils.getJsonByInput(request);

        JSONPath.set(input,"$.context.application",JSONObject.toJSON(applicationDO));
        JSONPath.set(input,"$.context.api_url",  StringUtils.substringBefore(request.getRequestURL().toString(), appConfig.getDesignPath()) + appConfig.getApiPath());


        GroovyConverter converter = new GroovyConverter();
        converter.setRestTemplate(appBean.getRestTemplate());


        Callback<String, DataSource> dataSourceCallBack = param -> doService.getDataSource();

        converter.withBean("apijson",new ApiJsonBean(converter, dataSourceCallBack));

        converter.withBean("jdbcTemplate",new JdbcTemplateBean(converter, new Callback<String, JdbcTemplate>() {
            @Override
            public JdbcTemplate call(String param) {
              return  new MyJdbcTemplate(doService.getDataSource());
            }
        }));


        if ( StringUtils.endsWithAny(uri,".gsp")) {
            // ******* gsp **********************************************

            writeGspResponse( classPath , uri,input,request,response);
        }

        else if ( StringUtils.endsWithAny(uri,".jt",".japi",".jpage")) {
            // ******* jt **********************************************

            writeJTResponse(converter,classPath + uri,input,response);

        } else {

            writeStaticResponse(classPath + uri,response);

        }

    }




}
