package fun.utils.api.demo;

import fun.utils.api.core.valid.AbstractValidator;

public class MyValidator1 extends AbstractValidator {

    public MyValidator1() {
        this.message = "应为朱氏";
    }

    @Override
    public boolean isValid(Object value) throws Exception {
        return value.toString().matches("^朱.+");
    }
}
