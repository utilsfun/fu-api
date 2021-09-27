package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.services.DoService;
import fun.utils.jsontemplate.common.DataUtils;
import fun.utils.jsontemplate.core.GroovyConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BaseController {

    private static final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

    protected final AppBean appBean;
    protected final DoService doService;
    protected final ApiProperties.Application appConfig;


    @Autowired
    protected WebApplicationContext webApplicationContext;

    public BaseController(ApiProperties.Application appConfig, AppBean appBean) {

        this.appBean = appBean;
        this.appConfig = appConfig;
        this.doService = appBean.getDoService();

        log.info("Create {} path:{} name:{}", this.getClass().getSimpleName(), appConfig.getApiPath(), appConfig.getName());
    }


    public void writeResponse(HttpServletResponse response, Object data) throws IOException {

        Object result = ObjectUtils.clone(data);

        //返回内容格式化为 application/json
        byte[] returnBytes = DataUtils.toWebJSONString(result).getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/json; charset=utf-8");
        //写入返回流
        IOUtils.write(returnBytes, response.getOutputStream());

    }


    public void writeJTResponse(GroovyConverter converter, String resourceUrl, JSON data, HttpServletResponse response) throws IOException {

        Resource resource = webApplicationContext.getResource(resourceUrl);

        if (resource == null || !resource.exists()) {
            //静态文件资源不存在
            writeApiError(response, 404, "can't find resource " + resourceUrl);
        }
        else {

            Object template = JSON.parse(IOUtils.toByteArray(resource.getInputStream()));

            try {

                Object result = converter.convert(template, data);
                writeResponse(response, result);

            } catch (Exception e) {

                //convert 失败
                writeApiError(response, 503, "can't convert " + resourceUrl);
                log.warn("can't convert " + resourceUrl, e);
            }

        }

    }


    public void writeGspResponse(String classPath, String uri, JSONObject input, HttpServletRequest request, HttpServletResponse response) throws IOException, ScriptException {

        Resource resource = webApplicationContext.getResource(classPath + uri);

        if (resource == null || !resource.exists()) {
            //静态文件资源不存在
            response.sendError(404, "can't find resource " + uri);
        }
        else {

            String code = IOUtils.toString(resource.getInputStream(), "utf-8");
            Bindings bindings = new SimpleBindings();
            Map<String,Object> context = new HashMap<>();

            context.put("request", request);
            context.put("response", response);
            context.put("session",  request.getSession());
            context.put("application", request.getSession().getServletContext());
            context.put("webApplicationContext", webApplicationContext);

            bindings.put("$context",context);


//            Map<String,Object> context = $context;
//            HttpServletRequest request = context.get("request") ;
//            HttpServletResponse response = context.get("response") ;
//            HttpSession session = context.get("session") ;
//            WebApplicationContext webApplicationContext = context.get("webApplicationContext") ;

            log.debug("groovyEngine.eval(\"" + code + "\")");
            groovyEngine.eval(code, bindings);

        }

    }

    public void writeStaticResponse(String resourceUrl, HttpServletResponse response) throws IOException {

        Resource resource = webApplicationContext.getResource(resourceUrl);
        if (resource == null || !resource.exists()) {
            //静态文件资源不存在
            response.sendError(404);
        }
        else {
            //通过文件后缀名分析文件的媒体类型

            //List<MediaType> mediaTypes = MediaTypeFactory.getMediaTypes(uri);
            //MediaType mediaType = mediaTypes.size() > 0 ? mediaTypes.get(0)  : MediaType.TEXT_PLAIN;

            String contentType = null;

            if (resourceUrl.endsWith(".woff2")) {
                contentType = "font/woff2";
            }
            else {
                contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.TEXT_PLAIN).toString();
            }

            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=1800");

            //写入返回流
            byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
            IOUtils.write(returnBytes, response.getOutputStream());
        }

    }


    public void writeApiError(HttpServletResponse response, int code, String message) throws IOException {
        JSONObject data = new JSONObject();
        data.put("status", code);
        data.put("msg", message);
        response.setStatus(code);
        writeResponse(response, data);
    }


    public void writeApiSuccess(HttpServletResponse response, Object data) throws IOException {
        JSONObject result = new JSONObject();
        result.put("status", 0);
        result.put("msg", "success");
        result.put("data", data);
        writeResponse(response, result);
    }


}
