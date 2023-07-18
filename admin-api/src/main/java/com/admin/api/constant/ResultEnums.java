package com.admin.api.constant;

/**
 * 枚举：返回码-->返回信息，主要用户返回信息
 * @author wang_dgang
 * @since 2018-10-22 15:51:57
 */
public enum ResultEnums {
	// 常用信息
	ERROR(-1, "请求错误"),
	FAILED(0, "请求失败"),
	SUCCESS(1, "请求成功"),
	
	// 请求错误相关
	ERR_UPLOAD(0, "上传失败"),

	// 请求成功相关
	SUCC_UPLOAD(1, "上传成功"),


	;
	// 状态码
	private int code;
	// 返回信息
	private String msg;
	ResultEnums(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
}
