package fun.utils.api.core.valid;

import fun.utils.api.core.common.Validation;
import org.apache.commons.lang3.StringUtils;

public class ValidateEmail extends Validation {

    public ValidateEmail(String message) {
        this.type = "@Email";
        this.message = StringUtils.defaultIfBlank(message, "${title}值应为有效的Email地址");
    }
}
