package priv.asura.wechat_service.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import priv.asura.wechat_service.Application;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientTest {

    private URL base;
    HttpHeaders requestHeaders = new HttpHeaders();
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        this.base = new URL(String.format("http://localhost:%d/", port));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("client-id", "abcdefghijklmnopqrstuvwxyz");
        requestHeaders.add("client-secret", "01234567189");
    }

    @Test
    public void createClient() {
        HttpEntity<Map> requestEntity = new HttpEntity<>(new HashMap(2) {{
            put("name", "测试");
        }}, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(this.base.toString() + "/client/create", requestEntity, String.class);
        System.out.println(String.format("测试结果为：%s", responseEntity.getBody()));
    }

    @Test
    public void createClientDouble() {
        createClient();
        createClient();
    }
}
