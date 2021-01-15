package fun.utils.api.core.common;

import fun.utils.api.core.common.po.ApplicationPO;
import fun.utils.api.core.common.po.DocumentPO;
import fun.utils.api.core.common.po.FilterPO;
import fun.utils.api.core.common.po.ParameterPO;

import java.util.List;

public class Application {

    public ApplicationPO applicationPO; //应用定义

    public List<ParameterPO> parameters; //公共参数
    public List<DocumentPO> documents; //文档
    public List<FilterPO> filters; //过滤器


}
