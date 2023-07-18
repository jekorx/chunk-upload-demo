package com.admin.api.config;

import com.admin.api.constant.ResultEnums;

/**
 * 自定义异常
 * 继承RuntimeException，而不是Exception，否则导致spring事务无法回滚
 * @author wang_dgang
 * @since 2018-10-23 15:51:36
 */
public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	// 自定义错误码
	private int code;
	private String msg;
	public BusinessException(ResultEnums resultEnum) {
		super(resultEnum.getMsg());
		this.code = resultEnum.getCode();
	}
	public BusinessException(int code, String msg) {
		super(msg);
		this.code = code;
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
}
