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
public class ApiAsyncController {

    public ApiAsyncController(ApiProperties.Application app) {
        this.app = app;
        log.info("Create ApiController {}",app.getPath());
    }

    private final ApiProperties.Application app;


    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) {

        AsyncContext asyncContext = request.startAsync();
        //设置监听器:可设置其开始、完成、异常、超时等事件的回调处理
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onTimeout(AsyncEvent event) {
                log.warn("request timeout", new TimeoutException());
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
                //线程开始;
            }

            @Override
            public void onError(AsyncEvent event) {
                log.warn("request error", event.getThrowable());
            }

            @Override
            public void onComplete(AsyncEvent event) {
                //这里可以做一些清理资源的操作...
            }
        });

        //设置超时时间
        asyncContext.setTimeout(60000);
        asyncContext.start(() -> {

            HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
            HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();

            try {

                doExecute(req, res);

            } catch (Exception e) {

                try {
                    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                } catch (Exception e1) {
                    log.error("Http Response Error fail", e1);
                }

            }
            //异步请求完成通知
            //此时整个请求才完成
            asyncContext.complete();
        });

        //request的线程连接已经释放了

    }

    public void doExecute(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = request.getRequestURI();
        url = url.replaceFirst(request.getServletContext().getContextPath() + app.getPath(), "");

        String subPath = "/" + UriComponentsBuilder.fromUriString(url).build().getPathSegments().get(0);


    }


}
