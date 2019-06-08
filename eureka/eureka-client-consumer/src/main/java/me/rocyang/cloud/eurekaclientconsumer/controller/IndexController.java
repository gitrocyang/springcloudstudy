package me.rocyang.cloud.eurekaclientconsumer.controller;

import com.alibaba.fastjson.JSONObject;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String App_Name = "eureka-client-provider";

    @RequestMapping("/provider")
    public JSONObject callProvider(){
        //直接访问提供者，绕开了eureka服务中心
        /*return restTemplate.getForObject(
                "http://localhost:8001/index/name",
                JSONObject.class
        );*/
        //通过eureka服务中心访问微服务
        return restTemplate.getForObject(
                "http://eureka-client-provider/index/name",
                JSONObject.class
        );
    }

    @Autowired
    private EurekaClient eurekaClient;
    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/appinfo/eureka")
    public Object getAppInfoByEurekaClient(){
        return eurekaClient.getInstancesByVipAddress(App_Name,false);

    }

    @RequestMapping("/appinfo/discovery")
    public Object getAppInfoByDiscoveryClient(){
        return discoveryClient.getInstances(App_Name);
    }

}
