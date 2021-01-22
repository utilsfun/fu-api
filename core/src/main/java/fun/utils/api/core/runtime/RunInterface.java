package fun.utils.api.core.runtime;


import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.persistence.ParameterDO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class RunInterface {

    public String name;
    public RunApplication runApplication;

    public InterfaceDO interfaceDO;

    private List<DocumentDO> documents;
    private List<FilterDO> filters;
    private List<ParameterDO> parameters;


}
