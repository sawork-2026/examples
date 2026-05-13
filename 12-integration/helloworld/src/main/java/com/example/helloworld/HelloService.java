package com.example.helloworld;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class HelloService {

    @ServiceActivator(inputChannel = "inputChannel", outputChannel = "outputChannel")
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
