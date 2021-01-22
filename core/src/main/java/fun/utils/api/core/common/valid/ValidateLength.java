package fun.utils.api.core.common.valid;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.runtime.Validation;
import org.apache.commons.lang3.StringUtils;

public class ValidateLength extends Validation {

    public ValidateLength(int min, int max, String message) {
        this.type = "@Length";
        this.data = new JSONObject();
        this.data.put("min", min);
        this.data.put("max", max);
        this.message = StringUtils.defaultIfBlank(message, "${title}值长度应在${min}到${max}之间");
    }
}
