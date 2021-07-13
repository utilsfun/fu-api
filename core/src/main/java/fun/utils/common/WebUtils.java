package fun.utils.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.DataUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;


public class WebUtils {


    public static JSONObject getJsonByInput(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        String method = request.getMethod().toLowerCase();
        result.put("method", method);

        /* ******* headers *******  */
        JSONObject headers = new JSONObject();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            List<String> valueList = new ArrayList<>();
            while (headerValues.hasMoreElements()) {
                valueList.add(headerValues.nextElement());
            }
            if (valueList.size() == 1) {
                headers.put(headerName, valueList.get(0));
            }
            else {
                headers.put(headerName, valueList);
            }
        }
        result.put("headers", headers);


        /* ******* request *******  */
        JSONObject requestNode = getRequestInfo(request);

        result.put("request", requestNode);


        /* ******* cookies *******  */
        JSONObject cookiesNode = new JSONObject();

        Cookie[] cookieList = request.getCookies();
        if (cookieList != null) {
            for (Cookie cookie : cookieList) {
                cookiesNode.put(cookie.getName(), cookie.getValue());
            }
        }
        result.put("cookies", cookiesNode);


        /* ******* parameters *******  */

        JSONObject parametersNode = new JSONObject();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                parametersNode.put(paramName, paramValues[0]);
            }
            else {
                parametersNode.put(paramName, Arrays.asList(paramValues));
            }
        }
        result.put("parameters", parametersNode);


        /* ******* body *******  */
        JSONObject bodyNode = new JSONObject();

        JSONObject queryBody = new JSONObject();

        try {
            String src = StringUtils.defaultString(request.getQueryString(), "").trim();
            if (DataUtils.isJSONObject(src)) {
                src = URLDecoder.decode(src, "utf8");
                queryBody = JSON.parseObject(src);
            }
        } catch (Exception e) {
        }

        JSONObject fromBody = new JSONObject();

        if (request.getContentType() != null
                && ContentType.parse(request.getContentType()).getMimeType().matches("application/json|text/json|text/plain")
                && request.getContentLength() > 0) {
            try {
                String src = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(src)) {
                    fromBody = JSON.parseObject(src);
                }
            } catch (Exception e) {
            }
        }
        else {
            fromBody.putAll(getJsonByParameters(request));
        }

        bodyNode.putAll(queryBody);
        bodyNode.putAll(fromBody);

        result.put("body", bodyNode);


        return result;


    }

    public static JSONObject getJsonByParameters(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        //   System.out.println("request:" + JSON.toJSONString(JSON.toJSON(request)));
        Enumeration<String> params = request.getParameterNames();

        while (params.hasMoreElements()) {

            // 参数全名
            String param_full = params.nextElement();
            //System.out.println("param_full:" + param_full);

            String param_value = request.getParameter(param_full);
            String param_next = param_full;

            JSONObject preJo = result;
            JSONArray preArray = null;
            boolean preisJo = true;

            while (StringUtils.isNotBlank(param_next)) {

                // 本次处理的相对参数名称
                String param_now = param_next;

                // 去掉两边[]
                if (param_now.startsWith("[")) {
                    //param_now = param_now.replaceFirst("^\\[", "").replaceFirst("\\]", "");
                    param_now = StringUtils.substringBetween(param_now, "[", "]");
                }

                // 本次处理对象名称
                String a_name = param_now.replaceAll("\\[.*]", "");


                // 下次处理参数名称
                param_next = param_now.substring(a_name.length());

                // System.out.println(param_now + "," + a_name + "," + param_next);
                // 分析对象名字

                if (StringUtils.isBlank(a_name)) {
                    // 数组的值
                    preArray.add(param_value);
                    // 结束了
                    break;

                }

                if (preisJo) {
                    // 上一层是对象-------------------------------

                    Object node = preJo.get(a_name);
                    // 是否已有节点对象

                    if (node instanceof JSONObject) {
                        // 已有对象，是个对象
                        preJo = (JSONObject) node;
                        preisJo = true;
                        continue;

                    }
                    else if (node instanceof JSONArray) {

                        // 已有数组，是个数组
                        preArray = (JSONArray) node;
                        preisJo = false;
                        continue;

                    }
                    else {

                        // 没有要新建
                        if (StringUtils.isBlank(param_next)) {

                            preJo.put(a_name, param_value);
                            // 结束了
                            break;

                        }
                        else if (param_next.startsWith("[]")) {
                            // 数组
                            JSONArray aArray = new JSONArray();
                            preJo.put(a_name, aArray);
                            preArray = aArray;
                            preisJo = false;

                        }
                        else {
                            // 对象
                            JSONObject ajo = new JSONObject();
                            preJo.put(a_name, ajo);
                            preJo = ajo;
                            preisJo = true;
                        }

                    }

                }
                else {
                    // 上层是数组--------------------------------

                    // 是否已有节点对象
                    if (preArray.size() < Integer.parseInt(a_name)) {

                        // 没有要新建
                        if (param_next.startsWith("[]")) {
                            // 数组
                            JSONArray aArray = new JSONArray();
                            preArray.add(aArray);
                            preArray = aArray;
                            preisJo = false;

                        }
                        else {
                            // 对象
                            JSONObject ajo = new JSONObject();
                            preArray.add(ajo);
                            preisJo = true;
                        }

                    }
                    else {

                        // 已有对象
                        Object node = preArray.get(Integer.parseInt(a_name));
                        if (node instanceof JSONArray) {
                            preArray = (JSONArray) node;
                            preisJo = false;

                        }
                        else {

                            if (node instanceof JSONObject) {
                                preJo = (JSONObject) node;
                                preisJo = false;
                            }
                        }

                    }

                }

            } // next param_next

        } // next param_full

        return result;

    }

    public static String getUserIp(HttpServletRequest request) {

        // 用户IP
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;

    }

    public static String getFullURL(HttpServletRequest request) {
        return request.getRequestURL().toString() + (StringUtils.isBlank(request.getQueryString()) ? "" : "?" + request.getQueryString());
    }

    public static JSONObject getRequestInfo(HttpServletRequest request) {
        JSONObject result = new JSONObject();

        result.put("pathInfo", request.getPathInfo());
        result.put("contextPath", request.getContextPath());
        result.put("requestURL", request.getRequestURL().toString());
        result.put("requestURI", request.getRequestURI());
        result.put("fullURL", getFullURL(request));
        result.put("queryString", request.getQueryString());
        result.put("method", request.getMethod());
        result.put("scheme", request.getScheme());
        result.put("protocol", request.getProtocol());
        result.put("sessionId", request.getRequestedSessionId());
        result.put("serverName", request.getServerName());
        result.put("localName", request.getLocalName());
        result.put("localAddr", request.getLocalAddr());
        result.put("remoteAddr", request.getRemoteAddr());
        result.put("remoteHost", request.getRemoteHost());
        result.put("remoteUser", request.getRemoteUser());
        result.put("userIp", getUserIp(request));

        return result;
    }

}
