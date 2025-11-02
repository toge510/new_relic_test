FROM tomcat:9.0.90-jdk8-temurin-jammy

# Remove the default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy the web application
COPY ./webapp /usr/local/tomcat/webapps/ROOT

# Copy the custom server.xml
COPY ./server.xml /usr/local/tomcat/conf/server.xml

# Copy the servlet source code
COPY ./src /tmp/src

# Compile the servlet
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes
RUN javac -cp /usr/local/tomcat/lib/servlet-api.jar -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes /tmp/src/WeatherServlet.java

# Clean up the source code
RUN rm -rf /tmp/src
