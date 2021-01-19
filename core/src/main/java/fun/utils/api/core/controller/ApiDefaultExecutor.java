package fun.utils.api.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class ApiDefaultExecutor implements ApiExecutor {

    @Override
    public Object doExecute(ApiProperties.Application app, HttpServletRequest request) {

        String url = request.getRequestURI();
        url = url.replaceFirst(request.getServletContext().getContextPath() + app.getPath(), "");

        String subPath = "/" + UriComponentsBuilder.fromUriString(url).build().getPathSegments().get(0);

        return null;

    }
}
