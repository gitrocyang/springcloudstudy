package me.rocyang.cloud.eurekastart.listener;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaStateChangeListner {

    private Logger logger = LoggerFactory.getLogger(EurekaStateChangeListner.class);

    @EventListener
    public void listen(EurekaInstanceCanceledEvent event){
        logger.info("{} \t {} 服务下线",event.getServerId(),event.getAppName());
    }

    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event){
        InstanceInfo info = event.getInstanceInfo();
        logger.info("{}:{} \t {} 服务上线",info.getIPAddr(),info.getPort(),info.getAppName());
    }

    @EventListener
    public void listen(EurekaInstanceRenewedEvent event){
        logger.info("{} \t {} 服务续约",event.getServerId(),event.getAppName());
    }

    @EventListener
    public void listen(EurekaRegistryAvailableEvent event){
        logger.info("注册中心 启动");
    }

    @EventListener
    public void listen(EurekaServerStartedEvent event){
        logger.info("Eureka Server 启动");
    }
}
