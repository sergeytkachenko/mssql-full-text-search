package com.bpmonline.controller;

import com.bpmonline.model.Activity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
public class IndexController {

    public String getContent(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    @RequestMapping("/")
    public String greeting(@RequestParam(value="q", required=false) String q, Model model) throws SQLException, IOException {
        String url = String.format("http://localhost:9200/demo/_search?size=30&q=title:%s", URLEncoder.encode(q, "UTF-8"));
        String content = getContent(url);
        HashMap map = new ObjectMapper().readValue(content, HashMap.class);
        LinkedHashMap hitsWrap = (LinkedHashMap) map.get("hits");
        int took = (int) map.get("took");
        ArrayList<LinkedHashMap> hits = (ArrayList<LinkedHashMap>)hitsWrap.get("hits");
        ArrayList<Activity> activities = new ArrayList<>();
        hits.forEach(hit -> {
            LinkedHashMap source = (LinkedHashMap) hit.get("_source");
            double score = (double) hit.get("_score");
            Activity activity = new Activity();
            activity.title = (String) source.get("title");
            activity.body = (String) source.get("body");
            activity.body = replaceTags(activity.body);
            activity.rank = String.valueOf(score);
            activity.detailResult = (String) source.get("detailResult");
            activity.recepient = (String) source.get("recepient");
            activity.copyRecepient = (String) source.get("copyRecepient");
            activity.sender = (String) source.get("sender");
            activities.add(activity);
        });
        model.addAttribute("activities", activities);
        model.addAttribute("executeTime", took + " ms");
        model.addAttribute("q", q);
        return "index";
    }

    private String replaceTags(String value) {
        return value.replaceAll("(<([\\s/]*(a|abbr|acronym|address|applet|area|article|aside|audio|b|base|basefont|bdi|bdo|bgsound|blockquote|big|body|blink|br|button|canvas|caption|center|cite|code|col|colgroup|command|comment|datalist|dd|del|details|dfn|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|form|footer|frame|frameset|h1|h2|h3|h4|h5|h6|head|header|hgroup|hr|html|i|iframe|img|input|ins|isindex|kbd|keygen|label|legend|li|link|main|map|marquee|mark|menu|meta|meter|nav|nobr|noembed|noframes|noscript|object|ol|optgroup|option|output|p|param|plaintext|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|span|source|strike|strong|style|sub|summary|sup|table|tbody|td|textarea|tfoot|th|thead|time|title|tr|tt|u|ul|var|video|wbr|xmp)(\\b|\\/)[^>]*)>)", "");
    }

}
