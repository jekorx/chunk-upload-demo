package com.admin.api.constant;

/**
 * 返回值包装类
 * @author wang_dgang
 * @since 2018-10-22 15:53:48
 * @param <T>
 */
public class Result<T> {
	// 状态码
	private int code;
	// 状态信息
	private String msg;
	// 返回值
	private T data;
	
	// 默认无参构造
	public Result() {
	}
	/**
	 * 构造函数
	 * @param resultEnum 枚举
	 */
	public Result(ResultEnums resultEnum) {
		this.code = resultEnum.getCode();
		this.msg = resultEnum.getMsg();
	}
	/**
	 * 构造函数
	 * @param code 状态码
	 * @param msg 状态信息
	 */
	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	/**
	 * 构造函数
	 * @param resultEnum 枚举 
	 * @param data 返回值
	 */
	public Result(ResultEnums resultEnum, T data) {
		this.code = resultEnum.getCode();
		this.msg = resultEnum.getMsg();
		this.data = data;
	}
	/**
	 * 构造函数
	 * @param code 状态码
	 * @param msg 状态信息
	 * @param data 返回值
	 */
	public Result(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
