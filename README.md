# Minimal Chat application using Spring AI

This project is a Java application that integrates a Chat UI powered by Ollama using Vaadin UI. It provides a responsive interface for interacting with Ollama's conversational models.

<img width="504" alt="vaadin-spring-ai-open-ai-chat" src="https://github.com/samie/spring-ai-chat/assets/991105/d7216455-f28e-4d34-8cd0-f7f1074f6087">

## Features
- Full-stack application with Vaadin frontend and Spring Boot backend.
- Real-time chatting capability using local Ollama in Docker container.
- Dynamic message streaming and display of Markdown.

## Prerequisites
- Docker container for running ollama models
- JDK 17 or later
- Maven 3.6 or later

## Running the Application

Install and run ollama in localhost:11434
```
docker run -d -v ollama:/root/.ollama -p 11434:11434  --name ollama ollama/ollama
```

Install ollama 'mistral' model (used by the web app by default)
```
docker exec ollama ollama pull mistral
```

Optional: check that everything works using curl:
```
curl http://localhost:11434/api/chat -d '{"model": "mistral", "messages": [{"role": "user", "content": "is black darker than white?"}], "stream":false}'
```


Run the web application in [localhost:8080](http://localhost:8080/):
```
mvn spring-boot:run
```
