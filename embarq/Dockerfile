# Image d'Embarq : on emballe le jar déjà construit et testé par le pipeline.
# mvn package d'abord, puis : docker build -t embarq:<version> .
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 embarq
USER 10001
WORKDIR /app
COPY target/embarq.jar embarq.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "embarq.jar"]
