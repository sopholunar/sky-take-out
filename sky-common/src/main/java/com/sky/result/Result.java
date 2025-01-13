package com.sky.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * 用于 Web API 或服务的响应结果封装。通过使用泛型 T，该类可以返回不同类型的数据
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败code 用于表示响应的状态码。这个字段可以根据实际需要扩展，如 200 表示成功，400 表示客户端请求错误，500 表示服务器内部错误等。
    private String msg; //错误信息，若请求成功，这个字段可能为空或包含一些说明信息；若请求失败，则可以包含具体的错误描述，如“参数不合法”，“数据库连接失败”等。
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
