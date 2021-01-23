package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import fun.utils.api.core.services.DoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ApiDefaultExecutor implements ApiExecutor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DoService doService;

    @Override
    public Object doExecute(ApiProperties.Application app, HttpServletRequest request) throws ExecutionException {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc =  UriComponentsBuilder.fromUriString(url).build();

        String applicationName = uc.getPathSegments().get(0);
        String interfaceName = uc.getPathSegments().get(1);

        System.out.println( applicationName + "/" + interfaceName );

        return JSON.toJSON(doService.getApplicationDO(applicationName));



    }
}
