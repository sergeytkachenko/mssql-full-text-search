package com.bpmonline.controller;

import com.bpmonline.model.Activity;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

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
    public String greeting(@RequestParam(value="q", required=false) String q, Model model) throws SQLException {

        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        String sql = String.format("SELECT top 30 a1.*, a2.RANK FROM Activity a1\n" +
                "\tINNER JOIN FREETEXTTABLE(Activity, title, '%s', LANGUAGE 1049, 30) as a2 ON a1.id = a2.[KEY]", q);
        long startTime = System.nanoTime();
        ResultSet rs = stmt.executeQuery(sql);
        ArrayList<Activity> activities = new ArrayList<>();
        while (rs.next()) {
            String title = rs.getString("title");
            String body = rs.getString("body");
            String rank = rs.getString("RANK");
            String sender = rs.getString("sender");
            String detailResult = rs.getString("detailedResult");
            String recepient = rs.getString("recepient");
            String copyRecepient = rs.getString("copyRecepient");
            Activity activity = new Activity();
            activity.title = title;
            body = replaceTags(body);
            body = body.replaceAll("&nbsp;", "");
            activity.body = body;
            activity.rank = rank;
            activity.sender = sender;
            activity.detailResult = detailResult;
            activity.recepient = recepient;
            activity.copyRecepient = copyRecepient;
            activities.add(activity);
        }
        long executeTime = System.nanoTime() - startTime;
        model.addAttribute("resultCount", activities.size());
        model.addAttribute("activities", activities);
        model.addAttribute("executeTime", executeTime / 1000000 + " ms");
        model.addAttribute("q", q);
        return "index";
    }

    private String replaceTags(String value) {
        return value.replaceAll("(<([\\s/]*(a|abbr|acronym|address|applet|area|article|aside|audio|b|base|basefont|bdi|bdo|bgsound|blockquote|big|body|blink|br|button|canvas|caption|center|cite|code|col|colgroup|command|comment|datalist|dd|del|details|dfn|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|form|footer|frame|frameset|h1|h2|h3|h4|h5|h6|head|header|hgroup|hr|html|i|iframe|img|input|ins|isindex|kbd|keygen|label|legend|li|link|main|map|marquee|mark|menu|meta|meter|nav|nobr|noembed|noframes|noscript|object|ol|optgroup|option|output|p|param|plaintext|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|span|source|strike|strong|style|sub|summary|sup|table|tbody|td|textarea|tfoot|th|thead|time|title|tr|tt|u|ul|var|video|wbr|xmp)(\\b|\\/)[^>]*)>)", "");
    }

}
