package com.red.generator.exception;

/**
 * db driver loading exception
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/15     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-15 10:17
 * @since 1.0.0
 */
public class DbDriverLoadingException extends RuntimeException {
    public DbDriverLoadingException(String message) {
        super(message);
    }
}
