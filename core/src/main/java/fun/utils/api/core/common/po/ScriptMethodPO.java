package fun.utils.api.core.common.po;

import com.alibaba.fastjson.JSONObject;

public class ScriptMethodPO {

    public String id; //ID

    public String name; //名称

    public String title; //标题,仅用于接口文档

    public JSONObject param; //名称

    public String returnType; //返回类型 (java class 定义) 如: void,String,int,...,com.alibaba.fastjson.JSONObject

    public String implementType; //实现类型 sql/url/groovy/bean/javascript

    public String implementCode; //实现代码

}
