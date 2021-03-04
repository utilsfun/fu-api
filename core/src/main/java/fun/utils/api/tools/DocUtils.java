package fun.utils.api.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.common.ValidConfig;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.services.DoService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DocUtils {

    public static JSONObject getParametersDocData(DoService doService, List<Long> parameterIds ) throws ExecutionException {
        JSONObject result = new JSONObject();
        result.put("items",getParametersDocArray(doService,parameterIds));
        return result;
    }

    public static JSONArray getParametersDocArray(DoService doService, List<Long> parameterIds ) throws ExecutionException {
        JSONArray result = new JSONArray();
        if (parameterIds != null){
            for (Long childrenId:parameterIds) {
                result.add(getParameterDocData(doService,childrenId));
            }
        }
        return result;
    }

    public static JSONObject getParameterDocData(DoService doService, long parameterId ) throws ExecutionException {

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

        JSONArray children = getParametersDocArray(doService,parameterDO.getParameterIds());
        result.put("children",children);

        return result;

    }


    public static JSONObject getApplicationDocData(DoService doService, String applicationName ) throws ExecutionException {
        JSONObject toObj = new JSONObject();
        toObj.put("documentIds","@{documentIds}");
        toObj.put("filterIds","@{filterIds}");
        toObj.put("errorCodes","@{errorCodes}");
        toObj.put("gmtModified","@{gmtModified}");
        toObj.put("interfaceNames","@{interfaceNames}");
        toObj.put("name","@{name}");
        toObj.put("note","@{note}");
        toObj.put("owner","@{owner}");
        toObj.put("parameterIds","@{parameterIds}");
        toObj.put("title","@{title}");
        toObj.put("version","@{version}");

        ApplicationDO applicationDO = doService.getApplicationDO(applicationName);
        JSONObject fromObj = (JSONObject) JSON.toJSON(applicationDO);
        JSONObject result = DataUtils.fullRefJSON (toObj,fromObj);

        JSONArray interfaceDocDatas = new JSONArray();
        for (String str: applicationDO.getInterfaceNames()) {
            String[] iNames = str.split(":");
            JSONObject interfaceDocData = getInterfaceDocData(doService,applicationName,iNames[1],iNames[0]);
            interfaceDocDatas.add(interfaceDocData);
        }
        result.put("interfaces",interfaceDocDatas);
        return result;
    }

    public static JSONObject getDocumentDocData(DoService doService, Long documentId ) throws ExecutionException {
        JSONObject toObj = new JSONObject();
        toObj.put("format","@{format}");
        toObj.put("note","@{note}");
        toObj.put("title","@{title}");
        toObj.put("content","@{content}");
        DocumentDO documentDO = doService.getDocumentDO(documentId);
        JSONObject fromObj = (JSONObject) JSON.toJSON(documentDO);
        JSONObject result = DataUtils.fullRefJSON (toObj,fromObj);

        String format = documentDO.getFormat();
        if (StringUtils.startsWithIgnoreCase(format,"code/")){
            String language = StringUtils.removeStartIgnoreCase(format,"code/");
            result.put("format","code");
            result.put("language",language);
        }else if (StringUtils.startsWithIgnoreCase(format,"image/")){
            String language = StringUtils.removeStartIgnoreCase(format,"image/");
            result.put("format","image");
            result.put("language",language);
        }else if (StringUtils.equalsIgnoreCase(format,"amis")){
            result.put("amis",JSONObject.parseObject(documentDO.getContent()));
        }

        return result;
    }

    public static void writeResponse(HttpServletResponse response , InputStream templateInputStream, JSONObject data) throws IOException {

        JSONObject ret = JSON.parseObject(templateInputStream, JSONObject.class);
        ret = DataUtils.fullRefJSON(ret,data);

        // ret.put("data", pageData);
        //返回内容格式化为 application/json
        byte[] returnBytes = DataUtils.toWebJSONString(ret).getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/json; charset=utf-8");
        //写入返回流
        IOUtils.write(returnBytes, response.getOutputStream());
    }

    public static JSONObject getInterfaceDocData(DoService doService,String applicationName, String interfaceName, String method) throws ExecutionException {

        JSONObject toObj = new JSONObject();
        toObj.put("documentIds","@{documentIds}");
        toObj.put("applicationName","@{applicationName}");

        toObj.put("requestExample","@{requestExample}");
        toObj.put("responseExample","@{responseExample}");

        toObj.put("errorCodes","@{errorCodes}");
        toObj.put("filterIds","@{filterIds}");
        toObj.put("gmtModified","@{gmtModified}");
        toObj.put("name","@{name}");
        toObj.put("note","@{note}");
        toObj.put("owner","@{owner}");
        toObj.put("parameterIds","@{parameterIds}");
        toObj.put("title","@{title}");
        toObj.put("method","@{method}");



        InterfaceDO interfaceDO = doService.getInterfaceDO(applicationName,interfaceName,method);
        JSONObject fromObj = (JSONObject) JSON.toJSON(interfaceDO);
        JSONObject result = DataUtils.fullRefJSON (toObj,fromObj);
        return result;

    }

}