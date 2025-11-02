FROM tomcat:9.0.90-jdk8-temurin-jammy

# Build the webapp into a WAR and deploy as ROOT.war

# install bash (some base images may use sh only) and ensure jar is available (provided by JDK)
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy webapp and sources into a temp build dir
COPY ./webapp /tmp/webapp
COPY ./src /tmp/src
COPY ./server.xml /usr/local/tomcat/conf/server.xml

# Compile Java sources into classes directory
RUN mkdir -p /tmp/war/WEB-INF/classes && \
	javac -cp /usr/local/tomcat/lib/servlet-api.jar -d /tmp/war/WEB-INF/classes /tmp/src/*.java

# Copy webapp static files into war dir
RUN cp -R /tmp/webapp/* /tmp/war/ && \
	# Ensure WEB-INF exists and move compiled classes there
	mkdir -p /tmp/war/WEB-INF && cp -R /tmp/war/WEB-INF/classes /tmp/war/WEB-INF/classes || true

# Create ROOT.war from /tmp/war contents
RUN cd /tmp/war && jar cf /usr/local/tomcat/webapps/ROOT.war .

# Cleanup build artifacts
RUN rm -rf /tmp/src /tmp/webapp /tmp/war

EXPOSE 8080
