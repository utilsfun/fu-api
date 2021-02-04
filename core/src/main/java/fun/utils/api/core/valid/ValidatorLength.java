package fun.utils.api.core.valid;

import fun.utils.api.core.common.ClassUtils;
import org.apache.commons.validator.GenericValidator;

// @Length,parameters:{min:5, max:10},message:"密码长度应在6到12之间"
public class ValidatorLength extends AbstractValidator {

    public ValidatorLength() {
        this.message = "值长度应在${min|default:'0'}到${max|default:'更大'}之间";
    }

    @Override
    public boolean isValid(Object value) throws Exception {
        int min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Integer.class) : 0;
        int max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Integer.class) : Integer.MAX_VALUE;
        return GenericValidator.isInRange(String.valueOf(value).length(), min, max);

    }

}
