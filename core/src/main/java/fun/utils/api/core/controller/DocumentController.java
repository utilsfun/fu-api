package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.services.DoService;
import fun.utils.api.doc.DocUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DocumentController {

    private final AppBean appBean;
    private final DoService doService;
    private final ApiProperties.Application app;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public DocumentController(ApiProperties.Application app, AppBean appBean) {

        this.appBean = appBean;
        this.app = app;
        this.doService = appBean.getDoService();

        log.info("Create DocController path:{} name:{}", app.getDocPath(), app.getName());
    }

    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), app.getDocPath()).replaceFirst("^/", "");
        String filename = StringUtils.substringAfterLast(url, "/");
        String filenameExt = StringUtils.substringAfterLast(url, ".");
        String filenamePre = StringUtils.substringBeforeLast(filename, ".");

        String applicationName = app.getName();


        if ("document.jpage".equalsIgnoreCase(filename)) {

            String id = request.getParameter("id");
            JSONObject pageData = DocUtils.getDocumentDocData(doService,Long.valueOf(id));

            String reName = "document_" +  pageData.get("format") + ".jpage";
            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/" + reName);
            DocUtils.writeResponse(response, resource.getInputStream(),pageData);

        }
        else if ("application.jpage".equalsIgnoreCase(filename)) {

            String referer = request.getHeader("Referer");
            String thisUrl = StringUtils.defaultIfBlank(referer, request.getRequestURL().toString());
            String baseUrl = StringUtils.substringBeforeLast(thisUrl,request.getContextPath() + "/" + app.getDocPath());
            baseUrl += request.getContextPath() ;
            JSONObject pageData = DocUtils.getApplicationDocData(doService,applicationName);
            pageData.put("baseUrl",baseUrl);
            pageData.put("apiPath",app.getApiPath());
            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/application.jpage");
            DocUtils.writeResponse(response, resource.getInputStream(),pageData);

        }
        else if ("parameters.jpage".equalsIgnoreCase(filename)) {

            String idString = StringUtils.defaultIfBlank(request.getParameter("ids"),"");
            String[] ids = idString.split(",");
            List<Long> parameterIds = new ArrayList<>();
            for (String id:ids) {
                if (StringUtils.isNumeric(id)) {
                    parameterIds.add(Long.valueOf(id));
                }
            }
            JSONObject pageData = DocUtils.getParametersDocData(doService,parameterIds);

            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/parameters.jpage");
            DocUtils.writeResponse(response, resource.getInputStream(),pageData);

        }
        else if ("interface.jpage".equalsIgnoreCase(filename)) {

            String iName = request.getParameter("name");
            String[] iNames = iName.split(":");
            JSONObject pageData = DocUtils.getInterfaceDocData(doService,applicationName,iNames[1], iNames[0]);

            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/interface.jpage");
            DocUtils.writeResponse(response, resource.getInputStream(),pageData);

        }
        else {
            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/" + uri);
            if (resource == null || !resource.exists()) {
                //静态文件资源不存在
                response.sendError(404);
            }
            else {
                //通过文件后缀名分析文件的媒体类型

                //List<MediaType> mediaTypes = MediaTypeFactory.getMediaTypes(uri);
                //MediaType mediaType = mediaTypes.size() > 0 ? mediaTypes.get(0)  : MediaType.TEXT_PLAIN;


                String contentType = null;

                if ("woff2".equalsIgnoreCase(filenameExt)) {
                    contentType = "font/woff2";
                }
                else if ("page".equalsIgnoreCase(filenameExt)) {
                    contentType = MediaType.APPLICATION_JSON.toString();
                }
                else {
                    contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.TEXT_PLAIN).toString();
                }

                response.setContentType(contentType);
                //写入返回流
                byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
                IOUtils.write(returnBytes, response.getOutputStream());
            }
        }


    }


}
