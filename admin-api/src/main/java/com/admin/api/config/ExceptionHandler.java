package com.admin.api.config;

import com.admin.api.constant.Result;
import com.admin.api.constant.ResultEnums;
import com.admin.api.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 * @author wang_dgang
 * @since 2018-10-23 15:49:38
 */
@ControllerAdvice
public class ExceptionHandler {

	private final static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
	
	/**
	 * 异常处理，返回json格式的异常包装信息，而非错误页面
	 * @param e
	 * @return
	 */
	@org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
	@ResponseBody
	public <T> Result<T> handler(Exception e) {
		if (e instanceof BusinessException) {
			// 业务异常，响应相应错误
			BusinessException be = (BusinessException) e;
			return ResultUtil.error(be.getCode(), be.getMessage());
		} else {
			log.error(" -- 系统异常 --> {}", e);
			// 系统异常，相应相应错误
			return ResultUtil.error(ResultEnums.ERROR.getCode(), ResultEnums.ERROR.getMsg());
		}
	}

}
