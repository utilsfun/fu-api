package fun.utils.api.core.common;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NonNull;

@Data
public class ApiParameter {

    @NonNull
    public String name; //名称

    public String title; //标题

    public String note; // 参数说明

    public String valuePath; // 取值位置

    public Object defaultValue; // 默认值；

    public Object value; // 参数值；

    public String dataType; // 参数的数据类型，string,int,long,double,boolean,date,object,array

    public boolean required = false; //	是否为必传参数,false:非必传参数; true:必传参数

    public boolean hidden = false; // 是否隐藏，隐藏时文档中不显示参数,但可以传入 false:不隐藏; true:隐藏

    public boolean readOnly = false; //	是否只读,只读时不能传入参数,只取默认值，false:不只读; true:只读

    public String example ;	//属性的示例值

    public JSONObject validation; //参数验证
    {

        validation.get
    }

}
