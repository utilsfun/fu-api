package fun.utils.api.core.common.groovy;

import lombok.Data;

import java.util.List;

@Data
public class ScriptParameter {

    public String title; //标题,仅用于接口文档

    public String note; // 参数说明,仅用于接口文档

    public boolean required = false; //	是否为必传参数,false:非必传参数; true:必传参数

    public String defaultValue = null; // 默认值；

    public String dataType = "String"; // 参数的数据类型，string,int,long,double,boolean,date,json

    public boolean array = false; //是否为数组

}
