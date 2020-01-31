package config;

import lombok.Data;

@Data
public class DatabaseConfig {

    private String JDBC_DRIVER = "org.h2.Driver";
    private String DB_URL = "jdbc:h2:mem:test;INIT=create schema if not exists test\\;"
        + "runscript from '/Users/manish.jain/IdeaProjects/moneyTransferRevolut/src/main/resources/create.sql'\\;"
        + " runscript from '/Users/manish.jain/IdeaProjects/moneyTransferRevolut/src/main/resources/populate.sql';";

    private String USER = "sa";
    private String PASS = "";
}
