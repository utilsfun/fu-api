package fun.utils.api.core.common.groovy;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScriptMethod {

    public String title; //标题,仅用于接口文档

    public Map<String,ScriptParameter> parameters = new HashMap<>();

    public String script; //实现代码

    public String returnType; //返回类型 (java class 定义) 如: void,String,int,...,com.alibaba.fastjson.JSONObject

}
