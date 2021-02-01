package fun.utils.api.core.persistence;

import fun.utils.api.core.common.ValidConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description:【接口参数表】
 */

@Data
public class ParameterDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【上级类型】application,interface,parameter
     */
    private String parentType;

    /**
     * 【上级ID】
     */
    private long parentId;

    /**
     * 【名称】上级+名称 唯一
     */
    private String name;

    /**
     * 【别称】json array 别称,多个可用参数值将映射到第一个区配的值如 参数名称id,别称uid,gid,uuid传入id=&uid=&gid=100&uuid=200,最后参数id取值为100
     */
    private List<String> alias;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【排序】
     */
    private int sort;

    /**
     * 【取值位置】query header cookie form body 默认"body" , 为子参数不填,时同上级
     */
    private String position;

    /**
     * 【默认值】
     */
    private String defaultValue;

    /**
     * 【数据类型】string,int,long,double,boolean,date,object
     */
    private String dataType;

    /**
     * 【是否数组】0否 1是
     */
    private int isArray;

    /**
     * 【是否必填】0否 1是
     */
    private int isRequired;

    /**
     * 【是否隐藏】0否 1是 隐藏时文档中不显示参数,但可以传入
     */
    private int isHidden;

    /**
     * 【是否只读】0否 1是 只读时不能传入参数,只取默认值
     */
    private int isReadOnly;

    /**
     * 【示例值】json array 支持mock变量
     */
    private List<String> examples;

    /**
     * 【参数验证】json Validation格式
     */
    private List<ValidConfig> validations;

    /**
     * 【状态】0正常 1停用
     */
    private int status;

    /**
     * 【创建时间】
     */
    private Date gmtCreate;

    /**
     * 【修改时间】
     */
    private Date gmtModified;


    private List<Long> parameterIds;

    public List<Long> getParameterIds(){
        if (parameterIds == null){
            parameterIds = new ArrayList<>();
        }
        return parameterIds;
    }

}
