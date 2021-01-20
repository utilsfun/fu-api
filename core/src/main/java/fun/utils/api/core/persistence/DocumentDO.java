package fun.utils.api.core.persistence;

import java.util.Date;

/**
 * Description:【接口文档表】
 */
public class DocumentDO {

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
     * 【说明】
     */
    private String note;

    /**
     * 【内容格式】text,java,js,markdown,html,json,xml,...
     */
    private String format;

    /**
     * 【内容体】
     */
    private String content;

    /**
     * 【权限】公开（public），保护（protected），私有（private）
     */
    private String permission;

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
     * setter for column 【说明】
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * getter for column 【说明】
     */
    public String getNote() {
        return this.note;
    }

    /**
     * setter for column 【内容格式】text,java,js,markdown,html,json,xml,...
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * getter for column 【内容格式】text,java,js,markdown,html,json,xml,...
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * setter for column 【内容体】
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * getter for column 【内容体】
     */
    public String getContent() {
        return this.content;
    }

    /**
     * setter for column 【权限】公开（public），保护（protected），私有（private）
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * getter for column 【权限】公开（public），保护（protected），私有（private）
     */
    public String getPermission() {
        return this.permission;
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