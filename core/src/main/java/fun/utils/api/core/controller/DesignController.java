package fun.utils.api.core.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.DataUtils;
import fun.utils.api.core.common.WebUtils;
import fun.utils.api.core.persistence.ApplicationDO;

import fun.utils.common.apijson.ApiJsonCaller;
import fun.utils.jsontemplate.ApiJsonBean;
import fun.utils.jsontemplate.GroovyConverter;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class DesignController extends BaseController {


    private final String classPath = "classpath:fu-api/design/";

    public DesignController(ApiProperties.Application app, AppBean appBean) {
        super(app, appBean);
    }


    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws Exception {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), app.getDesignPath()).replaceFirst("^/", "");
        String filename = StringUtils.substringAfterLast(url, "/");
        String filenameExt = StringUtils.substringAfterLast(url, ".");
        String filenamePre = StringUtils.substringBeforeLast(filename, ".");

        String applicationName = app.getName();
        ApplicationDO applicationDO = doService.getApplicationDO(applicationName);
        JSONObject input = WebUtils.getJsonByInput(request);

        JSONPath.set(input,"$.context.application",JSONObject.toJSON(applicationDO));


        GroovyConverter converter = new GroovyConverter();

        Callback<String, DataSource> dataSourceCallBack = new Callback<String, DataSource>() {
            @Override
            public DataSource call(String param) {
                return doService.getDataSource();
            }
        };

        converter.withBean("apijson",new ApiJsonBean(converter, dataSourceCallBack));



        // ******* jt **********************************************
        if ( StringUtils.endsWithAny(filename,".jt",".japi",".jview")) {

            JSONObject jsonTemplate = loadJSONObject(classPath, filename);
            JSONObject data =  converter.convert(jsonTemplate,input);
            writeJsonSuccess(response,data);

        } else {
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/" + uri);
            if (resource == null || !resource.exists()) {
                //静态文件资源不存在
                response.sendError(404);
            }
            else {

                //通过文件后缀名分析文件的媒体类型
                //List<MediaType> mediaTypes = MediaTypeFactory.getMediaTypes(uri);
                //MediaType mediaType = mediaTypes.size() > 0 ? mediaTypes.get(0)  : MediaType.TEXT_PLAIN;

                String contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.TEXT_PLAIN).toString();
                response.setContentType(contentType);
                //写入返回流
                byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
                IOUtils.write(returnBytes, response.getOutputStream());
            }
        }


    }


}
