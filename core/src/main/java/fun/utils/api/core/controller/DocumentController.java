package fun.utils.api.core.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.api.core.common.ValidConfig;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.services.DoService;
import fun.utils.common.WebUtils;
import fun.utils.jsontemplate.beans.ApiJsonBean;
import fun.utils.jsontemplate.common.DataUtils;
import fun.utils.jsontemplate.core.GroovyConverter;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DocumentController extends BaseController {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final String classPath = "classpath:fu-api/document/";

    public DocumentController(ApiProperties.Application appConfig, AppBean appBean) {
        super(appConfig, appBean);
    }


    static class ParameterBean {

        private DoService doService;

        public ParameterBean(DoService doService) {
            this.doService = doService;
        }


        public  JSONObject getParametersDocData(String parameters ) throws ExecutionException {
            String[] ids = parameters.split(",");
            List<Long> parameterIds = new ArrayList<>();
            for (String id:ids) {
                if (StringUtils.isNumeric(id)) {
                    parameterIds.add(Long.valueOf(id));
                }
            }
            return getParametersDocData(parameterIds);
        }

        public  JSONObject getParametersDocData( List<Long> parameterIds ) throws ExecutionException {
            JSONObject result = new JSONObject();
            result.put("items",getParametersDocArray(parameterIds));
            return result;
        }

        public  JSONArray getParametersDocArray(List<Long> parameterIds ) throws ExecutionException {
            JSONArray result = new JSONArray();
            if (parameterIds != null){
                for (Long childrenId:parameterIds) {
                    result.add(getParameterDocData(childrenId));
                }
            }
            return result;
        }

        public  JSONObject getParameterDocData(long parameterId ) throws ExecutionException {

            JSONObject result = new JSONObject();
            ParameterDO parameterDO = doService.getParameterDO(parameterId);

            String name = parameterDO.getName();
            if (parameterDO.getAlias() != null && parameterDO.getAlias().size() > 0){
                name += "\r\n" + parameterDO.getAlias().toString();
            }
            result.put("name",name);

            String title = parameterDO.getTitle();
            result.put("title",title);

            boolean isRequired = parameterDO.getIsRequired() == 1;
            result.put("isRequired",isRequired ? "是" : "否");

            //result.put("isRequired", parameterDO.getIsRequired());

            String dataType = parameterDO.getDataType();
            boolean isArray = parameterDO.getIsArray() == 1;
            if (isArray){
                dataType += "[] 数组";
            }
            result.put("dataType",dataType);

            String defaultValue = parameterDO.getDefaultValue();
            result.put("defaultValue",defaultValue);


            String note = parameterDO.getNote();
            List<String> notes = new ArrayList<>();
            if (StringUtils.isNotBlank(note)){
                notes.add(note);
            }

            List<ValidConfig> validConfigs = parameterDO.getValidations();
            if (validConfigs != null){

                for (ValidConfig validConfig:validConfigs) {
                    if (validConfig == null){
                        continue;
                    }
                    notes.add("须:" + validConfig.getType() + "(" + DataUtils.valueFormat(validConfig.getData(),validConfig.message) + ")" );
                }


            }

            List<String> examples = parameterDO.getExamples();
            if (examples != null){
                for (String example:examples) {
                    notes.add("例:" + example);
                }
            }

            result.put("note", StringUtils.join(notes,"\r\n"));

            JSONArray children = getParametersDocArray(parameterDO.getParameterIds());
            result.put("children",children);

            return result;

        }
    }

    @ResponseBody
    public void request(HttpServletRequest request, HttpServletResponse response) throws Exception {


        String url = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        UriComponents uc = UriComponentsBuilder.fromUriString(url).build();

        String uri = StringUtils.substringAfter(uc.getPath(), appConfig.getDocPath()).replaceFirst("^/", "");


        String applicationName = appConfig.getName();

        ApplicationDO applicationDO = doService.getApplicationDO(applicationName);
        JSONObject input = WebUtils.getJsonByInput(request);

        JSONPath.set(input,"$.context.application",JSONObject.toJSON(applicationDO));
        JSONPath.set(input,"$.context.api_url",  StringUtils.substringBefore(request.getRequestURL().toString(), appConfig.getDocPath()) + appConfig.getApiPath());

        GroovyConverter converter = new GroovyConverter();
        converter.setRestTemplate(appBean.getRestTemplate());

        Callback<String, DataSource> dataSourceCallBack = param -> doService.getDataSource();

        converter.withBean("apijson",new ApiJsonBean(converter, dataSourceCallBack));
        converter.withBean("doService",doService);

        converter.withBean("parameterBean",new ParameterBean(doService));


        // ******* jt **********************************************
        if ( StringUtils.endsWithAny(uri,".jt",".japi",".jpage")) {

            writeJTResponse(converter,classPath + uri,input,response);

        } else {

            writeStaticResponse(classPath + uri,response);

        }

    }


}
