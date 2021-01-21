package fun.utils.api.core.runtime;

import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.persistence.ParameterDO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class RunApplication {

    private String name;
    private ApplicationDO application;

    private List<DocumentDO> documents;
    private List<FilterDO> filters;
    private List<ParameterDO> parameters;

}
