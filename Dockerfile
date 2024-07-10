FROM ollama/ollama:0.2.1 AS base

# Install Java runtime to base using openjdk:23-ea-21-jdk-slim layers
RUN /bin/sh -c set -eux; apt-get update; apt-get upgrade -y; apt-get install -y --no-install-recommends ca-certificates p11-kit wget sudo; rm -rf /var/lib/apt/lists/*
ENV JAVA_HOME=/usr/local/openjdk-21
ENV LANG=C.UTF-8
ENV JAVA_VERSION=21
ENV PATH=/usr/local/openjdk-21/bin:${PATH}
RUN /bin/sh -c set -eux; arch="$(dpkg --print-architecture)"; case "$arch" in 'amd64') downloadUrl='https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz'; downloadSha256='a30c454a9bef8f46d5f1bf3122830014a8fbe7ac03b5f8729bc3add4b92a1d0a'; ;; 'arm64') downloadUrl='https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-aarch64_bin.tar.gz'; downloadSha256='e8f4ed1a69815ddf56d7da365116eefc1e5a1159396dffee3dd21616a86d5d28'; ;; *) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; esac; savedAptMark="$(apt-mark showmanual)"; apt-get update; apt-get install -y --no-install-recommends wget ; rm -rf /var/lib/apt/lists/*; wget --progress=dot:giga -O openjdk.tgz "$downloadUrl"; echo "$downloadSha256 *openjdk.tgz" | sha256sum --strict --check -; mkdir -p "$JAVA_HOME"; tar --extract --file openjdk.tgz --directory "$JAVA_HOME" --strip-components 1 --no-same-owner ; rm openjdk.tgz*; apt-mark auto '.*' > /dev/null; [ -z "$savedAptMark" ] || apt-mark manual $savedAptMark > /dev/null; apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false; { echo '#!/usr/bin/env bash'; echo 'set -Eeuo pipefail'; echo 'trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth "$JAVA_HOME/lib/security/cacerts"'; } > /etc/ca-certificates/update.d/docker-openjdk; chmod +x /etc/ca-certificates/update.d/docker-openjdk; /etc/ca-certificates/update.d/docker-openjdk; find "$JAVA_HOME/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf; ldconfig; java -Xshare:dump; fileEncoding="$(echo 'System.out.println(System.getProperty("file.encoding"))' | jshell -s -)"; [ "$fileEncoding" = 'UTF-8' ]; rm -rf ~/.java; javac --version; java --version

# Build stage using separate openjdk:21 environment
FROM openjdk:21 AS builder

# Update package lists and Install Maven
RUN microdnf update -y && \
    microdnf install -y maven && \
    microdnf clean all

WORKDIR /usr/src/app

# Copy rest of the dependencies from pom.xml.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY . .

# Do the build using production profile
RUN mvn clean install -Pproduction

# Runtime stage from base
FROM base AS runtime

# Prepare app as workdir
RUN useradd -m appuser
RUN mkdir -p /app && \
    chown -R appuser /app
WORKDIR /app

# Copy the binary from builder and startup script to start both ollama and webapp
COPY --chown=appuser --from=builder /usr/src/app/target/*.jar /app/app.jar
COPY --chown=appuser --chmod=722 startup.sh /app/startup.sh

EXPOSE 8080
ENTRYPOINT ["/app/startup.sh"]