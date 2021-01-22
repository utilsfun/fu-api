package fun.utils.api.core.common.valid;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.runtime.Validation;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class ValidateRange extends Validation {

    public ValidateRange(BigDecimal min, BigDecimal max, String message) {
        this.type = "@Range";
        this.data = new JSONObject();
        this.data.put("min", min);
        this.data.put("max", max);
        this.message = StringUtils.defaultIfBlank(message, "${title}值范围应在${min}到${max}之间");
    }
}
