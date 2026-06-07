package com.mtfm.deadman.plugin.excel.exception;

/**
 * Excel 导入导出过程中的运行时异常。
 */
public class DeadExcelException extends RuntimeException {

    /**
     * 构造异常。
     *
     * @param message 错误说明
     */
    public DeadExcelException(String message) {
        super(message);
    }

    /**
     * 构造异常。
     *
     * @param message 错误说明
     * @param cause   原始异常
     */
    public DeadExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
