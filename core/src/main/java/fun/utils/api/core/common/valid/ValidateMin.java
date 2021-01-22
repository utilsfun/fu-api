package fun.utils.api.core.common.valid;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.runtime.Validation;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class ValidateMin extends Validation {

    public ValidateMin(BigDecimal min, String message) {
        this.type = "@Min";
        this.data = new JSONObject();
        this.data.put("min", min);
        this.message = StringUtils.defaultIfBlank(message, "${title}值应不小于${min}");
    }
}
