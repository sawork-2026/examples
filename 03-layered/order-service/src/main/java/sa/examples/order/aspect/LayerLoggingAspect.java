package sa.examples.order.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 跨层日志切面 — 演示 AOP 如何解决跨层关注点。
 *
 * 无需在每一层手动写日志代码，一个切面统一拦截三层的方法调用，
 * 打印方法名、参数、返回值和耗时。
 */
@Aspect
@Component
public class LayerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LayerLoggingAspect.class);

    // 分别定义三层的切入点
    @Pointcut("within(sa.examples.order.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(sa.examples.order.service..*)")
    public void serviceLayer() {}

    @Pointcut("within(sa.examples.order.repository..*)")
    public void repositoryLayer() {}

    // 统一拦截三层所有 public 方法
    @Around("controllerLayer() || serviceLayer() || repositoryLayer()")
    public Object logAround(ProceedingJoinPoint jp) throws Throwable {
        String layer = detectLayer(jp.getTarget().getClass().getPackageName());
        String method = jp.getSignature().toShortString();

        log.info("[{}] --> {}", layer, method);
        long start = System.currentTimeMillis();

        Object result = jp.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("[{}] <-- {} ({}ms)", layer, method, elapsed);

        return result;
    }

    private String detectLayer(String pkg) {
        if (pkg.contains("controller")) return "Controller";
        if (pkg.contains("service"))    return "Service";
        if (pkg.contains("repository")) return "Repository";
        return "Unknown";
    }
}
