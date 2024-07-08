package com.example.demo;

import com.github.dockerjava.api.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.testcontainers.ollama.OllamaContainer;

import java.io.IOException;

@Service
@ApplicationScope
public class OllamaService {

    private final OllamaContainer ollama;

    private final ChatClient.Builder chatClientBuilder;

    public OllamaService(ChatClient.Builder chatClientBuilder) {
        ollama = new OllamaContainer("ollama/ollama:0.1.48")
                .withCreateContainerCmdModifier(cmd ->
                {
                    cmd.withBinds(Bind.parse("ollama:/root/.ollama"));
                    cmd.withPortBindings(new PortBinding(Ports.Binding.bindPort(11434), new ExposedPort(11434)));
                });

        this.chatClientBuilder = chatClientBuilder;
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        ollama.start();
        ollama.execInContainer("ollama", "pull", "mistral");
    }
    @PreDestroy
    public void stop() {
        if (ollama.isRunning()) {
            ollama.stop();
        }
    }

    public ChatClient getChatClient() {
        if (ollama.isRunning()) {
            return chatClientBuilder.build();
        }
        return null;
    }
}
