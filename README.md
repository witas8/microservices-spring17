-	Microservices Architecture
 
Dockerized microservices application that uses ordered list of events (Kafka), Circuit Breaker Pattern (Resiliance4J), tracking mechanism (Zipkin), authentication and authorization operations (Keycloak), monitoring (Prometheus and Grafana). Data are stored in PostgreSQL and MongoDB.

-	Testcontainers
In the page https://www.testcontainers.org/ go to home, modules, test framework integration
Testcontainers are going to be used for integration testing purpose.
The Bill of Materials (BOM) of test containers are consisted in a dependency management, because in the project many modules (Kafka, MongoDB) will be tested by test containers. 
Set a version of test containers only in one place (where the BOM is defined).
With implementing a module of a database then there is no need to specify connection in the test resources directory.

-	Parent project
Create a parent object – add modules and delete a source folder.
We can start creating project from the parent perspective or adding existing projects.
If the second choice was chosen then, paste to the POM of a parent object this configuration:
 
Moreover, paste the build tag at the end in the POM file of the root project.
If we use test containers, then paste a dependency management in a POM file of the root project in a purpose to do not repeat ourselves in every project module. In this case the dependency management will be propagate from parent to the children.
At the end to check if everything was properly set just do maven clean verify. The goal:
 

-	Inter Process Communication
Communication inside the project is a synchronous (service waits for a response from a service): 
Add a bean configuration where the Web Client will be initialized:
 
Example of sync communication – service1 (order) saves data if it exists in service2 (inventory)
 






-	Service Discovery pattern
To make a call to the service instance that is currently working (many ports or hosts could be available in a production cloud). Hence, the service will call first the Discover Server (module in the project) to ask the working another service working host. It is called Load Balancing.
The purpose is to avoid a hardcoding URLs hosts and ports. When server is stopped then other services have automatically registered previous running service host that it communicated with.
   
- Remember that server (project module) has to be run before clients (project service modules)
- The server needs be deployed in a cloud to be always available from anywhere.
For example Netflix Eureka - https://spring.io/projects/spring-cloud-netflix
- Add to the POM file in the server module (https://start.spring.io/ add dependency and explore)
 
- Add to the POM file in the parent project:
 
- Add to the POM file in each service module:
 
- Add on top of each main classes of each service modules annotation @EnableEurekaClient
- Add in application properties of each service modules: eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
spring.application.name=product-service # product-service is your service name
- Check if works - http://localhost:8761/
- To create multiple instances of the service with different ports, then change port value in application properties to 0 (previously for ex. 8081) to make spring choose random port. 
Then, edit configuration -> modify options -> allow multiple instances.
Then we can use in our code http://inventory-service/api/inventory instead of http://localhost:8082/api/inventory
- Finally, to let a service to call another service via one of the working host add @LoadBalanced and do some changes in a WebClientConfig and service classes in a service module
-	API Gateway (Spring Cloud Gateway)
It is responsible to route the request (routing based on request headers) from users to the corresponding services. It works like gatekeeper and take care of an authentication, security, load balancing, SSL termination. Go to: https://spring.io/projects/spring-cloud-gateway
The goal is to call application via one URL with one port ex. 8080, but a user will automatically be transferred to the appropriate service with different hosted URL and port.
 
- Create new module in the parent and add dependency
 
- Define routes (rules or routing scheme) for each server and service like:
 
At the end there must be defined a server gateway with the endpoint that was defined by the Spring Cloud, so not by using low balancer (lb), but with ex. localhost URL.


- 	Postman 
Generate token details with OAuth 2.0 authorization and Request Headers (but before postman finish the Security configuration (described below). Every time we create new docker container, new client secret is created as well ( http://localhost:8080/ )
   
-	Security
We can outsource an authentication and authorization operations to the KEYCLOAK server, to avoid creating configuration by hand. Install the Keycloak through Docker. Change port to 8181:8080 to make the container run on 8181 port when it will be working on my machine.
https://www.keycloak.org/getting-started/getting-started-docker 
- Run docker container to connect to the KEYCLOACK on localhost http://localhost:8181/
- Login (admin, admin) and go to admin console to create a realm to group all clients into single logical entity (realm) and enable client authentication and authorization options. Then go to Real Settings -> OpenID Endpoint Configuration -> copy an issuer endpoint.
- In Gateway API project module add dependencies in the POM file:
 
- In Gateway API module extend an application property of link to my KEYCLOAK client, example:
spring.security.oauth2.resourceserver.jwt.issuer-uri= http://localhost:8181/realms/spring-boot-mircoservices-java17-realm to enable communication to my security config from KEYCLOACK.
- Create configuration bean/class that with authorization filter chain in the API Gateway module:
   
- We need to enable a basic authentication in discovery server, because we blocked permission in a gateway project and we do not use a token to send any response, but just we need to view the endpoint in the browser. Add in discovery server module: dependency, config class. 
Tip: leave login and password without using a values and application properties (it works then).
Also change application properties (one line login, pass) in each module (gateway too):











-	Circuit Breaker Pattern
We can use Resiliance4J library and Spring Cloud Circuit Breaker to implement the pattern logic. The purpose is to get a robust (resilient on problems with database, slow responding, network issue) communication (mainly synchronous) between services, because there is no sense to keep calling not working service. There are states – open (not working), half open (slow working, half responding), close (change to closed if a request is executed successfully). If communication did not go well then status is still open, then we run an error message or fallback logic. 
 
 
In a controller of serviceA add above a method (not class) annotation
@CircuitBreaker(name=”x”, fallbackMethod = “myNewMethod)
where x is the name used in a properties after instances (ex. inventory as in above picture)
To monitor the status, state and behavior of service that makes calls go to (service port): http://localhost:8081/actuator/health
Then add Retry annotation when there is a slow response or no response form another service
 
-	Distributed Tracing
Use Spring Cloud Sleuth for tracking mechanism and Zipkin for an UI purpose https://zipkin.io/.
Download Zipkin by: docker run -d -p 9411:9411 openzipkin/zipkin (remember to run container).
Track requests to monitor issues of breaking resilience and performance problems in logs.
 
Add to every service and server dependency to POM files and application properties:
   
To monitor logs, go to http://localhost:9411/zipkin and press run query button.
There are spans created automatically, but we can create our additional with the Tracer library.
   


-	Kafka
Kafka is an open source, distributed streaming platform that allows to develop a real-time event-driven architecture application. There are continuously produced and consumed streams of data records. The produced records are replicated and partitioned to make apps to be simultaneously used without lag in performance and keeps the order of occurrence.
- It is fast, highly efficient, high accurate, resilient and fault tolerant, horizontally scaled.
Use case scenario – decoupling (dividing) dependencies, messaging, tracking, data gathering. 
- Producers create data records and pack them to topics. Topics are ordered list of events that are physically stored (for seconds, hours or years). Record has timestamp, key-value, metadata.
Consumers (or consumer group) subscribe, reads, listen those topics in real-time or reads them after saving. Between Producers and Consumer is a Stream API that analyze, aggregate, or transform data and then produce the resulting stream. 
   
- Broker contains a partition. The partition (queue) contains records. Each record has sequential number identify them uniquely called an offset. However, topic is a group of partition that handling the same kind of data. In a broker can be set partition replicas and one leader.
- Kafka works as a transportation mechanism, but it is more than a message broker (as Rabbit), because by Stream API data can transformed and aggregated before they reach the consumer. Consumer reads data in the same order as it was broadcasted, but we can configure as setting a range (offset). Hence, with Kafka we can achieve stateless transformation - filter and map or stateful transformation – aggregation (combine multiple events into a single value) all managed in real-time (low latency).

Kafka in the project:
- We can implement event-driven architecture by using Apache Kafka approach to perform an asynchronous communication (not waiting for the response). Hence, after we receive a request to the service A (producer), we place an event as a message to the Kafka broker that is send to the service B (consumer) that could be for example a notification service (sending SMS, email).
- Copy: https://developer.confluent.io/quickstart/kafka-docker/ and run: docker compose up -d
 



- Create a Kafka Producer ex. producer-service:
Add dependencies and change application properties to apply core Spring concept of 
Kafka-based messaging solution to the service that will work as a producer:
  Then, send a message to Kafka Topic Cluster by adding kafka class to constructor with key-value pai and create a class to wrap a value that will be send via Kafka just for easier serialization.
private final KafkaTemplate<String, MySerializationClass> kafkaTemplate; //OrderPlacedEvent
kafkaTemplate.send("myTopicName", new MyObjectForSerialization(service.valueToBeSend()));

- Create a Kafka Consumer ex. notification-service:
Add all dependencies to POM file (copy from explorer) https://start.spring.io/ and prepare application.properties (producer is changed to consumer and serialization to deserialization):
   
Add @KafkaListener(topics = "thisame_name_defined_in_producer_service") above a method.
The method argument is the same class as defined in producer service MyObjectForSerialization
 




-	Docker
Dockerfile:
(Dockerfile and Dockerfile.layerd part can be skipped, you can go to the JIB plugin part)
Instead of building Dockerfile we could build Dockerfile.layared with defined layers to improve the process of building a docker image by using a Multi State Builds Mechanism to make image size lower. To do so, in terminal type cd.\module-name (ex. cd .\api-gateway\) and then docker build -t apigateway-layered -f Dockerfile.layered .
where -t stands for tag name and -f defines configuration file (by default it is Dockerfile)
and a dot “.” points that location is present directory. But there is a better way (jib).
 
JIB plugin:
Do not create any Dockerfile in each module, just add a plugin to the main project POM 
and run maven - clean, compile and jib build to create images and send them to docker-hub.
Before, login to docker hub profile in terminal: docker login -u=yourlogin -p=yourpassword
Then, use docker compose to download images from docker-hub and run them.
 
Docker compose:
- Where 8080:8000 means port outside:inside the container. Inside means it is defined in application.properties, and outside means it is used in a web browser. 
Where localhost is changed to the container names.
- Prepare a docker compose to run all containers and refer to docker images to dockerhub.
Before send container to docker hub before through jib plugin as was described above.
- Create application-docker.properties to prepare configuration when the service will be run from a container. Add in docker-compose an environment SPRING_PROFILES_ACTIVE=docker. 
In application-docker.properties the app refers to the container name instead of localhost.
- add data.sql files in src/main/resources folder in services that will store data to database
- run by: docker compose up -d where d stands for detached from the terminal (in background)
- To verify type in terminal ex: docker logs -f inventory-service or use docker desktop
- At the end we can login to Keycloak, add a realm (the same name as in the gateway service) and export (last button on left) and copy the content to realm folder in app as realm-export.json 
-	Project verification
1. If we want to contact docker to container with configured gateway service then we need to change localhost to keycloak to generate authorization token (a way not used in a production). Go to C:\Windows\System32\drivers\etc path and in the “hosts” file add 127.0.0.1 keycloak
 
3. Add a new realm in a keycloak with the same realm name that as was defined in the gateway application-docker.properties: spring.security.oauth2.resourceserver.jwt.issuer-uri= http://localhost:8181/realms/spring-boot-mircoservices-java17-realm
2. In Postman change localhost to keycloak as the service was defined in the docker-compose:
http://keycloak:8080/realms/spring-boot-mircoservices-java17-realm/protocol/openid-connect/token 
3. The gateway service port in a docker-compose was exposed to 8181. Hence, the application services’ port (not Keycloak or Zipkin etc.) is now 8181 http://localhost:8181/api/inventory 


-	Monitoring
Use Prometheus and Grafana to monitor services and view status of them in a dashboard. Prometheus gather all metrics from spring boot app and store it in an in-memory database. 
Grafana will visual those gathered data. The Spring Boot Actuator endpoints give access to data.
- Add Prometheus and Spring Boot Actuator dependencies in every service module separately:
  
- Enable actuator endpoints in each module (services)configuration file (application.properties):
management.endpoints.web.exposure.include= Prometheus
- Install Prometheus and Grafana via docker-compose with preexisting image from docker hub
and create a Prometheus folder (not module) and a configuration file prometheus.yaml
In a docker-compose, in a Prometheus volumes part exists a callback to this file:
./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml 
   
- verify if Prometheus works on http://localhost:9090/ and search for logback_events_total and go to status service discovery and targets 
- verify if Grafana works http://localhost:3000/ and add database Prometheus with http://prometheus:9090/ URL. Prometheus is defined as a name of container as well. Then create or import already created by someone dashboard.
