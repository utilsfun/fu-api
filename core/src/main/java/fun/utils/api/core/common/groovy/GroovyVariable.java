package fun.utils.api.core.common.groovy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
public class GroovyVariable {

    private String title; //标题,仅用于接口文档

    private String note; // 参数说明,仅用于接口文档

    private boolean required = false; //	是否为必传参数,false:非必传参数; true:必传参数

    private String defaultValue = null; // 默认值；

    private String dataType = "String"; // 参数的数据类型，string,int,long,double,boolean,date,json

    private boolean array = false; //是否为数组

}
