#base image (jdk instead of jre to low a size of an image):
FROM openjdk:17
#copy a jar file from target folder to the container:
COPY target/*.jar app.jar
#when container is starting up, it will run an entrypoints commands:
ENTRYPOINT ["java","-jar","/app.jar"]


### Installing:
# to build a docker file:
# cd .\api-gateway\
# -t stands for tag name, dot "." stands for present directory (location):
# docker build -t apigateway-dockerfile .



