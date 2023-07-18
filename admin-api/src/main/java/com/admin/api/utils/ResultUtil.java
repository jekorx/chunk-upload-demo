package com.admin.api.utils;

import com.admin.api.constant.Result;
import com.admin.api.constant.ResultEnums;

/**
 * 返回值包装工具类
 * @author wang_dgang
 * @since 2018-10-22 15:53:56
 */
public class ResultUtil {
	/**
	 * 请求成功，无返回结果的
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success() {
		return new Result<>(ResultEnums.SUCCESS);
	}
	/**
	 * 请求成功，通用提示
	 * @param resultEnums
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success(ResultEnums resultEnums) {
		return new Result<>(resultEnums);
	}
	/**
	 * 请求成功
	 * @param object 返回值
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success(T object) {
		return new Result<>(ResultEnums.SUCCESS, object);
	}
	/**
	 * 请求成功（如：保存、修改成功，返回结果可为null，提示保存成功、修改成功、删除成功等）
	 * @param msg
	 * @param object 返回值
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success(String msg, T object) {
		return new Result<>(ResultEnums.SUCCESS.getCode(), msg, object);
	}
	/**
	 * 请求成功
	 * @param resultEnums
	 * @param object 返回值
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success(ResultEnums resultEnums, T object) {
		return new Result<>(resultEnums, object);
	}
	/**
	 * 请求失败（Enums.FAILED，请求失败）
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> error() {
		return new Result<>(ResultEnums.FAILED);
	}
	/**
	 * 请求失败（Enums中定义的错误）
	 * @param resultEnum
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> error(ResultEnums resultEnum) {
		return new Result<>(resultEnum);
	}
	/**
	 * 请求失败（Enums.FAILED，msg自定义，如：返回一些字段验证错误信息，使用频率低没必要在Enums中定义的）
	 * @param msg
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> error(String msg) {
		return new Result<>(ResultEnums.FAILED.getCode(), msg);
	}
	/**
	 * 请求失败（code，msg 都是自定义，尽量现在Enums中定义错误（尤其是常用的错误），避免使用此方法）
	 * @param code
	 * @param msg
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> error(int code, String msg) {
		return new Result<>(code, msg);
	}
}
