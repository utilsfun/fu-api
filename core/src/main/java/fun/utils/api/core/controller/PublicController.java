package fun.utils.api.core.controller;


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
import java.util.concurrent.ExecutionException;

@Slf4j
public class PublicController {

    private final String pubPath;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public PublicController(String pubPath) {
        this.pubPath = pubPath;
        log.info("Create PubController path:{}", pubPath);
    }

    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), pubPath).replaceFirst("^/", "");
        String filename = StringUtils.substringAfterLast(url, "/");
        String filenameExt = StringUtils.substringAfterLast(url, ".");
        String filenamePre = StringUtils.substringBeforeLast(filename, ".");


        Resource resource = webApplicationContext.getResource("classpath:fu-api/public/" + uri);
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
