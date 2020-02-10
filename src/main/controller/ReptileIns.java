package main.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import main.model.Content;
import main.util.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author JC
 */
public class ReptileIns {

    private static String proxyHost = "127.0.0.1";
    private static String proxyPort = "64562";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        //设置代理
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
        try {
            List<Content> contentList = new ArrayList<>();
            //连接url
            String url = "https://www.instagram.com/mobil1/?hl=en";
            //获取该链接首页数据
            Document doc = Jsoup.connect(url)
                    //模拟浏览器访问
                    .userAgent("User-Agent:Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36")
                    //设置超时
                    .timeout(30000).followRedirects(true)
                    .get();
            //解析数据
            Elements script = doc.getElementsByTag("script");
            Element element1 = script.get(4);
            String html = element1.html();
            String replace = html.replace("window._sharedData = ", "").replace(";", "");
            JSONObject jsonObject = JSONObject.parseObject(replace);
            JSONObject entryData = getJsonObject(jsonObject, "entry_data");
            JSONArray profilePage = getJSONArray(entryData, "ProfilePage");
            for (Object object : profilePage) {
                JSONObject edgeOwnerToTimelineMedia = new JSONObject();
                String id = "";
                JSONObject jsonObject2 = JSONObject.parseObject(object.toString());
                if (jsonObject2.containsKey("graphql")) {
                    Object jsonObject111 = jsonObject2.get("graphql");
                    JSONObject graphql = JSONObject.parseObject(jsonObject111.toString());
                    JSONObject users = getJsonObject(graphql, "user");
                    //分页需要数据
                    id = users.get("id").toString();
                    edgeOwnerToTimelineMedia = getJsonObject(users, "edge_owner_to_timeline_media");
                    JSONArray edges = getJSONArray(edgeOwnerToTimelineMedia, "edges");
                    List<Content> urlData = getUrlData(edges);
                    if (urlData != null && urlData.size() > 0) {
                        contentList.addAll(urlData);
                    }
                }
                //获取后面分页数据
                int number = 0;
                List<Content> moreData = getMoreData(doc, edgeOwnerToTimelineMedia, id, number);
                if (moreData != null && moreData.size() > 0) {
                    contentList.addAll(moreData);
                }
            }
            System.out.println("爬取数据条数为：" + contentList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }

    /**
     * 获取分页数据
     *
     * @param doc                      获取hash_code数据支持
     * @param edgeOwnerToTimelineMedia 获取分页数据支持
     * @param id                       用户id
     * @param number                   递归次数
     * @return 结果
     * @throws UnsupportedEncodingException 编码异常
     */
    private static List<Content> getMoreData(Document doc, JSONObject edgeOwnerToTimelineMedia, String id, int number) throws UnsupportedEncodingException {
        if (doc == null || edgeOwnerToTimelineMedia == null || id == null) {
            return null;
        }
        //递归次数不大于500次
        number++;
        if (number > 500) {
            return null;
        }
        //获取分页数据
        String pageInfo = edgeOwnerToTimelineMedia.get("page_info").toString();
        if (pageInfo == null || "".equals(pageInfo)) {
            return null;
        }
        JSONObject pageInfoObject = JSONObject.parseObject(pageInfo);
        if (pageInfoObject == null) {
            return null;
        }
        Boolean hasNextPage = (Boolean) pageInfoObject.get("has_next_page");
        //是否还有数据没有直接返回
        if (!hasNextPage) {
            return null;
        }
        Object endCursor = pageInfoObject.get("end_cursor");
        if (endCursor == null) {
            return null;
        }
        //获取hash_code
        Elements script = doc.getElementsByTag("script");
        if (script == null || script.size() < 1) {
            return null;
        }
        Element element1 = script.get(14);
        String src1 = element1.attr("src");
        String url = "https://www.instagram.com" + src1;
        //请求分页数据，需要设置代理
        String queryId = HttpClientUtil.doGet(url, proxyHost, Integer.valueOf(proxyPort));
        if (queryId == null || "".equals(queryId)) {
            return null;
        }
        //TODO hash_code
        int i = queryId.lastIndexOf("queryId", queryId.lastIndexOf("queryId") - 10);
        queryId = queryId.substring(i + 9);
        queryId = queryId.substring(0, queryId.indexOf(",") - 1);
        Map<String, Object> variables = new HashMap<>(16);
        variables.put("id", id);
        variables.put("first", 50);
        variables.put("after", endCursor.toString());
        String str = JSON.toJSONString(variables);
        //url编码
        String data = java.net.URLEncoder.encode(str, "UTF-8");
        //请求分页数据,需要设置代理
        String moreUrl = "https://www.instagram.com/graphql/query/?query_hash=";
        String pageData = HttpClientUtil.doGet(moreUrl + queryId + "&variables=" + data, proxyHost, Integer.valueOf(proxyPort));
        if (pageData == null || "".equals(pageData)) {
            return null;
        }
        //解析数据
        JSONObject pageDataJson = JSONObject.parseObject(pageData);
        if (pageDataJson == null) {
            return null;
        }
        JSONObject dataJson = getJsonObject(pageDataJson, "data");
        if (dataJson == null) {
            return null;
        }
        JSONObject users = getJsonObject(dataJson, "user");
        if (users == null) {
            return null;
        }
        JSONObject edgeOwnerToTimelineMedia1 = getJsonObject(users, "edge_owner_to_timeline_media");
        if (edgeOwnerToTimelineMedia1 == null) {
            return null;
        }
        JSONArray edges = getJSONArray(edgeOwnerToTimelineMedia1, "edges");
        if (edges == null) {
            return null;
        }
        List<Content> urlData = getUrlData(edges);
        if (urlData == null) {
            urlData = new ArrayList<>();
        }
        //下页还有数据递归调用
        List<Content> moreData = getMoreData(doc, edgeOwnerToTimelineMedia1, id, number);
        if (moreData != null && moreData.size() > 0) {
            urlData.addAll(moreData);
        }
        return urlData;
    }

    private static JSONObject getJsonObject(JSONObject jsonObject, String key) {
        if (null == jsonObject || null == key || "".equals(key)) {
            return null;
        }
        Object object = jsonObject.get(key);
        if (null == object) {
            return null;
        }
        return JSONObject.parseObject(object.toString());
    }

    private static JSONArray getJSONArray(JSONObject jsonObject, String key) {
        if (null == jsonObject || null == key || "".equals(key)) {
            return null;
        }
        Object object = jsonObject.get(key);
        if (null == object) {
            return null;
        }
        return JSONObject.parseArray(object.toString());
    }

    private static List<Content> getUrlData(JSONArray edges) {
        List<Content> contentList = new ArrayList<>();
        if (null == edges) {
            return contentList;
        }
        for (Object o : edges) {
            JSONObject jsonObject = JSONArray.parseObject(o.toString());
            if (null == jsonObject) {
                return null;
            }
            Object node = jsonObject.get("node");
            if (null == node) {
                return null;
            }
            JSONObject displayUrl = JSONArray.parseObject(node.toString());
            if (null == displayUrl) {
                return null;
            }
            if ((Boolean) displayUrl.get("is_video")) {
                Content content = new Content();
                Object id = displayUrl.get("id").toString();
                if (null == id) {
                    return null;
                }
                content.setId(id.toString());
                String videoUrl = "https://www.instagram.com/p/";
                String video = videoUrl + displayUrl.get("shortcode").toString();
                content.setViedeoUrl(video);
                Object display = displayUrl.get("display_url");
                if (null == display) {
                    return null;
                }
                content.setPicUrl(display.toString());
                contentList.add(content);
            }
        }
        return contentList;
    }
}
