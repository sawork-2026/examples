package com.example.echo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EchoController.class)
class EchoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void info_returnsAppNameVersionAndHostname() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app").value("echo-boot"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.hostname").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void echo_returnsMessage() throws Exception {
        mvc.perform(get("/echo").param("msg", "hello-k8s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.echo").value("hello-k8s"))
                .andExpect(jsonPath("$.pod").exists());
    }

    @Test
    void echo_defaultMessage() throws Exception {
        mvc.perform(get("/echo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.echo").value("hello"));
    }
}
