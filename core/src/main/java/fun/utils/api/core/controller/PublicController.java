package fun.utils.api.core.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
public class PublicController extends BaseController {


    protected final String classPath = "classpath:fu-api/public/";

    public PublicController(ApiProperties.Application appConfig, AppBean appBean) {
        super(appConfig, appBean);
    }


    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {

        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), appConfig.getPubPath()).replaceFirst("^/", "");

        writeStatusResponse(classPath + uri,response);

    }


}
