package fun.utils.api.core.valid;

import org.apache.commons.validator.GenericValidator;

public class ValidatorPhone extends AbstractValidator {

    public ValidatorPhone() {
        this.message = "应为正确的手机号码格式";
    }

    @Override
    public boolean isValid(Object value) throws Exception {

        String regex = "^\\+?(86)?1(3|4|5|6|7|8|9)\\d{9}$";
        return GenericValidator.matchRegexp(String.valueOf(value), regex);

    }

}
