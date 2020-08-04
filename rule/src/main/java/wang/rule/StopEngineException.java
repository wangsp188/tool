package wang.rule;

/**
 * @Description 停止引擎异常,标识engin需要停止了
 * @Author wangshaopeng
 * @Date 2020-08-04
 */
public class StopEngineException extends Exception {
    public StopEngineException() {
    }

    public StopEngineException(String message) {
        super(message);
    }
}
