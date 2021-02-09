package fun.utils.api.doc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.common.ValidConfig;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.services.DoService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DocUtils {

    public static JSONArray getParametersDocData(DoService doService, List<Long> parameterIds ) throws ExecutionException {
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
        if (parameterDO.getAlias() != null & parameterDO.getAlias().size() > 0){
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

        JSONArray children = getParametersDocData(doService,parameterDO.getParameterIds());
        result.put("children",children);

        return result;

    }


    public static JSONObject getApplicationDocData(DoService doService, String applicationName ) throws ExecutionException {
        JSONObject toObj = new JSONObject();
        toObj.put("documentIds","@{documentIds}");
        toObj.put("filterIds","@{filterIds}");
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
        return result;
    }

}
