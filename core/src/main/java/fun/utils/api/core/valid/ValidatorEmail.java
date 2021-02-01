package fun.utils.api.core.valid;

import org.apache.commons.validator.GenericValidator;

// @Email
public class ValidatorEmail extends AbstractValidator {

    public ValidatorEmail() {
        this.message = "应为正确的Email格式";
    }

    @Override
    public boolean isValid(Object value) throws Exception {
        Class<?> vClass = value.getClass();
        if (vClass.isAssignableFrom(String.class)) {
            return GenericValidator.isEmail((String) value);
        }
        else {
            throw new Exception(vClass.getSimpleName() + "数据不适用@Email验证.");
        }
    }

}
