package fun.utils.api.core.controller;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.api.apijson.ApiJsonCaller;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.common.WebUtils;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.tools.DesignUtils;
import fun.utils.api.tools.DocUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DesignController extends BaseController {


    private final String classPath = "classpath:fu-api/design/";

    public DesignController(ApiProperties.Application app, AppBean appBean) {
        super(app, appBean);
    }


    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, IOException, SQLException {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), app.getDesignPath()).replaceFirst("^/", "");
        String filename = StringUtils.substringAfterLast(url, "/");
        String filenameExt = StringUtils.substringAfterLast(url, ".");
        String filenamePre = StringUtils.substringBeforeLast(filename, ".");

        String applicationName = app.getName();
        ApplicationDO applicationDO = doService.getApplicationDO(applicationName);

        if ("document_list.jpage".equalsIgnoreCase(filename)) {

            JSONObject pageData = new JSONObject();
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/document_list.jpage");
            DesignUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else if ("document_edit.jpage".equalsIgnoreCase(filename)) {

            JSONObject input = WebUtils.getJsonByInput(request);

            String _act = (String) JSONPath.eval(input, "$.parameters._act");

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "document_edit.jpage");

            if ("update".equalsIgnoreCase(_act)) {

                String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id");

                if (StringUtils.isBlank(id)) {
                    writeJPageError(response, 500, "没有id参数");
                }
                else {
                    JSONObject pageData = DesignUtils.getDocumentEditData(doService, Long.valueOf(id));
                    pageData.put("_act", "update");
                    writeResponse(response, jpage, pageData);
                }

            }
            else if ("insert".equalsIgnoreCase(_act)) {
                JSONObject pageData = new JSONObject();
                pageData.put("_act", "insert");
                pageData.put("format", "html");
                pageData.put("permission", "public");
                pageData.put("status", "0");
                writeResponse(response, jpage, pageData);

            }
            else {
                writeJPageError(response, 500, "无效的_act参数", input);
            }

        }
        else if ("document.api".equalsIgnoreCase(filename)) {

            JSONObject input = WebUtils.getJsonByInput(request);

            String _act = DataUtils.jsonValueByPath(input, "parameters._act", "body.params._act", "body._act");

            JSONObject configObj = WebUtils.loadJSONObject(webApplicationContext, classPath, "document_config.json");

            if ("query".equalsIgnoreCase(_act)) {

                String parent_type = DataUtils.jsonValueByPath(input, "parameters._type", "body.params._type", "body._type");

                JSONObject paramObj = new JSONObject();
                paramObj.put("parent_type", parent_type);
                paramObj.put("parent_id", applicationDO.getId());

                JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("query-input"), paramObj);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.get(getData);

                DesignUtils.writeResponse(response, configObj.getJSONObject("query-output"), result);

            }
            else if ("update".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                JSONObject putData = DataUtils.fullRefJSON(configObj.getJSONObject("put-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.put(putData);

                DesignUtils.writeResponse(response, configObj.getJSONObject("put-output"), result);

            }
            else if ("insert".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());

                JSONObject postData = DataUtils.fullRefJSON(configObj.getJSONObject("post-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.post(postData);

                DesignUtils.writeResponse(response, configObj.getJSONObject("post-output"), result);

            }
            else if ("delete".equalsIgnoreCase(_act)) {

                String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");

                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());
                srcData.put("id", id);

                JSONObject deleteData = DataUtils.fullRefJSON(configObj.getJSONObject("delete-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.delete(deleteData);

                DesignUtils.writeResponse(response, configObj.getJSONObject("delete-output"), result);

            }
            else if ("sort".equalsIgnoreCase(_act)) {

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = new JSONObject();
                String ids = DataUtils.jsonValueByPath(input, "parameters.ids", "body.params.ids", "body.ids");
                String[] idArray = StringUtils.split(ids, ",");
                for (int i = 0; i < idArray.length; i++) {
                    JSONObject paramObj = new JSONObject();
                    paramObj.put("id", idArray[i]);
                    paramObj.put("sort", i + 1);
                    JSONObject putData = DataUtils.fullRefJSON(configObj.getJSONObject("sort-input"), paramObj);
                    result = apiJsonCaller.put(putData);
                }
                DesignUtils.writeResponse(response, configObj.getJSONObject("sort-output"), result);
            }
            else {
                writeJPageError(response, 500, "无效的_act参数", input);
            }
        }
        else if ("application_edit.jpage".equalsIgnoreCase(filename)) {

            String referer = request.getHeader("Referer");
            String thisUrl = StringUtils.defaultIfBlank(referer, request.getRequestURL().toString());
            String baseUrl = StringUtils.substringBeforeLast(thisUrl, request.getContextPath() + "/" + app.getDesignPath());
            baseUrl += request.getContextPath();
            JSONObject pageData = DesignUtils.getApplicationEditData(doService, applicationName);
            pageData.put("baseUrl", baseUrl);
            pageData.put("designPath", app.getDesignPath());
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/application_edit.jpage");
            DesignUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else if ("application_edit.do".equalsIgnoreCase(filename)) {

            JSONObject input = WebUtils.getJsonByInput(request);


            JSONObject fromObj = new JSONObject();
            fromObj.put("tag", "API_APPLICATION");
            JSONObject appData = new JSONObject();
            appData.put("id", "@{id}");
            appData.put("note", "@{note}");
            appData.put("owner", "@{owner}");
            appData.put("title", "@{title}");
            appData.put("version", "@{version}");
            appData.put("status", "@{status}");

            fromObj.put("API_APPLICATION", appData);

            JSONObject putData = DataUtils.fullRefJSON(fromObj, input.getJSONObject("body"));

            ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
            JSONObject result = apiJsonCaller.put(putData);

            doService.reloadApplicationDO(applicationName);

            JSONObject template = new JSONObject();
            template.put("data", "@{API_APPLICATION}");
            template.put("code", "@{code}");
            template.put("msg", "@{msg}");
            DesignUtils.writeResponse(response, template, result);

        }
        else if ("application_config.jpage".equalsIgnoreCase(filename)) {

            JSONObject pageData = DesignUtils.getApplicationConfigData(doService, applicationName);
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/application_config.jpage");
            DesignUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else if ("application_config.do".equalsIgnoreCase(filename)) {

            JSONObject input = WebUtils.getJsonByInput(request);


            JSONObject fromObj = new JSONObject();
            fromObj.put("tag", "API_APPLICATION");
            JSONObject appData = new JSONObject();
            appData.put("id", "@{id}");
            appData.put("config", "@{config}");
            fromObj.put("API_APPLICATION", appData);

            JSONObject putData = DataUtils.fullRefJSON(fromObj, input.getJSONObject("body"));

            ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
            JSONObject result = apiJsonCaller.put(putData);

            doService.reloadApplicationDO(applicationName);

            JSONObject template = new JSONObject();
            template.put("data", "@{API_APPLICATION}");
            template.put("code", "@{code}");
            template.put("msg", "@{msg}");
            DesignUtils.writeResponse(response, template, result);

        }
        else if ("application_error.jpage".equalsIgnoreCase(filename)) {

            JSONObject pageData = DesignUtils.getApplicationErrorData(doService, applicationName);
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/application_error.jpage");
            DesignUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else if ("application_error.do".equalsIgnoreCase(filename)) {

            JSONObject input = WebUtils.getJsonByInput(request);

            JSONObject fromObj = new JSONObject();
            fromObj.put("tag", "API_APPLICATION");
            JSONObject appData = new JSONObject();
            appData.put("id", "@{id}");
            appData.put("error_codes", "@{errorCodes}");
            fromObj.put("API_APPLICATION", appData);

            JSONObject putData = DataUtils.fullRefJSON(fromObj, input.getJSONObject("body"));

            ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
            JSONObject result = apiJsonCaller.put(putData);

            doService.reloadApplicationDO(applicationName);

            JSONObject template = new JSONObject();
            template.put("data", "@{API_APPLICATION}");
            template.put("code", "@{code}");
            template.put("msg", "@{msg}");
            DesignUtils.writeResponse(response, template, result);

        }
        else if ("parameters.jpage".equalsIgnoreCase(filename)) {

            String idString = StringUtils.defaultIfBlank(request.getParameter("ids"), "");
            String[] ids = idString.split(",");
            List<Long> parameterIds = new ArrayList<>();
            for (String id : ids) {
                if (StringUtils.isNumeric(id)) {
                    parameterIds.add(Long.valueOf(id));
                }
            }
            JSONObject pageData = DocUtils.getParametersDocData(doService, parameterIds);

            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/parameters.jpage");
            DocUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else if ("interface.jpage".equalsIgnoreCase(filename)) {

            String iName = request.getParameter("name");
            String[] iNames = iName.split(":");
            JSONObject pageData = DocUtils.getInterfaceDocData(doService, applicationName, iNames[1], iNames[0]);

            Resource resource = webApplicationContext.getResource("classpath:fu-api/document/interface.jpage");
            DocUtils.writeResponse(response, resource.getInputStream(), pageData);

        }
        else {
            Resource resource = webApplicationContext.getResource("classpath:fu-api/design/" + uri);
            if (resource == null || !resource.exists()) {
                //静态文件资源不存在
                response.sendError(404);
            }
            else {

                //通过文件后缀名分析文件的媒体类型
                //List<MediaType> mediaTypes = MediaTypeFactory.getMediaTypes(uri);
                //MediaType mediaType = mediaTypes.size() > 0 ? mediaTypes.get(0)  : MediaType.TEXT_PLAIN;

                String contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.TEXT_PLAIN).toString();
                response.setContentType(contentType);
                //写入返回流
                byte[] returnBytes = IOUtils.toByteArray(resource.getInputStream());
                IOUtils.write(returnBytes, response.getOutputStream());
            }
        }


    }


}