package com.oz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.oz.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.regex.Pattern;

@Service
public class GoodServiceImpl implements GoodService {

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Boolean buyGood(String goodId, String username, String password) throws IOException, InterruptedException {
        boolean buyGoodIsOk = false;
        System.out.println("-----------------开始生成手机号码-----------------");
        String phone = phoneNote(username, password, 10);
        if (phone != null) {
            Long unionID = System.currentTimeMillis();
            System.out.println("-----------------创建用户-----------------");
            Boolean isok = register(phone, unionID);
            if (isok) {
                System.out.println("-----------------登录用户-----------------");
                String token = userLogin(phone, unionID);
                if (token != null) {
                    System.out.println("-----------------商品砍价-----------------");
                    boolean goodIsOk = goodBargain(token, goodId);
                    if (goodIsOk) {
                        buyGoodIsOk = true;
                        System.out.println("商品砍价成功");
                    } else {
                        System.out.println("商品砍价失败");
                    }
                } else {
                    System.out.println("用户登录失败");
                }
            } else {
                System.out.println("用户创建失败");
            }
        } else {
            System.out.println("电话号码获取失败");
        }
        Thread.sleep(10000);
        return buyGoodIsOk;
    }


    /**
     * 获取手机号那
     *
     * @param username 短信平台账号
     * @param password 短信平台密码
     * @param hits     取号码次数
     * @return
     */
    private String phoneNote(String username, String password, int hits) throws InterruptedException, IOException {
        String mobile = null;
        String getPhoneUrl = "http://www.jisuyunma.top:84/?shouduanxin_zaixianhaoma_plpt";
        String params = "username=" + username + "&password=" + password + "&xmid=15748&xzgj=不限&xzyys=不限&xzsf=不限&hmlx=实卡&glhmd=1&qhsl=1&dcjs=1";
        String resPhone = URLDecoder.decode(sendPost(getPhoneUrl, params));
        String phone = resPhone.substring(resPhone.indexOf("|") + 1, resPhone.lastIndexOf("|"));
        String pattern = "^1[\\d]{10}";
        boolean isMatch = Pattern.matches(pattern, phone);
        if (isMatch) {
            mobile = phone;
        }
        if (mobile == null && hits >= 0) {
            hits--;
            Thread.sleep(10000);
            phoneNote(username, password, hits);
        }
        return mobile;
    }

    /**
     * 用户注册
     *
     * @param mobile  手机号码
     * @param unionID 会员编码
     * @return
     */
    private Boolean register(String mobile, Long unionID) throws IOException {
        boolean isok = false;
        StringBuffer url = new StringBuffer("https://passport.gooeto.com/register/register");
        HttpHeaders headers = new HttpHeaders();
        headers.add("cookie", null);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject paramObject = new JSONObject();
        paramObject.put("phone", mobile);
        paramObject.put("password", "qwe123");
        paramObject.put("origin", "GYTJK");
        paramObject.put("from", "GYTJK");
        paramObject.put("source", "app");
        paramObject.put("deviceId", unionID);
        JSONObject bodyObject = applyHttpHeaders(url.toString(), paramObject, headers);
        if (bodyObject.containsKey("code") && bodyObject.get("code").equals(200)) {
            String mes = bodyObject.get("mes").toString();
            File fileOrFilename = new File("D://手机号码.txt");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOrFilename, true)));
            out.write("手机号码:" + mobile + "--登录提示:" + mes + "\n");
            out.close();
            isok = true;
        }
        System.out.println("创建用户:" + bodyObject);
        return isok;
    }

    /**
     * 用户登录接口
     *
     * @param mobile  手机号码
     * @param unionID 会员编码
     * @return
     */
    private String userLogin(String mobile, Long unionID) {
        String token = null;
        StringBuffer url = new StringBuffer("https://m.gooeto120.com/API/user/userLogin");
        HttpHeaders headers = new HttpHeaders();
        headers.add("cookie", null);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject paramObject = new JSONObject();
        paramObject.put("type", "1");
        paramObject.put("phone", mobile);
        paramObject.put("password", "qwe123");
        paramObject.put("nextURL", "1234");
        paramObject.put("from", "GYTJK");
        paramObject.put("origin", "GYTJK");
        paramObject.put("source", "app");
        paramObject.put("deviceId", unionID);
        JSONObject bodyObject = applyHttpHeaders(url.toString(), paramObject, headers);
        if (bodyObject.containsKey("data")) {
            JSONObject dataObject = bodyObject.getJSONObject("data");
            if (dataObject.containsKey("memberToken")) {
                token = dataObject.get("memberToken").toString();
            }
        }
        System.out.println("用户登录接口:" + bodyObject);
        return token;
    }

    /**
     * 砍价商品
     *
     * @param token
     * @param goodId 商品编码
     * @return
     */
    private boolean goodBargain(String token, String goodId) {
        boolean isok = false;
        StringBuffer url = new StringBuffer("https://jzhtbms.gooeto.com/API/newMall/activity/helpHaggle");
        HttpHeaders headers = new HttpHeaders();
        headers.add("cookie", "token=" + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject paramObject = new JSONObject();
        paramObject.put("activityHaggleId", goodId);
        JSONObject bodyObject = applyHttpHeaders(url.toString(), paramObject, headers);
        if (bodyObject.containsKey("code") && bodyObject.get("code").equals(200)) {
            isok = true;
        }
        System.out.println("砍价商品:" + bodyObject);
        return isok;
    }


    private JSONObject applyHttpHeaders(String url, JSONObject paramObject, HttpHeaders headers) {
        HttpEntity<JSONObject> request = new HttpEntity<>(paramObject, headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(url, request, JSONObject.class);
        JSONObject bodyObject = responseEntity.getBody();
        return bodyObject;
    }


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    private String sendPost(String url, String param) throws IOException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            throw e;
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
