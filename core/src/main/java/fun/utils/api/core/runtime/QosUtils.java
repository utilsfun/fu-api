package fun.utils.api.core.runtime;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.redisson.api.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class QosUtils {

    public static final Cache<String, RateLimiter> localRateLimiterCache = CacheBuilder.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();


    public static final Cache<String, Semaphore> localSemaphoreCache = CacheBuilder.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();

    public static boolean localRateLimit(String key, int maxSpeed, int timeOut) throws ExecutionException {

        RateLimiter localRateLimiter = localRateLimiterCache.get(key, () -> RateLimiter.create(maxSpeed));

        if (timeOut > 0) {
            return localRateLimiter.tryAcquire(timeOut, TimeUnit.SECONDS);
        }
        else if (timeOut == 0) {
            localRateLimiter.acquire();
            return true;
        }
        else {
            return localRateLimiter.tryAcquire();
        }

    }

    public static boolean globalRateLimit(RedissonClient redisson, String key, int maxSpeed, int timeOut) throws ExecutionException {

        RRateLimiter rateLimiter = redisson.getRateLimiter(key);
        if (!rateLimiter.isExists()){
            rateLimiter.setRate(RateType.OVERALL,maxSpeed,1, RateIntervalUnit.SECONDS);
            rateLimiter.expire(120, TimeUnit.SECONDS);
        }

        if (timeOut > 0) {
            return rateLimiter.tryAcquire(timeOut, TimeUnit.SECONDS);
        }
        else if (timeOut == 0) {
            rateLimiter.acquire();
            return true;
        }
        else {
            return rateLimiter.tryAcquire();
        }

    }

    public static Semaphore localThreadLimit(String key, int maxThreads, int timeOut) throws ExecutionException, InterruptedException {

        Semaphore semaphore = localSemaphoreCache.get(key, () -> new Semaphore(maxThreads));

        if (timeOut > 0) {

           boolean isReady =  semaphore.tryAcquire(timeOut, TimeUnit.SECONDS);
            return isReady ? semaphore : null;

        }
        else if (timeOut == 0) {
             semaphore.acquire();
             return  semaphore;

        }
        else {
            boolean isReady =  semaphore.tryAcquire();
            return isReady ? semaphore : null;
        }

    }

    public static RSemaphore globalThreadLimit(RedissonClient redisson, String key, int maxSpeed, int timeOut) throws ExecutionException, InterruptedException {

        RSemaphore semaphore = redisson.getSemaphore(key);
        if (!semaphore.isExists()){
            semaphore.trySetPermits(maxSpeed);
            semaphore.expire(120, TimeUnit.SECONDS);
        }

        if (timeOut > 0) {

            boolean isReady =  semaphore.tryAcquire(timeOut, TimeUnit.SECONDS);
            return isReady ? semaphore : null;

        }
        else if (timeOut == 0) {
            semaphore.acquire();
            return  semaphore;

        }
        else {
            boolean isReady =  semaphore.tryAcquire();
            return isReady ? semaphore : null;
        }

    }

}
