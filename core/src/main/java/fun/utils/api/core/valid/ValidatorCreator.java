package fun.utils.api.core.valid;

public interface ValidatorCreator {

   public AbstractValidator create(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException;

}
