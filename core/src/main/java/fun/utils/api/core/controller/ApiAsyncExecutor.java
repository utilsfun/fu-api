package fun.utils.api.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ApiAsyncExecutor {
    public void doExecute(HttpServletRequest request, HttpServletResponse response);
}
