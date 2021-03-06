package fun.utils.api.core.persistence;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.ErrorCode;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description:接口方法表
 */

@Data
public class InterfaceDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【应用Id】
     */
    private long applicationId;

    /**
     * 【应用Name】
     */
    private String applicationName;

    /**
     * 【名称】 应用 + 名称 唯一
     */
    private String name;

    /**
     * 【分组名称】
     */
    private String groupName;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【排序】
     */
    private int sort;

    /**
     * 【方法】get/put/post/delete
     */
    private String method;

    /**
     * 【调用示例】支持mock变量
     */
    private String requestExample;

    /**
     * 【返回示例】支持mock变量
     */
    private String responseExample;

    /**
     * 【实现类型】groovy/bean,...
     */
    private String implementType;

    /**
     * 【实现代码】
     */
    private String implementCode;

    /**
     * 【配置】 json
     */
    private JSONObject config;

    /**
     * 【错误码】
     */
    private List<ErrorCode> errorCodes;

    /**
     * 【版本号】
     */
    private String version;

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


    private List<Long> documentIds;

    public List<Long> getDocumentIds(){
        if (documentIds == null){
            documentIds = new ArrayList<>();
        }
        return documentIds;
    }


    private List<Long> parameterIds;

    public List<Long> getParameterIds(){
        if (parameterIds == null){
            parameterIds = new ArrayList<>();
        }
        return parameterIds;
    }

    private List<Long> filterIds ;
    public List<Long> getFilterIds(){
        if (filterIds == null){
            filterIds = new ArrayList<>();
        }
        return filterIds;
    }

}
