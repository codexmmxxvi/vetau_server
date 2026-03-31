package codex.mmxxvi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CachingService {
    private final  StringRedisTemplate stringRedisTemplate;
    public CachingService(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public String get(String value){
        return stringRedisTemplate.opsForValue().get(value);
    }
    public void set(String key, String value, Long timeout, TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key,value,timeout, timeUnit);
    }
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
