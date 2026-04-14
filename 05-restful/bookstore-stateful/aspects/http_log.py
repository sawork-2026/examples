"""HTTP 请求日志切面"""
import time
from framework.aop import aspect


@aspect(layer="controller", point="around")
def request_log(method, *args, **kwargs):
    # 从参数中找 Request 对象
    request = next((a for a in args if hasattr(a, '_method')), None)
    path = request._path if request else '?'
    http_method = request._method if request else '?'
    t0 = time.time()
    try:
        result = method(*args, **kwargs)
        elapsed = (time.time() - t0) * 1000
        status = result.render()[0] if result else '?'
        print(f"[HTTP] {http_method} {path} -> {status}  ({elapsed:.1f}ms)")
        return result
    except Exception as e:
        elapsed = (time.time() - t0) * 1000
        print(f"[HTTP] {http_method} {path} -> 500  ({elapsed:.1f}ms) ERROR: {e}")
        raise
