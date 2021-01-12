package fun.utils.api.core.common.po;

public class FilterPO {

    public String id; //
    public String parentType; //上级类型 application,interface
    public String parentId; //上级ID

    public int sort ;// 排序
    public String position ; //过滤位置 enter,execute,return

    public String implementType; //实现类型 sql/url/groovy/bean/javascript/native
    public String implementCode; //实现代码

}
