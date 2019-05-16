package com.red.generator.model;

import lombok.Data;

/**
 * DatabaseDTO
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/16     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-16 09:07
 * @since 1.0.0
 */
@Data
public class DatabaseDTO {
    private String name;
    private int value;
    private String driverClass;
}
