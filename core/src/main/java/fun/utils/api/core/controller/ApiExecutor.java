package fun.utils.api.core.controller;

import javax.servlet.http.HttpServletRequest;

public interface ApiExecutor {
    public Object doExecute(ApiProperties.Application app, HttpServletRequest request);
}
