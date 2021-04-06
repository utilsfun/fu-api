package fun.utils.jsontemplate;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.DataUtils;
import javafx.util.Callback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public class JdbcTemplateBean implements Callback<JSONObject, Object> {


    private final GroovyConverter converter;
    private final Callback<String, JdbcTemplate> jdbcTemplateCallback;

    public JdbcTemplateBean(GroovyConverter converter, Callback<String, JdbcTemplate> jdbcTemplateCallback) {
        this.converter = converter;
        this.jdbcTemplateCallback = jdbcTemplateCallback;
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

        String datasource = StringUtils.defaultIfBlank(param.getString("datasource"), "default");
        JdbcTemplate jdbcTemplate = jdbcTemplateCallback.call(datasource);

        String method = param.getString("method");
        JSONObject result = new JSONObject();

        try {

            JSONObject request = param.getJSONObject("request");
            if (request == null) {
                throw new Exception("field 'request' must be APIJSON JSONObject ");
            }
            else {
                request = DataUtils.copyJSONObject(request);
            }


            log.debug("JdbcTemplate." + method + " : " + JSON.toJSONString(request));


            if ("update".equalsIgnoreCase(method)) {

                String sql = request.getString("sql");
                List<Object> args = request.getJSONArray("args");
                int ret = jdbcTemplate.update(sql, args.toArray());

                result.put("data", ret);
            }
            else if ("query_for_list".equalsIgnoreCase(method)) {

                String sql = request.getString("sql");
                List<Object> args = request.getJSONArray("args");
                List<Map<String, Object>> ret = jdbcTemplate.queryForList(sql, args.toArray());
                result.put("data", ret);

            }
            else if ("query_for_map".equalsIgnoreCase(method)) {

                String sql = request.getString("sql");
                List<Object> args = request.getJSONArray("args");
                Map<String, Object> ret = jdbcTemplate.queryForMap(sql, args.toArray());
                result.put("data", ret);

            }
            else {
                throw new Exception("field 'method' must be get,put,post,delete ! but " + method);
            }
            result.put("status", 0);
            result.put("msg", "success");

        } catch (Exception e) {

            result.put("status", 500);
            result.put("msg", e.toString());

        }

        Object template = param.get("template");

        if (template == null) {
            return result;
        }
        else {
            return converter.convert(template, new GroovyConverter.SelfObject(converter, result));
        }

    }


}
