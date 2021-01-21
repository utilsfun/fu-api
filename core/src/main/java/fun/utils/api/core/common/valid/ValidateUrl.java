package fun.utils.api.core.common.valid;

import org.apache.commons.lang3.StringUtils;

public class ValidateUrl extends Validation {

    public ValidateUrl(String message) {
        this.type = "@Url";
        this.message = StringUtils.defaultIfBlank(message, "${title}值应为有效的URL地址");
    }
}
