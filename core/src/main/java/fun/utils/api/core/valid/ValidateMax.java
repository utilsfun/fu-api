package fun.utils.api.core.valid;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.runtime.Validation;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class ValidateMax extends Validation {

    public ValidateMax(BigDecimal max, String message) {
        this.type = "@Max";
        this.data = new JSONObject();
        this.data.put("max", max);
        this.message = StringUtils.defaultIfBlank(message, "${title}值应不大于${max}");
    }
}
