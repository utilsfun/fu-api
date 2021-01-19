package fun.utils.api.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ApiSyncController {

    public ApiSyncController(ApiProperties.Application app) {
        this.app = app;
        log.info("Create ApiSyncController {}",app.getPath());
    }

    private final ApiProperties.Application app;


    @ResponseBody
    public Object request(HttpServletRequest request, HttpServletResponse response) {

        String url = request.getRequestURI();
        url = url.replaceFirst(request.getServletContext().getContextPath() + app.getPath(), "");

        String subPath = "/" + UriComponentsBuilder.fromUriString(url).build().getPathSegments().get(0);

        return null;

    }

    public Object doExecute(HttpServletRequest request) throws Exception {
        return null;
    }


}
