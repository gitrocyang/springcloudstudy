package me.rocyang.cloud.eurekaclientstart.controller;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/index")
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping("/name")
    public JSONObject getAppName(){
        logger.info("provider 被访问了");
        JSONObject json = new JSONObject();
        json.put("name","eureka-client-provider");
        return json;
    }

}
