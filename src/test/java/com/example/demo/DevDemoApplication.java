package com.example.demo;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.ollama.OllamaContainer;

import java.io.IOException;
import java.util.logging.Logger;


public class DevDemoApplication extends DemoApplication {

	public static void main(String[] args) {

		SpringApplication.run(DevDemoApplication.class, args);
	}

	@ServiceConnection
	OllamaContainer ollama = new OllamaContainer("ollama/ollama:0.1.48")
			.withReuse(true)
			.withCreateContainerCmdModifier(cmd ->
				{
					cmd.withBinds(Bind.parse("ollama:/root/.ollama"));
					cmd.withPortBindings(new PortBinding(Ports.Binding.bindPort(11434), new ExposedPort(11434)));
				});


	@PostConstruct
	public void start() throws IOException, InterruptedException {
		Logger.getGlobal().info("Starting Ollama container and loading model.");
		ollama.start();
		ollama.execInContainer("ollama", "pull", "mistral");
	}

}
