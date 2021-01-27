package fun.utils.api.core.persistence;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.ErrorCode;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description:接口应用表
 */
@Data
public class ApplicationDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【名称】唯一
     */
    private String name;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【作者】
     */
    private String owner;

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


    private List<String> interfaceNames ;
    public List<String> getInterfaceNames(){
        if (interfaceNames == null){
            interfaceNames = new ArrayList<>();
        }
        return interfaceNames;
    }

    private List<Long> sourceIds ;
    public List<Long> getSourceIds(){
        if (sourceIds == null){
            sourceIds = new ArrayList<>();
        }
        return sourceIds;
    }

}
