package fun.utils.api.core.common.valid;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.vo.Validation;
import org.apache.commons.lang3.StringUtils;

public class ValidatePattern extends Validation {

    public ValidatePattern(String regex, String message) {
        this.type = "@Pattern";
        this.data = new JSONObject();
        this.data.put("regex",regex);
        this.message = StringUtils.defaultIfBlank(message,"${title}值应符合指定格式");
    }
}
