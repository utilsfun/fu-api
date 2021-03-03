package fun.utils.api.core.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RKeys;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

public class DataUtils {

    public static <T> List<T> getEmptyIfNull(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        else {
            return list;
        }
    }

    public static <K, V> Map<K, V> getEmptyIfNull(Map<K, V> map) {
        if (map == null) {
            return new HashedMap();
        }
        else {
            return map;
        }
    }

    //{}
    public static String stringFormat(String src, Map<String, Object> parameters) {

        //Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\s*(\\|[^\\{\\}]+)?\\}");
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\s*(\\|[^{}]+)?}");
        Matcher matcher = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = parameters.get(key);
            String exString = matcher.group(2);
            if (exString != null) {
                String[] expressions = split(removeStart(exString.trim(), "|"), "|");
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

        if (isBlank(expression)) {
            return value;
        }

        expression = expression.trim();

        if (expression.matches("default:.+")) {
            if (value == null) {
                String string = removeStartIgnoreCase(expression, "default:");
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
        return copyJSON(json);
    }

    public static JSONArray copyJSONArray(JSONArray json) {
        return copyJSON(json);
    }

    private static <T> T copyJSON(Object json) {
        return (T) JSON.parse(toWebJSONString(json));
    }

    public static boolean isJSONObject(Object object) {

        if (object instanceof JSONObject) {
            return true;
        }

        if (object == null) {
            return false;
        }

        return isBesieged(object.toString(),"{","}");

    }

    public static boolean isJSONArray(Object object) {

        if (object instanceof JSONObject) {
            return true;
        }

        if (object == null) {
            return false;
        }

        return isBesieged(object.toString(),"[","]");

    }


    public static boolean isBesieged(String source,String left,String right) {
        if (source == null) {
            return false;
        }

        String s = source.trim();
        return s.startsWith(left) && s.endsWith(right);
    }


    public static String extractBesieged(String source,String left,String right) {
        String substring = substringAfter(source,left);
        String result = substringBeforeLast(substring, right);
        return result;
    }


    public static String extractStr(String source, String regex, int group){
        String result = null;
        Matcher matcher =  Pattern.compile(regex).matcher(source);
        if (matcher.find()){
            result = matcher.group(group);
        }
        return result;
    }

    public static String extractStr(String source, String regex){
        return extractStr(source,regex,0);
    }

    public static List<String> extractList(String source, String regex, int group){
        List<String>  result = new ArrayList<>();
        Matcher matcher =  Pattern.compile(regex).matcher(source);
        while (matcher.find()){
            String value = matcher.group(group);
            if (value != null){
                result.add(value);
            }
        }
        return result;
    }

    public static List<String> extractList(String source, String regex){
        return extractList(source,regex,0);
    }

    public static List<String> extractGroups(String source, String regex){
        List<String>  result = new ArrayList<>();
        Matcher matcher =  Pattern.compile(regex).matcher(source);
        if (matcher.find()){
            for (int i = 0; i < matcher.groupCount() ; i++) {
                result.add(matcher.group(i));
            }
        }
        return result;
    }

    public static List<List<String>> extractGroupsList(String source, String regex){
        List<List<String>> result = new ArrayList<>();
        Matcher matcher =  Pattern.compile(regex).matcher(source);
        while (matcher.find()){
            List<String>  groups = new ArrayList<>();
            for (int i = 0; i < matcher.groupCount() ; i++) {
                groups.add(matcher.group(i));
            }
            result.add(groups);
        }
        return result;
    }

    //把value对象,填充数据并返回
    //为value为String并为 @{JsonPath}格时返回JsonPath对应data中的值
    public static JSONObject fullRefJSON(JSONObject value, JSONObject data){
        return fullRefObject(value,data);
    }

    private static <T> T fullRefObject(T value, JSONObject data){

            JSONObject p = copyJSONObject(data);

            if (value instanceof String && isBesieged ((String) value,"@{","}")){
                String path = extractBesieged((String) value,"@{","}");
                return (T) JSONPath.eval(p,path);
            }
            else if (value instanceof JSONObject){
                JSONObject target = new JSONObject();
                JSONObject objectValue = (JSONObject)value;
                objectValue.keySet().forEach(k-> {
                    Object v = objectValue.get(k);
                    Object v1 = fullRefObject(v, p);
                    String k1 = ClassUtils.castValue(fullRefObject(k, p),String.class);
                    target.put(k1,v1);
                });
                return (T) target;
            }
            else if (value instanceof JSONArray){
                JSONArray target = new JSONArray();
                JSONArray arrayValue = (JSONArray)value;
                arrayValue.forEach(v->{
                    target.add(fullRefObject(v,p));
                });
                return (T) target;
            }
            else{
                return value;
            }

    }


    public static JSONObject mergeJson(JSONObject fromObj, JSONObject toObj) {
        return mergeJson(fromObj, toObj, false,true);
    }

    //覆盖所有 fromObj中的值到 toObj中
    //arrayFill:遇到数组时,是否合并,否则替换数组
    //mustExist:是否只覆盖所有toObj中存在的值
    public static JSONObject mergeJson( JSONObject fromObj, JSONObject toObj, boolean mustExist, boolean arrayFill){

        JSONObject parameters = copyJSONObject(fromObj);
        JSONObject target = copyJSONObject(toObj);

        parameters.keySet().forEach(k-> {

            Object fromValue = parameters.get(k);

            if (target.containsKey(k)){
                //目标对象中有对应值
                Object toValue = target.get(k);

                if (toValue instanceof JSONObject) {
                    //当目标值类型为JSONObject
                    JSONObject toObjValue = (JSONObject) toValue;

                    if (fromValue instanceof JSONObject) {
                        //源数据值也为JSONObject时
                        JSONObject fromObjValue = (JSONObject)fromValue;
                        target.put(k,mergeJson(fromObjValue,toObjValue,mustExist,arrayFill));
                    }else{
                        //源数据值不为JSONObject时
                        target.put(k,fromValue);
                    }
                }
                else if (toValue instanceof JSONArray) {
                    //当目标值类型为JSONArray
                    JSONArray toObjValue = (JSONArray) toValue;

                    if (fromValue instanceof JSONArray) {
                        //源数据值也为JSONArray时
                        JSONArray fromObjValue =(JSONArray)fromValue;
                        if (arrayFill){
                            //补充
                            fromObjValue.removeAll(toObjValue);
                            toObjValue.addAll(fromObjValue);
                        }else{
                            //更新 覆盖
                            target.put(k,fromValue);
                        }
                    }
                    else{
                        //源数据值不为JSONArray时
                        if (arrayFill){
                            //补充
                            if (!toObjValue.contains(fromValue)){
                                toObjValue.add(fromValue);
                            }
                        }else{
                            //更新 覆盖
                            target.put(k,fromValue);
                        }
                    }
                }
                else{
                    //当目标值类型为基础类型
                    target.put(k,fromValue);
                }
            }
            else{
                //目标对象中没有对应值
                if (!mustExist){
                    //如果不是更新改值,就加入toObj
                    target.put(k,fromValue);
                }
            }
        });

        return target;

    }


    public static String toWebJSONString(Object ret) {
        return JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat,SerializerFeature.BrowserCompatible);
    }

    public static <T> T jsonValueByPath(JSONObject root,String... path){
        for (String s:path){
           T result = (T) JSONPath.eval(root,s);
           if (result != null){
               return result;
           }
        }
        return null;
    }
}
