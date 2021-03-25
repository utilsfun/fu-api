package fun.utils.jsontemplate;

import apijson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.DataUtils;
import fun.utils.common.apijson.ApiJsonCaller;
import javafx.util.Callback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

@Slf4j
public class ApiJsonBean implements Callback<JSONObject, Object> {


    private final GroovyConverter converter;
    private final Callback<String,DataSource> dataSourceCallback;

    public ApiJsonBean(GroovyConverter converter, Callback<String, DataSource> dataSourceCallback) {
        this.converter = converter;
        this.dataSourceCallback = dataSourceCallback;
    }

    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */
    @SneakyThrows
    @Override
    public Object call(JSONObject param) {

        String datasource = StringUtils.defaultIfBlank(param.getString("datasource"),"default");
        String method = param.getString("method");

        JSONObject request =param.getJSONObject("request");
        if (request == null){
            throw new Exception("field 'request' must be APIJSON JSONObject ");
        }else {
            request =  DataUtils.copyJSONObject(request);
        }

        Object template = param.get("template");

        ApiJsonCaller apiJsonCaller = new ApiJsonCaller(dataSourceCallback.call(datasource));

        JSONObject data = null;

        log.debug("ApiJsonCaller." + method + " : " + JSON.toJSONString(request));

        if ("get".equalsIgnoreCase(method)) {
            data = apiJsonCaller.get(request);
        }
        else if ("put".equalsIgnoreCase(method)) {
            data = apiJsonCaller.put(request);
        }
        else if ("post".equalsIgnoreCase(method)) {
            data = apiJsonCaller.post(request);
        }
        else if ("delete".equalsIgnoreCase(method)) {
            data = apiJsonCaller.delete(request);
        }
        else {
            throw new Exception("field 'method' must be get,put,post,delete ! but " + method);
        }

        if (template == null){
            return data;
        }else{
            return converter.convert(template, new GroovyConverter.SelfObject(converter, data));
        }

    }


}
