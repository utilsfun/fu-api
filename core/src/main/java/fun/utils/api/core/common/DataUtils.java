package fun.utils.api.core.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtils {

    public static <T> List<T> getEmptyIfNull(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        else {
            return list;
        }
    }

    public static <K,V> Map<K,V> getEmptyIfNull(Map<K,V> map) {
        if (map == null) {
            return new HashedMap();
        }
        else {
            return map;
        }
    }

    //{}
    public static String stringFormat(String src, Map<String, Object> parameters) {

        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\s*(\\|[^\\{\\}]+)?\\}");
        Matcher matcher = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = parameters.get(key);
            String exString = matcher.group(2);
            if (exString != null) {
                String[] expressions = StringUtils.split(StringUtils.removeStart(exString.trim(), "|"), "|");
                for (String expression : expressions) {
                    expression = expression.trim();
                    value = valueFormat(value, expression);
                }
            }
            matcher.appendReplacement(sb, String.valueOf(value));
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    public static Object valueFormat(Object value, String expression) {

        if (StringUtils.isBlank(expression)) {
            return value;
        }

        expression = expression.trim();

        if (expression.matches("default:.+")) {
            if (value == null) {
                String string = StringUtils.removeStartIgnoreCase(expression, "default:");
                return JSON.parse(string).toString();
            }
            else {
                return value;
            }
        }
        else if (expression.matches("json")) {
            if (value == null) {
                return null;
            }
            else {
                return JSON.toJSONString(value, false);
            }
        }
        else if (expression.matches("json\\s*:\\s*pretty")) {
            if (value == null) {
                return null;
            }
            else {
                return JSON.toJSONString(value, true);
            }
        }
        else if (expression.matches("toJson")) {
            if (value == null) {
                return null;
            }
            else {
                String string = value.toString();
                if (string.startsWith("{")) {
                    return JSON.parseObject(string);
                }
                else if (string.startsWith("[")) {
                    return JSON.parseArray(string);
                }
                else {
                    return null;
                }
            }
        }
        else {
            return value;
        }

    }

    public static JSONObject copyJSONObject(JSONObject json) {
        return (JSONObject) json.clone();
    }

    public static JSONArray copyJSONArray(JSONArray json) {
        return (JSONArray) json.clone();
    }

}
