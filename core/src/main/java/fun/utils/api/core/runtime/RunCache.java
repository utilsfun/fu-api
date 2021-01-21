package fun.utils.api.core.runtime;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.script.GroovyRunner;

import java.util.concurrent.TimeUnit;

public class RunCache {
    public final static Cache<String, RunApplication> runApplications = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(2, TimeUnit.MINUTES).build();
    public final static Cache<String, RunInterface> runInterfaces = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(2, TimeUnit.MINUTES).build();

}
