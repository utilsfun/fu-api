package fun.utils.api.core.common.po;

import fun.utils.api.core.vo.Validation;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class ParameterPO {

    public String id; //参数ID

    public String parentType; //上级类型 application,interface,parameter
    public String parentId; //上级ID

    @NonNull
    public String name; //名称

    public List<String> alias; //别称,多个可用参数值将映射到第一个区配的值如 参数名称id,别称uid,gid,uuid传入id=&uid=&gid=100&uuid=200,最后参数id取值为100

    public String title; //标题,仅用于接口文档

    public String note; // 参数说明,仅用于接口文档

    public int sort ;// 排序 ,仅用于接口文档

    public String position ; //取值位置  query header cookie form body 默认"body" , 为子参数不填,时同上级

    public String defaultValue; // 默认值；

    public String dataType; // 参数的数据类型，string,int,long,double,boolean,date,object

    public boolean array = false; //是否为数组

    public boolean required = false; //	是否为必传参数,false:非必传参数; true:必传参数

    public boolean hidden = false; // 是否隐藏，隐藏时文档中不显示参数,但可以传入 false:不隐藏; true:隐藏
    public boolean readOnly = false; //	是否只读,只读时不能传入参数,只取默认值，false:不只读; true:只读

    public List<String> examples ;	//示例值 支持mock变量
    public List<Validation> validations; //参数验证


}
