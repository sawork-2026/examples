package com.example.echo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class EchoController {

    @Value("${app.version:1.0.0}")
    private String version;

    /**
     * Returns hostname and version — useful for verifying which Pod handled the request
     * when kubectl scale deployment echo-boot --replicas=2 is in effect.
     */
    @GetMapping("/")
    public Map<String, String> info() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("app", "echo-boot");
        info.put("version", version);
        info.put("hostname", hostname());
        info.put("message", "Request handled by pod: " + hostname());
        return info;
    }

    /** Echoes the message — shows load balancing across pods when replicas > 1. */
    @GetMapping("/echo")
    public Map<String, String> echo(@RequestParam(defaultValue = "hello") String msg) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("echo", msg);
        response.put("pod", hostname());
        response.put("version", version);
        return response;
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
