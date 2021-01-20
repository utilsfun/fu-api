package fun.utils.api.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ApiController {

    private final ApiProperties.Application app;
    private final ApiExecutor executor;

    public ApiController(ApiProperties.Application app, ApiExecutor executor) {
        this.app = app;
        this.executor = executor;
        log.info("Create ApiSimpleController {}",app.getPath());
    }


    @ResponseBody
    public Object request(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = request.getRequestURI();
        url = url.replaceFirst(request.getServletContext().getContextPath() + app.getPath(), "");

        String subPath = "/" + UriComponentsBuilder.fromUriString(url).build().getPathSegments().get(0);

        return executor.doExecute(app ,request);

    }

}
