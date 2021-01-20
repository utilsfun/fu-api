package fun.utils.api.core.persistence;

import java.util.Date;
/**
 * Description:【接口过滤器表】
 */
public class FilterDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【上级类型】application,interface
     */
    private String parentType;

    /**
     * 【上级ID】
     */
    private long parentId;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【配置】 json
     */
    private String config;

    /**
     * 【排序】
     */
    private int sort;

    /**
     * 【实现类型】groovy/bean,...
     */
    private String implementType;

    /**
     * 【实现代码】
     */
    private String implementCode;

    /**
     * 【过滤位置】enter,execute,return
     */
    private String point;

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

    /**
     * setter for column 【编号】自增长编号
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * getter for column 【编号】自增长编号
     */
    public long getId() {
        return this.id;
    }

    /**
     * setter for column 【上级类型】application,interface
     */
    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    /**
     * getter for column 【上级类型】application,interface
     */
    public String getParentType() {
        return this.parentType;
    }

    /**
     * setter for column 【上级ID】
     */
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    /**
     * getter for column 【上级ID】
     */
    public long getParentId() {
        return this.parentId;
    }

    /**
     * setter for column 【标题】
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * getter for column 【标题】
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * setter for column 【配置】 json
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * getter for column 【配置】 json
     */
    public String getConfig() {
        return this.config;
    }

    /**
     * setter for column 【排序】
     */
    public void setSort(int sort) {
        this.sort = sort;
    }

    /**
     * getter for column 【排序】
     */
    public int getSort() {
        return this.sort;
    }

    /**
     * setter for column 【实现类型】groovy/bean,...
     */
    public void setImplementType(String implementType) {
        this.implementType = implementType;
    }

    /**
     * getter for column 【实现类型】groovy/bean,...
     */
    public String getImplementType() {
        return this.implementType;
    }

    /**
     * setter for column 【实现代码】
     */
    public void setImplementCode(String implementCode) {
        this.implementCode = implementCode;
    }

    /**
     * getter for column 【实现代码】
     */
    public String getImplementCode() {
        return this.implementCode;
    }

    /**
     * setter for column 【过滤位置】enter,execute,return
     */
    public void setPoint(String point) {
        this.point = point;
    }

    /**
     * getter for column 【过滤位置】enter,execute,return
     */
    public String getPoint() {
        return this.point;
    }

    /**
     * setter for column 【状态】0正常 1停用
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * getter for column 【状态】0正常 1停用
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * setter for column 【创建时间】
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * getter for column 【创建时间】
     */
    public Date getGmtCreate() {
        return this.gmtCreate;
    }

    /**
     * setter for column 【修改时间】
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * getter for column 【修改时间】
     */
    public Date getGmtModified() {
        return this.gmtModified;
    }
}