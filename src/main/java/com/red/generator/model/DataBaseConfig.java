package com.red.generator.model;

import lombok.Data;

/**
 * data base config model
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 10:22
 * @since 1.0.0
 */
@Data
public class DataBaseConfig {
    /**
     * The primary key in the sqlite db
     */
    private Integer id;

    private String dbType;
    /**
     * The name of the config
     */
    private String name;

    private String host;

    private String port;

    private String schema;

    private String username;

    private String password;

    private String encoding;

    private String lport;

    private String rport;

    private String sshPort;

    private String sshHost;

    private String sshUser;

    private String sshPassword;
}
