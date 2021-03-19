package fun.utils.common.apijson;

import apijson.RequestMethod;
import apijson.framework.APIJSONCreator;
import apijson.framework.APIJSONFunctionParser;
import apijson.framework.APIJSONObjectParser;
import apijson.framework.APIJSONVerifier;
import apijson.orm.*;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static apijson.framework.APIJSONConstant.*;

public class ApiJsonParser extends AbstractParser<Long> {

    public static final String TAG = "MyParser";


    private APIJSONCreator apijsonCreator;

    private ApiJsonParser() {
        super();
    }
    private ApiJsonParser(RequestMethod method) {
        super(method);
    }
    private ApiJsonParser(RequestMethod method, boolean needVerify) {
        super(method, needVerify);
    }


    private HttpSession session;

    public ApiJsonParser(RequestMethod method, boolean needVerify, APIJSONCreator apijsonCreator) {
        super(method, needVerify);
        this.apijsonCreator = apijsonCreator;
    }

    public HttpSession getSession() {
        return session;
    }

    public ApiJsonParser setSession(HttpSession session) {
        this.session = session;
        setVisitor(APIJSONVerifier.getVisitor(session));
        return this;
    }

    @Override
    public Parser<Long> createParser() {
        return apijsonCreator.createParser();
    }

    @Override
    public FunctionParser createFunctionParser() {
        return apijsonCreator.createFunctionParser();
    }

    @Override
    public SQLConfig createSQLConfig() {
        return apijsonCreator.createSQLConfig();
    }

    @Override
    public Verifier<Long> createVerifier() {
        return apijsonCreator.createVerifier();
    }

    @Override
    public SQLExecutor createSQLExecutor() {
        return apijsonCreator.createSQLExecutor();
    }


    @Override
    public JSONObject parseResponse(JSONObject request) {
        //补充format
        if (session != null && request != null) {
            if (request.get(FORMAT) == null) {
                request.put(FORMAT, session.getAttribute(FORMAT));
            }
            if (request.get(DEFAULTS) == null) {
                JSONObject defaults = (JSONObject) session.getAttribute(DEFAULTS);
                Set<Map.Entry<String, Object>> set = defaults == null ? null : defaults.entrySet();

                if (set != null) {
                    for (Map.Entry<String, Object> e : set) {
                        if (e != null && request.get(e.getKey()) == null) {
                            request.put(e.getKey(), e.getValue());
                        }
                    }
                }
            }
        }
        return super.parseResponse(request);
    }

    private FunctionParser functionParser;

    public FunctionParser getFunctionParser() {
        return functionParser;
    }
    @Override
    public Object onFunctionParse(String key, String function, String parentPath, String currentName, JSONObject currentObject) throws Exception {
        if (functionParser == null) {
            functionParser = createFunctionParser();
            functionParser.setMethod(getMethod());
            functionParser.setTag(getTag());
            functionParser.setVersion(getVersion());
            functionParser.setRequest(requestObject);

            if (functionParser instanceof APIJSONFunctionParser) {
                ((APIJSONFunctionParser) functionParser).setSession(getSession());
            }
        }
        functionParser.setKey(key);
        functionParser.setParentPath(parentPath);
        functionParser.setCurrentName(currentName);
        functionParser.setCurrentObject(currentObject);

        return functionParser.invoke(function, currentObject);
    }


    @Override
    public APIJSONObjectParser createObjectParser(JSONObject request, String parentPath, SQLConfig arrayConfig, boolean isSubquery, boolean isTable, boolean isArrayMainTable) throws Exception {

        AbstractSQLConfig.Callback callback = new AbstractSQLConfig.SimpleCallback() {

            @Override
            public SQLConfig getSQLConfig(RequestMethod method, String database, String schema, String table) {
                SQLConfig config = apijsonCreator.createSQLConfig();
                config.setMethod(method);
                config.setTable(table);
                return config;
            }
        };

        return new APIJSONObjectParser(getSession(), request, parentPath, arrayConfig, isSubquery,isTable,isArrayMainTable){
            @Override
            public SQLConfig newSQLConfig(RequestMethod method, String table, String alias, JSONObject request, List<Join> joinList, boolean isProcedure) throws Exception {
                return AbstractSQLConfig.newSQLConfig(method, table, alias, request, joinList, isProcedure,callback) ;
            }
        }.setMethod(getMethod()).setParser(this);

    }



    @Override
    public void onVerifyContent() throws Exception {
        //补充全局缺省版本号  //可能在默认为1的前提下这个请求version就需要为0  requestObject.getIntValue(VERSION) <= 0) {
        HttpSession session = getSession();
        if (session != null && requestObject.get(VERSION) == null) {
            requestObject.put(VERSION, session.getAttribute(VERSION));
        }
        super.onVerifyContent();
    }

}
