package com.bpmonline.controller;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

@Controller
public class IndexController {

    public javax.sql.DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://localhost\\mssql2014:49172;databaseName=work");
        dataSource.setUsername("sergey");
        dataSource.setPassword("!Prisoner33!");
        return dataSource;
    }

    @RequestMapping("/")
    public String greeting(@RequestParam(value="q", required=false) String q, Model model) throws SQLException, IOException {
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ClassLoader classLoader = getClass().getClassLoader();
        String fileName = "sql/example.sql";
        File file = new File(classLoader.getResource(fileName).getFile());
        String sqlString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
        String sql = String.format(sqlString, q);
        System.out.println(sql);
        ResultSet rs = stmt.executeQuery(sql);
        int i = 0;
        while (rs.next()) {
            i++;
        }
        return "index";
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
