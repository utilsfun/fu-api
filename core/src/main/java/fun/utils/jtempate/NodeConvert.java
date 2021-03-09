package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public interface NodeConvert {
    public Object convert(JSON data, JSONObject node, VarThree varThree) ;
}
