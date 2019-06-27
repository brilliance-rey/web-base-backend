package cc.mrbird.febs.common.exception;

/**
 *  系统内部异常
 * Date:   2019年6月26日 下午12:56:34 <br/>
 * @author RenEryan
 */
public class SysInnerException extends Exception {

    private static final long serialVersionUID = -994962710559017255L;

    public SysInnerException(String message) {
        super(message);
    }
}
