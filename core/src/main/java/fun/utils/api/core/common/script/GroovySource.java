package fun.utils.api.core.common.script;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode
public class GroovySource {

    private String id;

    private String title; //标题,仅用于接口文档

    private String note; //说明,仅用于接口文档

    private List<String> imports = new ArrayList<>(); //导入列表

    private List<String> sourceIds =  new ArrayList<>(); //依赖方法列表

    private String source; //实现代码

}
