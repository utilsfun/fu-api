package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Validation {

    //类型
    public String type;

    //验证参数
    public JSONObject data;

    //验证错误提示支持data中取值
    public String message;

}
