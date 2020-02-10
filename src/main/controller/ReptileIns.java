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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author JC
 */
public class ReptileIns {
    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";


    public static void main(String[] args) {
        //设置代理
        String proxyHost = "127.0.0.1";
        System.setProperty("http.proxyHost", proxyHost);
        String proxyPort = "64562";
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
        //爬取页面
        List<String> urlList = new ArrayList<>();
        urlList.add("https://www.instagram.com/mobil1/?hl=en");
        urlList.add("https://www.instagram.com/volkswagen/?hl=en");
        urlList.add("https://www.instagram.com/arnaud_4k/");
        urlList.add("https://www.instagram.com/dream.whipz/?hl=en");
        urlList.add("https://www.instagram.com/mustangsfamily/");
        urlList.add("https://www.instagram.com/genzlla/?hl=en");
        urlList.add("https://www.instagram.com/worldofmusclecar/");
        urlList.add("https://www.instagram.com/chevelleusa/");
        urlList.add("https://www.instagram.com/sheedysgt350/");
        urlList.add("https://www.instagram.com/gtrgoals/");
        urlList.add("https://www.instagram.com/duniamodifikasinusantara/");
        urlList.add("https://www.instagram.com/auto_magznet/?hl=en");
        urlList.add("https://www.instagram.com/mobilgue/?hl=en");
        urlList.add("https://www.instagram.com/motomobitv/?hl=en");
        urlList.add("https://www.instagram.com/carmudiid/?hl=en");
        urlList.add("https://www.instagram.com/otodriver_/?hl=en");
        urlList.add("https://www.instagram.com/mobil123id/?hl=en");
        urlList.add("https://www.instagram.com/otosia/?hl=en");
        urlList.add("https://www.instagram.com/otomotifweekly/?hl=en");
        urlList.add("https://www.instagram.com/cintamobilcom/?hl=en");
        urlList.add("https://www.instagram.com/mobilangka.id/?hl=id");
        try {
            List<Content> contentList = new ArrayList<>();
            for (String url : urlList) {
                //每个页面间隔一秒
                Thread.sleep(1000);
                //连接url
                int timeout = 300000;
                Document doc = Jsoup.connect(url)
                        //模拟浏览器访问
                        .userAgent("User-Agent:Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36")
                        //设置超时
                        .timeout(timeout).followRedirects(true)
                        .get();
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
                        id = users.get("id").toString();
                        edgeOwnerToTimelineMedia = getJsonObject(users, "edge_owner_to_timeline_media");
                        JSONArray edges = getJSONArray(edgeOwnerToTimelineMedia, "edges");
                        List<Content> urlData = getUrlData(edges);
                        if(urlData != null && urlData.size() > 0){
                            contentList.addAll(urlData);
                        }
                    }
                    int number = 0;
                    List<Content> moreData = getMoreData(doc, edgeOwnerToTimelineMedia, id, number);
                    if (moreData != null && moreData.size() > 0) {
                        contentList.addAll(moreData);
                    }
                }
            }
            System.out.println(contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Content> getMoreData(Document doc, JSONObject edgeOwnerToTimelineMedia, String id, int number) throws InterruptedException, UnsupportedEncodingException {
        if (doc == null || edgeOwnerToTimelineMedia == null || id == null) {
            return null;
        }
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
        String queryId = HttpClientUtil.doGet(url);
        if (queryId == null || "".equals(queryId)) {
            return null;
        }
        int i = queryId.lastIndexOf("queryId", queryId.lastIndexOf("queryId") - 10);
        queryId = queryId.substring(i + 9);
        queryId = queryId.substring(0, queryId.indexOf(",") - 1);
        Map<String, Object> variables = new HashMap<>(16);
        variables.put("id", id);
        variables.put("first", 50);
        variables.put("after", endCursor.toString());
        String str = JSON.toJSONString(variables);
        //用新的字符编码生成字符串,INS使用encodeURIComponent编码，但encodeURI也可获取数据
        String data = java.net.URLEncoder.encode(str, "UTF-8");
//        String data = encodeURIComponent(str);
        //请求分页数据
        String moreUrl = "https://www.instagram.com/graphql/query/?query_hash=";
        String pageData = HttpClientUtil.doGet(moreUrl + queryId + "&variables=" + data);
        if (pageData == null || "".equals(pageData)) {
            return null;
        }
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






















    /**
     * Description:
     *
     * @param input input
     * @return String
     */
    private static String encodeURIComponent(String input) {
        if (null == input || "".equals(input.trim())) {
            return input;
        }

        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        try {
            for (int i = 0; i < l; i++) {
                String e = input.substring(i, i + 1);
                if (!ALLOWED_CHARS.contains(e)) {
                    byte[] b = e.getBytes(StandardCharsets.UTF_8);
                    o.append(getHex(b));
                    continue;
                }
                o.append(e);
            }
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    private static String getHex(byte[] buf) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (byte aBuf : buf) {
            int n = (int) aBuf & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }

}
