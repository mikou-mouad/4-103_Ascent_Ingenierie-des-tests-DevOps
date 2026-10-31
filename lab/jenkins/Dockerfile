# Jenkins du lab : Java 21, Maven, client Docker, plugins et configuration « as code »
FROM jenkins/jenkins:2.580.2-lts-jdk21

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends maven jq curl ca-certificates \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && echo "deb [signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
      > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
 && rm -rf /var/lib/apt/lists/*
USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /var/jenkins_conf/casc.yaml
# Pas d'assistant d'installation. CSP assouplie pour afficher les rapports HTML (Cucumber, JaCoCo) :
# acceptable sur un lab, pas sur un Jenkins partagé.
ENV CASC_JENKINS_CONFIG=/var/jenkins_conf/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dhudson.model.DirectoryBrowserSupport.CSP="
