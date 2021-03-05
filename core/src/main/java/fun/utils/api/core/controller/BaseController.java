package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class BaseController {

    protected final AppBean appBean;
    protected final DoService doService;
    protected final ApiProperties.Application app;
    protected final String publicClassPath = "classpath:fu-api/public/";

    @Autowired
    protected WebApplicationContext webApplicationContext;

    public BaseController(ApiProperties.Application app, AppBean appBean) {

        this.appBean = appBean;
        this.app = app;
        this.doService = appBean.getDoService();

        log.info("Create {} path:{} name:{}", this.getClass().getSimpleName(), app.getApiPath(), app.getName());
    }


    public JSONObject loadJSONObject(String classPath,String uri) throws IOException {
        Resource resource = webApplicationContext.getResource(classPath + uri);
        JSONObject result = JSON.parseObject(resource.getInputStream(), JSONObject.class);
        return result;
    }

    public JSONArray loadJSONArray(String classPath, String uri) throws IOException {
        Resource resource = webApplicationContext.getResource(classPath + uri);
        JSONArray result = JSON.parseObject(resource.getInputStream(), JSONArray.class);
        return result;
    }


    public void writeResponse(HttpServletResponse response , JSONObject data) throws IOException {

        //返回内容格式化为 application/json
        byte[] returnBytes = DataUtils.toWebJSONString(data).getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/json; charset=utf-8");
        //写入返回流
        IOUtils.write(returnBytes, response.getOutputStream());

    }

    public void writeResponse(HttpServletResponse response , JSONObject template, JSONObject data) throws IOException {

        JSONObject ret = DataUtils.fullRefJSON(template,data);
        writeResponse(response,ret);

    }

    public void writeResponse(HttpServletResponse response , InputStream templateInputStream, JSONObject data) throws IOException {

        JSONObject ret = JSON.parseObject(templateInputStream, JSONObject.class);
        writeResponse (response,ret,data);

    }

    public void writeJPageError(HttpServletResponse response , int code, String message) throws IOException {
        writeJPageError(response, code, message, null);
    }

    public void writeJPageError(HttpServletResponse response , int code, String message, JSONObject info) throws IOException {

        JSONObject errorPage = loadJSONObject(publicClassPath,"error.jpage");
        JSONObject data = new JSONObject();
        data.put("code",code);
        data.put("message",message);
        data.put("info",info);
        JSONObject ret = DataUtils.fullRefJSON(errorPage,data);
        // ret.put("data", pageData);
        //返回内容格式化为 application/json
        byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/json; charset=utf-8");
        //写入返回流
        IOUtils.write(returnBytes, response.getOutputStream());
    }
}
