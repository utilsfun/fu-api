package fun.utils.api.core.controller;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

public interface ApiExecutor {
    public Object doExecute(ApiProperties.Application app, HttpServletRequest request) throws Exception;
}
