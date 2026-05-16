package BlogBack.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;//返回值code，200为成功，0和其余数字为失败
    private String msg;//报错后返回的信息
    private T data;//返回的数据

    /**
     * 静态方法，成功返回
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.code = 200;
        return result;
    }

    /**
     * 静态方法，成功返回数据
     * @param object
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    /**
     * 静态方法，失败返回信息
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
