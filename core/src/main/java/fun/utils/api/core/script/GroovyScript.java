package fun.utils.api.core.script;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode
public class GroovyScript extends GroovySource {

    private Map<String, GroovyVariable> declaredVariables = new HashMap<>();

    private JSONObject config; //配置

    private String returnType = "Object"; //返回类型 (java class 定义) 如: void,String,int,...,com.alibaba.fastjson.JSONObject

    private String version = "0";//版本

}
