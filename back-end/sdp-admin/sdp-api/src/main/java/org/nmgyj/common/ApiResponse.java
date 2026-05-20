package org.nmgyj.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一 REST 响应封装：业务成功时 {@code code=0}，失败时为非零错误码并附带提示信息。
 *
 * @param <T> 业务数据类型
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "统一 API 响应")
public class ApiResponse<T> {

    @Schema(description = "业务状态码，0 表示成功", example = "0")
    private Integer code;

    @Schema(description = "提示信息", example = "success")
    private String message;

    @Schema(description = "业务数据载荷")
    private T data;

    /**
     * 构造成功响应。
     *
     * @param data 业务数据，可为 null
     * @param <T>  数据类型
     * @return 封装后的响应体
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    /**
     * 构造失败响应（默认错误码为 1）。
     *
     * @param message 错误描述
     * @param <T>     数据类型占位
     * @return 封装后的响应体
     */
    public static <T> ApiResponse<T> fail(String message) {
        return fail(1, message);
    }

    /**
     * 构造失败响应。
     *
     * @param code    非零错误码
     * @param message 错误描述
     * @param <T>     数据类型占位
     * @return 封装后的响应体
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * @return 业务状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @param code 业务状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message 提示信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return 业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * @param data 业务数据
     */
    public void setData(T data) {
        this.data = data;
    }
}
