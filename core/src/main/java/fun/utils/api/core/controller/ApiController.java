package fun.utils.api.core.controller;

import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.runtime.ApiRunner;
import fun.utils.api.core.runtime.RunContext;
import fun.utils.api.core.services.DoService;
import fun.utils.common.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ApiController {

    private final AppBean appBean;
    private final DoService doService;
    private final ApiProperties.Application appConfig;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public ApiController(ApiProperties.Application appConfig, AppBean appBean) {

        this.appBean = appBean;
        this.appConfig = appConfig;
        this.doService = appBean.getDoService();

        log.info("Create ApiController path:{} name:{}", appConfig.getApiPath(), appConfig.getName());
    }

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

                apiExecute(req, res);

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

    public void apiExecute(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String applicationName = appConfig.getName();
        String interfaceName = StringUtils.substringAfter(uc.getPath(), appConfig.getApiPath()).replaceFirst("^/", "");

        RunContext runContext = new RunContext(appBean, applicationName, interfaceName, response, request);
        ApiRunner apiRunner = new ApiRunner(appBean, runContext);


        try {
            apiRunner.run();
        } catch (Exception e) {
            e.printStackTrace();
            runContext.setResult(e);
        }


        try {

            //是否通过返回对象返回数据, isVoid==true, 代码自行操作response对象返回
            if (!runContext.isVoid()) {
                //
                Object retObj = runContext.getResult();

                //判断是否为接口错误
                ApiException exception = null;

                if (retObj == null) {
                    exception = ApiException.resultNullException();
                }
                else if (retObj instanceof ApiException) {
                    exception = (ApiException) retObj;
                }
                else if (retObj instanceof Exception) {
                    exception = ApiException.unknownException((Throwable) retObj);
                }
                else if (retObj instanceof Throwable) {
                    exception = ApiException.unknownException((Throwable) retObj);
                }

                Map<String, Object> ret = new HashMap<>();

                if (exception == null) {
                    //接口成功时
                    ret.put("code", 200);
                    ret.put("message", "success");
                    ret.put("result", retObj);

                }
                else {
                    //接口出错时

                    ret.put("code", exception.code);
                    ret.put("message", exception.getMessage());
                    ret.put("detail", exception.detail);
                }

                //添加返回 header 数据
                runContext.getResponseHeaders().forEach((k, v) -> {
                    response.setHeader(k, v);
                });


                //返回内容格式化为 application/json
                byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
                response.setContentType("application/json; charset=utf-8");

                //写入返回流
                IOUtils.write(returnBytes, response.getOutputStream());

            }

        } finally {
            runContext.setCompleted(true);
        }
    }


}
