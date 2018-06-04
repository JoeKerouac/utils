package com.joe.utils.data;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO原型
 *
 * @author joe
 * @version 2018.05.24 14:03
 */
@Data
public class BaseDTO<T> implements Serializable {
    private static final long serialVersionUID = 5075924626508128661L;
    private static final Map<String, String> MSG = new HashMap<>();

    static {
        // 成功
        MSG.put("200", "success");
        // 系统错误
        MSG.put("500", "systemError");
    }

    /**
     * 请求状态
     */
    private String status;
    /**
     * 错误消息
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    /**
     * 默认成功构造器
     */
    public BaseDTO() {
        this.success();
    }

    /**
     * 设置一个状态的构造器
     *
     * @param status 默认状态
     */
    public BaseDTO(String status) {
        this.setStatus(status);
    }

    /**
     * 添加全局status-msg对（200、500为预设状态，200表示成功，500表示系统异常）
     *
     * @param status 状态码
     * @param msg    对应的消息
     */
    public static void addStatus(String status, String msg) {
        MSG.put(status, msg);
    }

    /**
     * 系统错误，未知原因（异常）
     */
    public void systemError() {
        this.setStatus("500");
    }

    /**
     * 设置状态码
     *
     * @param status  状态码
     * @param message 错误消息
     */
    private void status(String status, String message) {
        this.status = status;
        this.message = message;
    }

    private void setStatus(String status) {
        this.message = MSG.get(status);
        this.status = status;
    }

    /**
     * 请求成功
     */
    private void success() {
        setStatus("200");
    }

    /**
     * 构建简单的成功对象
     *
     * @param <T> 数据类型
     * @return 成功DTO
     */
    public static <T> BaseDTO<T> buildSuccess() {
        return new BaseDTO<>();
    }

    /**
     * 构建包含成功数据的成功对象
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 包含数据的成功对象
     */
    public static <T> BaseDTO<T> buildSuccess(T data) {
        BaseDTO<T> dto = new BaseDTO<>();
        dto.setData(data);
        return dto;
    }

    /**
     * 构建系统异常对象
     *
     * @param <T> 数据类型
     * @return 系统异常对象
     */
    public static <T> BaseDTO<T> buildError() {
        BaseDTO<T> dto = new BaseDTO<>();
        dto.setStatus("500");
        dto.setMessage("系统异常");
        return dto;
    }

    /**
     * 构建系统异常对象，status为500，msg为用户传入参数
     *
     * @param msg 异常消息
     * @param <T> 数据类型
     * @return 系统异常对象
     */
    public static <T> BaseDTO<T> buildError(String msg) {
        BaseDTO<T> dto = new BaseDTO<>();
        dto.setStatus("500");
        dto.setMessage(msg);
        return dto;
    }

    /**
     * 构建指定的错误信息
     *
     * @param status  错误代码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误消息
     */
    public static <T> BaseDTO<T> buildError(String status, String message) {
        BaseDTO<T> dto = new BaseDTO<>();
        dto.status(status, message);
        return dto;
    }
}
