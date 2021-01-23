package fun.utils.api.core.controller;

import fun.utils.api.core.script.GroovyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ApiController {

    private final ApiProperties.Application app;
    private final ApiExecutor executor;
    private final WebApplicationContext webApplicationContext;
    private final GroovyService groovyService;

    public ApiController(WebApplicationContext webApplicationContext,ApiProperties.Application app, ApiExecutor executor) {
        this.app = app;
        this.executor = executor;
        this.webApplicationContext = webApplicationContext;
        this.groovyService = new GroovyService();

        log.info("Create ApiSimpleController {}",app.getPath());
    }

    @ResponseBody
    public Object request(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return executor.doExecute(app ,request);
    }


}
