package fun.utils.api.core.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.apijson.ApiJsonCaller;
import fun.utils.common.DataUtils;
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
import java.util.function.Consumer;

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
        JSONObject input = WebUtils.getJsonByInput(request);

        // ******* document **********************************************
        if ("document_list.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "document_list.jpage");
            DesignUtils.writeResponse(response, jpage, input);

        }
        else if ("document_edit.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "document_edit.jpage");
            writeResponse(response, jpage, input);

        }
        else if ("document.api".equalsIgnoreCase(filename)) {

            JSONObject configObj = WebUtils.loadJSONObject(webApplicationContext, classPath, "document_config.json");
            String _act = DataUtils.jsonValueByPath(input, "parameters._act", "body.params._act", "body._act");

            if ("get".equalsIgnoreCase(_act)) {

                String _for = DataUtils.jsonValueByPath(input, "parameters._for", "body.params._for", "body._for");

                if ("update".equalsIgnoreCase(_for)) {

                    String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");

                    if (StringUtils.isBlank(id)) {
                        writeApiError(response,503,"没有id参数");
                        return;
                    }

                    JSONObject paramObj = new JSONObject();
                    paramObj.put("id", id);

                    JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("get-input"), paramObj);

                    ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                    JSONObject apiJsonResult = apiJsonCaller.get(getData);

                    JSONObject outputData = DataUtils.fullRefJSON(configObj.getJSONObject("get-output"), apiJsonResult);
                    outputData.getJSONObject("data").put("_act", "update");

                    writeResponse(response, outputData);

                }
                else if ("insert".equalsIgnoreCase(_for)) {

                    JSONObject outputData = configObj.getJSONObject("new-default");
                    outputData.getJSONObject("data").put("_act", "insert");
                    writeResponse(response, outputData);

                }

            }
            else if ("query".equalsIgnoreCase(_act)) {

                String parent_type = DataUtils.jsonValueByPath(input, "parameters._type", "body.params._type", "body._type");

                JSONObject paramObj = new JSONObject();
                paramObj.put("parent_type", parent_type);
                paramObj.put("parent_id", applicationDO.getId());

                JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("query-input"), paramObj);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.get(getData);

                writeResponse(response, configObj.getJSONObject("query-output"), result);

            }
            else if ("update".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                JSONObject putData = DataUtils.fullRefJSON(configObj.getJSONObject("put-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.put(putData);

                writeResponse(response, configObj.getJSONObject("put-output"), result);

            }
            else if ("insert".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());

                JSONObject postData = DataUtils.fullRefJSON(configObj.getJSONObject("post-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.post(postData);

                writeResponse(response, configObj.getJSONObject("post-output"), result);

            }
            else if ("delete".equalsIgnoreCase(_act)) {

                String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");
                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();


                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());
                srcData.put("id", id);

                JSONObject deleteData = DataUtils.fullRefJSON(configObj.getJSONObject("delete-input"), srcData);
                JSONObject result = apiJsonCaller.delete(deleteData);

                writeResponse(response, configObj.getJSONObject("delete-output"), result);

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
                writeResponse(response, configObj.getJSONObject("sort-output"), result);
            }
            else {
                writeJPageError(response, 500, "无效的_act参数", input);
            }
        }

        // ******* filter **********************************************

        else if ("filter_list.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "filter_list.jpage");
            writeResponse(response, jpage, input);

        }
        else if ("filter_edit.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "filter_edit.jpage");
            writeResponse(response, jpage, input);

        }
        else if ("filter.api".equalsIgnoreCase(filename)) {

            JSONObject configObj = WebUtils.loadJSONObject(webApplicationContext, classPath, "filter_config.json");
            String _act = DataUtils.jsonValueByPath(input, "parameters._act", "body.params._act", "body._act");

            if ("get".equalsIgnoreCase(_act)) {

                String _for = DataUtils.jsonValueByPath(input, "parameters._for", "body.params._for", "body._for");

                if ("update".equalsIgnoreCase(_for)) {

                    String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");

                    if (StringUtils.isBlank(id)) {
                        writeApiError(response,503,"没有id参数");
                        return;
                    }

                    JSONObject paramObj = new JSONObject();
                    paramObj.put("id", id);

                    JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("get-input"), paramObj);

                    ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                    JSONObject apiJsonResult = apiJsonCaller.get(getData);

                    JSONObject outputData = DataUtils.fullRefJSON(configObj.getJSONObject("get-output"), apiJsonResult);
                    outputData.getJSONObject("data").put("_act", "update");

                    writeResponse(response, outputData);

                }
                else if ("insert".equalsIgnoreCase(_for)) {

                    JSONObject outputData = configObj.getJSONObject("new-default");
                    outputData.getJSONObject("data").put("_act", "insert");
                    writeResponse(response, outputData);

                }

            }
            else if ("query".equalsIgnoreCase(_act)) {

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
                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();


                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());
                srcData.put("id", id);

                JSONObject deleteData = DataUtils.fullRefJSON(configObj.getJSONObject("delete-input"), srcData);


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


        // ******* parameter **********************************************

        else if ("parameter_list.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "parameter_list.jpage");
            writeResponse(response, jpage, input);

        }
        else if ("parameter_edit.jpage".equalsIgnoreCase(filename)) {

            JSONObject jpage = WebUtils.loadJSONObject(webApplicationContext, classPath, "parameter_edit.jpage");
            writeResponse(response, jpage, input);

        }
        else if ("parameter.api".equalsIgnoreCase(filename)) {

            JSONObject configObj = WebUtils.loadJSONObject(webApplicationContext, classPath, "parameter_config.json");
            String _act = DataUtils.jsonValueByPath(input, "parameters._act", "body.params._act", "body._act");

            if ("get".equalsIgnoreCase(_act)) {

                String _for = DataUtils.jsonValueByPath(input, "parameters._for", "body.params._for", "body._for");

                if ("update".equalsIgnoreCase(_for)) {

                    String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");

                    if (StringUtils.isBlank(id)) {
                        writeApiError(response,503,"没有id参数");
                        return;
                    }

                    JSONObject paramObj = new JSONObject();
                    paramObj.put("id", id);

                    JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("get-input"), paramObj);

                    ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                    JSONObject apiJsonResult = apiJsonCaller.get(getData);

                    JSONObject outputData = DataUtils.fullRefJSON(configObj.getJSONObject("get-output"), apiJsonResult);
                    outputData.getJSONObject("data").put("_act", "update");

                    writeResponse(response, outputData);

                }
                else if ("insert".equalsIgnoreCase(_for)) {

                    JSONObject outputData = configObj.getJSONObject("new-default");
                    outputData.getJSONObject("data").put("_act", "insert");
                    writeResponse(response, outputData);

                }

            }
            else if ("query".equalsIgnoreCase(_act)) {

                String parent_type = DataUtils.jsonValueByPath(input, "parameters._type", "body.params._type", "body._type");

                JSONObject paramObj = new JSONObject();
                paramObj.put("parent_type", parent_type);
                paramObj.put("parent_id", applicationDO.getId());

                JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("query-input"), paramObj);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject callerResult = apiJsonCaller.get(getData);

                JSONObject result = DataUtils.fullRefJSON(configObj.getJSONObject("query-output"), callerResult);
                JSONArray rows = result.getJSONObject("data").getJSONArray("rows");
                rows.forEach(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        JSONObject parameterItem = (JSONObject) o;
                        if ("object".equalsIgnoreCase(parameterItem.getString("data_type"))) {
                            JSONObject tempObj = new JSONObject();
                            tempObj.put("parent_type", "parameter");
                            tempObj.put("parent_id", parameterItem.getString("id"));
                            JSONObject tempGet = DataUtils.fullRefJSON(configObj.getJSONObject("query-input"), tempObj);
                            JSONObject tempCallerResult = apiJsonCaller.get(tempGet);
                            JSONObject tempResult = DataUtils.fullRefJSON(configObj.getJSONObject("query-output"), tempCallerResult);
                            JSONArray tempRows = tempResult.getJSONObject("data").getJSONArray("rows");
                            parameterItem.put("children", tempRows);
                            for (Object tmpO : tempRows) {
                                accept(tmpO);
                            }
                        }
                    }
                });

                DesignUtils.writeResponse(response, result);

            }
            else if ("update".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                String parent_type = srcData.getString("parent_type");
                if ("application".equals(parent_type)){
                    srcData.put("parent_id", applicationDO.getId());
                }else{
                    srcData.put("position", "");
                }

                JSONObject putData = DataUtils.fullRefJSON(configObj.getJSONObject("put-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.put(putData);

                DesignUtils.writeResponse(response, configObj.getJSONObject("put-output"), result);

            }
            else if ("insert".equalsIgnoreCase(_act)) {

                JSONObject srcData = input.getJSONObject("body");
                String parent_type = srcData.getString("parent_type");
                if ("application".equals(parent_type)){
                    srcData.put("parent_id", applicationDO.getId());
                }else{
                    srcData.put("position", "");
                }

                srcData.put("sort",(System.currentTimeMillis() / 1000) % 1000000000);
                JSONObject postData = DataUtils.fullRefJSON(configObj.getJSONObject("post-input"), srcData);

                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();
                JSONObject result = apiJsonCaller.post(postData);

                writeResponse(response, configObj.getJSONObject("post-output"), result);

            }
            else if ("delete".equalsIgnoreCase(_act)) {

                String id = DataUtils.jsonValueByPath(input, "parameters.id", "body.params.id", "body.id");
                ApiJsonCaller apiJsonCaller = doService.getApiJsonCaller();

                JSONObject paramObj = new JSONObject();
                paramObj.put("parameter_id",id);
                JSONObject getData = DataUtils.fullRefJSON(configObj.getJSONObject("delete-test"), paramObj);
                JSONObject getResult = apiJsonCaller.get(getData);

                if (getResult.getInteger("total") > 0){
                    writeApiError(response,503,"有子参数,不能删除");
                    return;
                }

                JSONObject srcData = input.getJSONObject("body");
                srcData.put("parent_type", "application");
                srcData.put("parent_id", applicationDO.getId());
                srcData.put("id", id);

                JSONObject deleteData = DataUtils.fullRefJSON(configObj.getJSONObject("delete-input"), srcData);


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
