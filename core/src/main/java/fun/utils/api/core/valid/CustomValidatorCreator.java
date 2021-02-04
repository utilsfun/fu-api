package fun.utils.api.core.valid;

public class CustomValidatorCreator implements ValidatorCreator{
    @Override
    public AbstractValidator create(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //自定义Class验证器 如: @:com.my.test.DemoValidator
        String className = name.replaceFirst("@:", "");
        Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        return  (AbstractValidator) validatorClass.newInstance();
    }
}
