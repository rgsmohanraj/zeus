package org.vcpl.lms.portfolio.loanaccount.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

@Path("/vpay")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VpayTransactionEnquiryApiResource {
    @Value("${vpay.keystore.password}")
    private String password;
    @Value("${vpay.access.url}")
    private String vPayUrl;
    @Value("${vpay.keystore.url}")
    private String keyStoreUrl;

    @POST
    @Path("transactionenquiry")
    public String transactionEnquiry(@RequestBody Map map) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        File jksFile = new File(keyStoreUrl);
        InputStream jksFileStream = new FileInputStream(jksFile);

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(jksFileStream, password.toCharArray());

        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(ks, (chain, authType) -> true).loadKeyMaterial(ks, password.toCharArray())
                .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslcontext)
                .build();

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();

        HttpPost post = new HttpPost(vPayUrl + "/v1/transcationenquiry");
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        post.setEntity(new StringEntity(mapper.writeValueAsString(map), ContentType.APPLICATION_JSON.getCharset()));
        CloseableHttpResponse response = httpClient.execute(post);
        return mapper.readValue(response.getEntity().getContent(),Map.class).toString();
    }
}
