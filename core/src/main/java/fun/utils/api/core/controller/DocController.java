package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.exception.ApiException;
import fun.utils.api.core.runtime.ApiRunner;
import fun.utils.api.core.runtime.RunContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
public class DocController {

    private final AppBean appBean;
    private final ApiProperties.Application app;

    public DocController(ApiProperties.Application app, AppBean appBean) {
        this.appBean = appBean;
        this.app = app;
        log.info("Create DocController path:{} name:{}", app.getPath(), app.getName());
    }

    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String target = request.getParameter("target");

    }

}
