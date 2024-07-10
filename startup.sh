#!/bin/sh

# Ollama runs as root on the background
ollama serve &

# Application runs as appuser
sudo -u appuser -s --preserve-env=JAVA_HOME,JAVA_VERSION,PATH java -jar /app/app.jar