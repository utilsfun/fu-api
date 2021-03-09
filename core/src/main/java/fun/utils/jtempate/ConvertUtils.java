package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;

public class ConvertUtils {
    public static <T> T copyJSON(Object json) {
        return (T) JSON.parse(JSON.toJSONString(json));
    }
}
