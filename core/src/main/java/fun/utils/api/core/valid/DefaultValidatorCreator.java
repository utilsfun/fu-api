package fun.utils.api.core.valid;

public class DefaultValidatorCreator implements ValidatorCreator{
    @Override
    public AbstractValidator create(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //内置验证器 如: @Max
        String className = name.replaceFirst("@", "fun.utils.api.core.valid.Validator");
        Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        return  (AbstractValidator) validatorClass.newInstance();
    }
}
