package fun.utils.api.core.script;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import groovy.lang.GroovyShell;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class GroovyService {

    private final Cache<String, GroovyRunner> cacheRunners = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(2, TimeUnit.MINUTES).build();
    private final Cache<String, GroovySource> cacheSources = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(2, TimeUnit.MINUTES).build();

    @Getter
    private GroovyShell shell = new GroovyShell();

    @Getter
    @Setter
    private Callback<String, GroovyScript> onGetRunnerById;

    @Getter
    @Setter
    private Callback<String, GroovySource> onGetSourceById = new GroovySrcDefaultLoader();

    public GroovyRunner getRunner(String id) throws Exception {
        GroovyScript groovyScript = onGetRunnerById.call(id);
        return getRunner(groovyScript);
    }


    public GroovyRunner getRunner(GroovyScript method) throws Exception {
        GroovyRunner runner = cacheRunners.get(method.getId(), () -> new GroovyRunner(this, method));
        if (runner.getVersion().equals(method.getVersion())) {
            return runner;
        } else {
            return reloadRunner(method);
        }
    }

    public GroovyRunner reloadRunner(GroovyScript groovyScript) throws Exception {
        cacheRunners.invalidate(groovyScript.getId());
        GroovyRunner runner = cacheRunners.get(groovyScript.getId(), () -> new GroovyRunner(this, groovyScript));
        return runner;
    }

    public void expireRunner(String id) throws Exception {
        cacheRunners.invalidate(id);
    }

    public void expireRunner(GroovyScript method) throws Exception {
        cacheRunners.invalidate(method.getId());
    }


    public GroovySource getSource(String id) throws Exception {
        GroovySource groovySource = cacheSources.get(id, () -> onGetSourceById.call(id));
        return groovySource;
    }


    public void expireSource(String id) throws Exception {
        cacheSources.invalidate(id);
    }

}
