package fun.utils.apijson;

import apijson.Log;
import apijson.RequestMethod;
import apijson.framework.APIJSONCreator;
import apijson.framework.APIJSONSQLConfig;
import apijson.framework.APIJSONSQLExecutor;
import apijson.orm.SQLConfig;
import apijson.orm.SQLExecutor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.DataUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApiJsonCaller {

    private final APIJSONCreator creator ;

    private final String dbName;
    private final String dbVersion;
    private final String dbSchema;

    public ApiJsonCaller(DataSource dataSource) throws SQLException {

        try (Connection connection = dataSource.getConnection()){
            this.dbName = connection.getMetaData().getDatabaseProductName().toUpperCase();
            this.dbVersion = connection.getMetaData().getDatabaseProductVersion();
            this.dbSchema = connection.getCatalog();
        }

        this.creator = new APIJSONCreator() {

            @Override
            public SQLConfig createSQLConfig() {

                APIJSONSQLConfig sqlConfig = new APIJSONSQLConfig(){
                    @Override
                    public String getSQLDatabase() {
                       return dbName;
                    }
                    @Override
                    public String getSQLSchema() {
                        return dbSchema;
                    }
                    @Override
                    public String getDBVersion() {
                        return dbVersion;
                    }

                };

                return sqlConfig;
            }

            public SQLExecutor createSQLExecutor() {

                return new APIJSONSQLExecutor(){

                    //  可重写以下方法，支持 Redis 等单机全局缓存或分布式缓存
                    //	@Override
                    //	public List<JSONObject> getCache(String sql, int type) {
                    //		return super.getCache(sql, type);
                    //	}
                    //	@Override
                    //	public synchronized void putCache(String sql, List<JSONObject> list, int type) {
                    //		super.putCache(sql, list, type);
                    //	}
                    //	@Override
                    //	public synchronized void removeCache(String sql, int type) {
                    //		super.removeCache(sql, type);
                    //	}

                    // 适配连接池，如果这里能拿到连接池的有效 Connection，则 SQLConfig 不需要配置 dbVersion, dbUri, dbAccount, dbPassword
                    @Override
                    public Connection getConnection(SQLConfig config) throws Exception {
                        Connection connection = connectionMap.get(config.getDatabase());
                        if (connection == null || connection.isClosed()) {
                            try {
                                // 另一种方式是 DruidConfig 初始化获取到 Datasource 后给静态变量 DATA_SOURCE 赋值： ds = DruidConfig.DATA_SOURCE.getConnection();
                                connection = dataSource.getConnection();
                                connectionMap.put(config.getDatabase(),connection);
                            } catch (Exception e) {
                                Log.e(TAG, "SQLExecutor getConnection " + e.getMessage());
                            }
                        }
                        // 必须最后执行 super 方法，因为里面还有事务相关处理。
                        // 如果这里是 return c，则会导致 增删改 多个对象时只有第一个会 commit，即只有第一个对象成功插入数据库表
                        return super.getConnection(config);
                    }
                };
            }
        };
    }

    public JSONObject get(JSONObject request){
        return new ApiJsonParser(RequestMethod.GET,false,creator).parseResponse(request);
    }

    public JSONObject post(JSONObject request){
        return new ApiJsonParser(RequestMethod.POST,false,creator).parseResponse(request);
    }

    public JSONObject delete(JSONObject request){
        return new ApiJsonParser(RequestMethod.DELETE,false,creator).parseResponse(request);
    }

    public JSONObject put(JSONObject request){
        return new ApiJsonParser(RequestMethod.PUT,false,creator).parseResponse(request);
    }


    public JSONObject getTree(JSONObject request){

        JSONObject result = get(request);

        JSONObject queryObject = request.getJSONObject("[]");
        for (String key: queryObject.keySet()){
            Object item = queryObject.get(key);
            if (item instanceof JSONObject){
                JSONObject  childrenConfig =  ((JSONObject)item).getJSONObject("@children");
                if (childrenConfig != null){
                    JSONArray rows = (JSONArray)JSONPath.eval(result,"\\[\\]." + key);
                    if (rows != null) {
                        rows.forEach(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                JSONObject data = (JSONObject) o;

                                JSONObject tempItem = DataUtils.copyJSONObject((JSONObject) item);
                                JSONObject tempConfig = DataUtils.copyJSONObject(childrenConfig);

                                String childrenName = tempConfig.getString("@column");
                                tempConfig.remove("@column");
                                tempItem.remove("@children");

                                tempItem.putAll(tempConfig);

                                List<String> tempKeys = new ArrayList<>();
                                tempKeys.addAll(tempItem.keySet());

                                for (String tempKey : tempKeys) {
                                    if (tempKey.endsWith("@")) {
                                        tempItem.put(tempKey.substring(0, tempKey.length() - 1), JSONPath.eval(data, tempItem.getString(tempKey)));
                                        tempItem.remove(tempKey);
                                    }
                                }

                                JSONObject tempGet = new JSONObject();
                                JSONObject key1Object = new JSONObject();
                                tempGet.put(key + "[]", key1Object);
                                key1Object.put(key, tempItem);

                                JSONObject tempCallerResult = get(tempGet);

                                JSONArray tempRows = tempCallerResult.getJSONArray(key + "[]");

                                if (tempRows != null) {
                                    JSONPath.set(data, childrenName, tempRows);
                                    for (Object tmpO : tempRows) {
                                        accept(tmpO);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }

        return result;
    }

}
