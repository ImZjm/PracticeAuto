package imzjm.practice.service;

import imzjm.practice.http.API;
import imzjm.practice.kfc.ClockInfo;
import imzjm.practice.kfc.iKun;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cdvisor {

    public static String getToken() {
        HttpResponse<String> response;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI("http://wgxy.cdvisor.com/login.xhtml"))
                    .build();
            response = API.sendRequest(request);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Matcher matcher = Pattern.compile("(?<=tokenstr\" type=\"hidden\" value=\")([^\"]*)").matcher(response.body());
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static String getCookie(iKun iKun, String token) {
        //请求参数
        StringBuilder params = new StringBuilder();
        params.append("tokenstr=").append(token);
        params.append("&");
        params.append("action=login&");
        params.append("auser=").append(iKun.getId());
        params.append("&");
        params.append("apass=").append(getMD5Hash(iKun.getPwd()));
        params.append("&");
        params.append("acode=whgc");

        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("http://wgxy.cdvisor.com/login.xhtml"))
                    .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            response = API.sendRequest(request);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!response.headers().allValues("Set-Cookie").isEmpty()) {
            String cookie = response.headers().allValues("Set-Cookie").get(0);
            Matcher matcher = Pattern.compile("(?<=userinfo=)([^;]*)").matcher(cookie);
            if (matcher.find())
                return matcher.group();

        }
        return "";

    }

    public static String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void getClockInfo(ClockInfo clockInfo){

        //垃圾网站,发送请求时,把wd和jd搞个反的,这里只能将错就错
        String body = "wd=" + clockInfo.getJd() + "&" + "jd=" + clockInfo.getWd();
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://www.cdvisor.com:8443/4142012738/app/sxjy/s_sx_sxqd.xhtml?action=qdget"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            response = API.sendRequest(request);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String data = response.body();

        //更新经度
        Matcher jd = Pattern.compile("(?<=\"lng\":)([^,]*)").matcher(data);
        if (jd.find())
            clockInfo.setJd(jd.group());

        //更新纬度
        Matcher wd = Pattern.compile("(?<=\"lat\":)([^}]*)").matcher(data);
        if (wd.find())
            clockInfo.setWd(wd.group());

        //更新省
        Matcher sheng = Pattern.compile("(?<=\"province\":\")([^\"]*)").matcher(data);
        if (sheng.find())
            clockInfo.setSheng(sheng.group());

        //更新市
        Matcher shi = Pattern.compile("(?<=\"city\":\")([^\"]*)").matcher(data);
        if (shi.find())
            clockInfo.setShi(shi.group());

        //更新区
        Matcher qu = Pattern.compile("(?<=\"district\":\")([^\"]*)").matcher(data);
        if (qu.find())
            clockInfo.setQu(qu.group());

        //更新location
        Matcher location = Pattern.compile("(?<=\"formatted_address\":\")([^\"]*)").matcher(data);
        if (location.find())
            clockInfo.setLocation(location.group());
    }

    public static String qdSave(ClockInfo clockInfo, String cookie, GroupMessageEvent event) {
        HttpResponse<String> response;
        String boundary = "------WebKitFormBoundarykfcCrazyTHvivo50";
        String body = boundary + "\r\nContent-Disposition: form-data; name=\"anquan\"" + "\r\n\r\n" + clockInfo.getAQ() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"jiankang\"" + "\r\n\r\n" + clockInfo.getJK() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"jd\"" + "\r\n\r\n" + clockInfo.getJd() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"wd\"" + "\r\n\r\n" + clockInfo.getWd() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"atype\"" + "\r\n\r\n" + clockInfo.getaTYPE() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"sheng\"" + "\r\n\r\n" + clockInfo.getSheng() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"shi\"" + "\r\n\r\n" + clockInfo.getShi() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"qu\"" + "\r\n\r\n" + clockInfo.getQu() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"location\"" + "\r\n\r\n" + clockInfo.getLocation() + "\r\n" +
                boundary + "\r\nContent-Disposition: form-data; name=\"filenames\"" + "\r\n\r\n" + clockInfo.getFilenames() + "\r\n" +
                boundary + "--";
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://www.cdvisor.com:8443/4142012738/app/sxjy/s_sx_sxqd.xhtml?action=qdsave"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47")
                    .header("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundarykfcCrazyTHvivo50")
                    .header("Cookie", "userinfo=" + cookie)
                    .build();

            response = API.sendRequest(request);

        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("签到状态: " + response.body());
        return response.body();

    }

    public static String qdSaveNew(ClockInfo clockInfo, String cookie) {
        CloseableHttpResponse response;

        HttpHost proxyHost = new HttpHost("127.0.0.1", 8888);

        try (
                //CloseableHttpClient httpClient = HttpClients.custom().setProxy(proxyHost).setSSLContext(API.createUnverifiedSslContext()).build()
                CloseableHttpClient httpClient = HttpClients.createDefault()
        ) {

            HttpPost httpPost = new HttpPost("https://www.cdvisor.com:8443/4142012738/app/sxjy/s_sx_sxqd.xhtml?action=qdsave");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=kfcCrazyTHvivo50");
            httpPost.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47");
            httpPost.setHeader("Cookie", "userinfo=" + cookie);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            multipartEntityBuilder.setBoundary("kfcCrazyTHvivo50");
            multipartEntityBuilder.addTextBody("anquan", clockInfo.getAQ(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("jiankang", clockInfo.getJK(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("jd", clockInfo.getJd(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("wd", clockInfo.getWd(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("atype", String.valueOf(clockInfo.getaTYPE()), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("sheng", clockInfo.getSheng(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("shi", clockInfo.getShi(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("qu", clockInfo.getQu(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("location", clockInfo.getLocation(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("filenames", clockInfo.getFilenames(), ContentType.APPLICATION_JSON);
            HttpEntity multipartEntity = multipartEntityBuilder.build();

            httpPost.setEntity(multipartEntity);

            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        HttpEntity responseEntity = response.getEntity();
        try {
            String body = EntityUtils.toString(responseEntity);
            System.out.println("签到状态: " + body);
            return body;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
