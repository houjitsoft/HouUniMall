package com.houjiang.base.plugincenter;

import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Map;

/** Installer addresses and credentials are deployment configuration, never caller input. */
@Component
public class ServiceInstallerClient {
    private final Environment env;
    private final RestTemplate http;
    public ServiceInstallerClient(Environment env) {
        this.env=env;
        var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);factory.setReadTimeout(15000);
        http=new RestTemplate(factory);
    }
    public String configuredInstance() {return env.getProperty("unimall.installer.instance-id","");}
    public Object call(String instance,String path,Map<String,Object> body) {
        String origin=env.getProperty("unimall.installer.url","");
        String secret=env.getProperty("unimall.installer.token","");
        if (instance==null || instance.isBlank() || !instance.equals(configuredInstance()) || secret.length()<32 || origin.isBlank())
            throw new IllegalStateException("当前站点尚未绑定安装代理");
        var uri=URI.create(origin);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost()==null
                || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null || !path.matches("/tasks(?:/[a-f0-9-]{36})?"))
            throw new IllegalStateException("安装代理地址配置无效");
        var headers=new HttpHeaders();headers.setBearerAuth(secret);headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-UniMall-Instance",instance);
        return http.exchange(origin.replaceAll("/+$","")+path,body==null?HttpMethod.GET:HttpMethod.POST,new HttpEntity<>(body,headers),Object.class).getBody();
    }
}
