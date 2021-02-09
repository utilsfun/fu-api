package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.runtime.ApiRunner;
import fun.utils.api.core.runtime.RunContext;
import fun.utils.api.core.services.DoService;
import fun.utils.api.doc.DocUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ApiController {

    private final AppBean appBean;
    private final DoService doService;
    private final ApiProperties.Application app;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public ApiController(ApiProperties.Application app, AppBean appBean) {

        this.appBean = appBean;
        this.app = app;
        this.doService = appBean.getDoService();

        log.info("Create ApiController path:{} name:{}", app.getPath(), app.getName());
    }

    @ResponseBody
    public void apiRequest(HttpServletRequest request, HttpServletResponse response) {

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

        String applicationName = app.getName();
        String interfaceName = StringUtils.substringAfter(uc.getPath(), app.getPath()).replaceFirst("^/", "");

        RunContext runContext = new RunContext(appBean, applicationName, interfaceName, response, request);
        ApiRunner apiRunner = new ApiRunner(appBean, runContext);


        try {
            apiRunner.run();
        } catch (Exception e) {
            runContext.setResult(e);
        }

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

    }

    @ResponseBody
    public void docRequest(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), app.getDocPath()).replaceFirst("^/", "");
        String filename = StringUtils.substringAfterLast(url, "/");
        String filenameExt = StringUtils.substringAfterLast(url, ".");
        String filenamePre = StringUtils.substringBeforeLast(filename, ".");

        if ("document.dochtml".equalsIgnoreCase(filename)) {
            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/index.html");
            if (resource == null || !resource.exists()) {
                //静态文件资源不存在
                response.sendError(404);
            }
            else {
                response.setContentType(MediaType.TEXT_HTML.toString());
                byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
                IOUtils.write(returnBytes, response.getOutputStream());
            }
        }
        else if ("document.jpage".equalsIgnoreCase(filename)) {

            String id = request.getParameter("id");
            JSONObject pageData = DocUtils.getDocumentDocData(doService,Long.valueOf(id));

         //   String reUri = uri.replaceFirst("document\\.jpage","document_" +  pageData.get("format") + ".jpage");

            Resource resource = webApplicationContext.getResource("classpath:fu-api/" + uri);
            JSONObject ret = JSON.parseObject(resource.getInputStream(), JSONObject.class);

            ret = DataUtils.fullRefJSON(ret,pageData);

           // ret.put("data", pageData);
            //返回内容格式化为 application/json
            byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
            response.setContentType("application/json; charset=utf-8");

            //写入返回流
            IOUtils.write(returnBytes, response.getOutputStream());

        }
        else if ("jpage".equalsIgnoreCase(filenameExt)) {

            Resource resource = webApplicationContext.getResource("classpath:fu-api/" + uri);

            JSONObject ret = JSON.parseObject(resource.getInputStream(), JSONObject.class);

            String jurl = request.getRequestURI().replaceFirst("/" + filenamePre + "\\.jpage", "/" + filenamePre + ".jdata");
            jurl += StringUtils.isNotBlank(request.getQueryString()) ? "?" + request.getQueryString() : "";

            ret.put("initApi", jurl);
            //返回内容格式化为 application/json
            byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
            response.setContentType("application/json; charset=utf-8");

            //写入返回流
            IOUtils.write(returnBytes, response.getOutputStream());

        }
        else if ("jdata".equalsIgnoreCase(filenameExt)) {

            Resource resource = webApplicationContext.getResource("classpath:fu-api/" + uri);
            JSONObject jData = JSON.parseObject(resource.getInputStream(), JSONObject.class);


            String applicationName = app.getName();
            JSONObject data = null;

            if ("application".equalsIgnoreCase(filenamePre)) {

                ApplicationDO applicationDO = doService.getApplicationDO(applicationName);
                data = (JSONObject) JSON.toJSON(applicationDO);

            }
            else if ("interface".equalsIgnoreCase(filenamePre)) {

                String iName = request.getParameter("name");
                String[] iNames = iName.split(":");

                InterfaceDO interfaceDO = doService.getInterfaceDO(applicationName, iNames[1], iNames[0]);

                data = (JSONObject) JSON.toJSON(interfaceDO);

            }
            else if ("parameters".equalsIgnoreCase(filenamePre)) {

                String idString = request.getParameter("ids");
                String[] ids = idString.split(",");
                List<Long> parameterIds = new ArrayList<>();
                for (String id:ids) {
                    parameterIds.add(Long.valueOf(id));
                }
                data = new JSONObject();
                data.put("items", DocUtils.getParametersDocData(doService,parameterIds));

            }
            else if ("document".equalsIgnoreCase(filenamePre)) {

                String id = request.getParameter("id");
                DocumentDO documentDO = doService.getDocumentDO(Long.valueOf(id));
                data = (JSONObject) JSON.toJSON(documentDO);

            }
            else {
                response.sendError(404);
                return;
            }

            jData = DataUtils.fullRefJSON(jData,data);

            Map<String, Object> ret = new HashMap<>();

            ret.put("status", 0);
            ret.put("msg", "success");
            ret.put("data", jData);

            //返回内容格式化为 application/json
            byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
            response.setContentType("application/json; charset=utf-8");

            //写入返回流
            IOUtils.write(returnBytes, response.getOutputStream());

        }
        else {
            Resource resource = webApplicationContext.getResource("classpath:fu-api/" + uri);
            if (resource == null || !resource.exists()) {
                //静态文件资源不存在
                response.sendError(404);
            }
            else {
                //通过文件后缀名分析文件的媒体类型

                //List<MediaType> mediaTypes = MediaTypeFactory.getMediaTypes(uri);
                //MediaType mediaType = mediaTypes.size() > 0 ? mediaTypes.get(0)  : MediaType.TEXT_PLAIN;


                String contentType = null;

                if ("woff2".equalsIgnoreCase(filenameExt)) {
                    contentType = "font/woff2";
                }
                else if ("page".equalsIgnoreCase(filenameExt)) {
                    contentType = MediaType.APPLICATION_JSON.toString();
                }
                else {
                    contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.TEXT_PLAIN).toString();
                }

                response.setContentType(contentType);
                //写入返回流
                byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
                IOUtils.write(returnBytes, response.getOutputStream());
            }
        }


    }

    public void getUi(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {


    }

    public void getData(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException {

    }

}
