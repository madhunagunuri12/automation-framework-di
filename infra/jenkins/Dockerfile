FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian bookworm stable" > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Ensure a stable java command exists for jenkins-plugin-cli wrapper.
RUN bash -lc 'set -euo pipefail; \
  if command -v java >/dev/null 2>&1; then \
    echo "java already available at: $(command -v java)"; \
  elif [ -x /opt/java/openjdk/bin/java ]; then \
    ln -sf /opt/java/openjdk/bin/java /usr/bin/java; \
  elif [ -x /usr/local/openjdk-21/bin/java ]; then \
    ln -sf /usr/local/openjdk-21/bin/java /usr/bin/java; \
  else \
    echo "ERROR: Java runtime not found in expected locations."; \
    ls -la /opt || true; \
    ls -la /opt/java || true; \
    ls -la /usr/local || true; \
    exit 1; \
  fi; \
  java -version'

ENV JENKINS_UC=https://updates.jenkins.io \
    JENKINS_UC_EXPERIMENTAL=https://updates.jenkins.io/experimental \
    JENKINS_INCREMENTALS_REPO_MIRROR=https://repo.jenkins-ci.org/incrementals \
    JENKINS_PLUGIN_INFO=https://updates.jenkins.io/current/plugin-versions.json

COPY infra/jenkins/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN chown jenkins:jenkins /usr/share/jenkins/ref/plugins.txt

USER jenkins

RUN bash -lc 'set -euo pipefail; \
  export PATH="/opt/java/openjdk/bin:/usr/local/openjdk-21/bin:${PATH}"; \
  for attempt in 1 2 3 4 5; do \
    echo "Installing Jenkins plugins (attempt ${attempt}/5)..."; \
    if jenkins-plugin-cli --verbose --plugin-file /usr/share/jenkins/ref/plugins.txt; then \
      exit 0; \
    fi; \
    if [ "${attempt}" -lt 5 ]; then \
      sleep $((attempt * 20)); \
    fi; \
  done; \
  echo "Plugin installation failed after 5 attempts."; \
  exit 1'
