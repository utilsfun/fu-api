package fun.utils.api.core.valid;

import org.apache.commons.validator.GenericValidator;


//@Url
public class ValidatorUrl extends AbstractValidator {

    public ValidatorUrl() {
        this.message = "应为正确的Url格式";
    }

    @Override
    public boolean isValid(Object value) throws Exception {
        Class<?> vClass = value.getClass();
        if (vClass.isAssignableFrom(String.class)) {
            return GenericValidator.isUrl((String) value);
        }
        else {
            throw new Exception(vClass.getSimpleName() + "数据不适用@Url验证.");
        }
    }

}
