package fun.utils.api.core.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

public class BeanService {
    @Autowired
    private WebApplicationContext webApplicationContext;
    public Object get(String name){
        return webApplicationContext.getBean(name);
    }
}
