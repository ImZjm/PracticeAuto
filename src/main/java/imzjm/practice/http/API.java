package imzjm.practice.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class API {
    private static final String proxyHost = "127.0.0.1";
    private static final int proxyPort = 8888;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            //.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)))
            .sslContext(createUnverifiedSslContext())
            .build();

    public static HttpResponse<String> sendRequest(HttpRequest request) throws URISyntaxException, IOException, InterruptedException {

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static SSLContext createUnverifiedSslContext() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return sslContext;
    }

}
