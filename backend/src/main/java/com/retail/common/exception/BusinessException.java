package com.retail.common.exception;

/**
 * 业务异常：由业务逻辑主动抛出，全局异常处理器统一转为 Result
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
