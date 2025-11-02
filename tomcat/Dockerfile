FROM tomcat:9.0.90-jdk8-temurin-jammy

RUN apt-get update && apt-get install -y bash curl unzip

# install bash (some base images may use sh only) and ensure jar is available (provided by JDK)
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy webapp and sources into a temp build dir
COPY ./webapp /tmp/webapp
COPY ./src /tmp/src
COPY ./server.xml /usr/local/tomcat/conf/server.xml

# Copy any vendored libs (e.g. mysql-connector-java.jar) from repo into build context
# Note: the build context for this service is ./tomcat, so the vendored libs should live in tomcat/lib
# Use relative path within the build context.
COPY ./lib /tmp/build_lib

# Compile Java sources into classes directory
RUN mkdir -p /tmp/war/WEB-INF/classes && \
	javac -cp /usr/local/tomcat/lib/servlet-api.jar -d /tmp/war/WEB-INF/classes /tmp/src/*.java

# Copy webapp static files into war dir
RUN cp -R /tmp/webapp/* /tmp/war/ && \
	# Ensure WEB-INF exists and move compiled classes there
	mkdir -p /tmp/war/WEB-INF && cp -R /tmp/war/WEB-INF/classes /tmp/war/WEB-INF/classes || true

# Prepare WEB-INF/lib and copy any vendored connector into the WAR and Tomcat lib.
# Place the MySQL connector JAR at `tomcat/lib/mysql-connector-java.jar` in the repo to vendor it.
RUN mkdir -p /tmp/war/WEB-INF/lib && \
	# copy any files present in build context lib dir into the WAR lib
	cp -rn /tmp/build_lib/* /tmp/war/WEB-INF/lib/ 2>/dev/null || true && \
	# if connector was provided, also copy to Tomcat's common lib for visibility
	if [ -f /tmp/war/WEB-INF/lib/mysql-connector-java.jar ]; then \
		cp /tmp/war/WEB-INF/lib/mysql-connector-java.jar /usr/local/tomcat/lib/ || true; \
	else \
		echo "mysql-connector-java.jar not found in ./tomcat/lib; attempting to download from Maven Central..."; \
		curl -fSL -o /tmp/war/WEB-INF/lib/mysql-connector-java.jar \
		  https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar && \
		ls -l /tmp/war/WEB-INF/lib/ && \
		cp /tmp/war/WEB-INF/lib/mysql-connector-java.jar /usr/local/tomcat/lib/ && \
		ls -l /usr/local/tomcat/lib/ || \
		echo "Warning: failed to download mysql-connector-java.jar; the JDBC driver will be missing"; \
	fi

# Create ROOT.war from /tmp/war contents
RUN cd /tmp/war && jar cf /usr/local/tomcat/webapps/ROOT.war .

# Install New Relic Java agent
ENV VERSION=8.24.0
RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${VERSION}/newrelic-java-${VERSION}.zip && \
    unzip newrelic-java-${VERSION}.zip -d /usr/local/ && \
    rm newrelic-java-${VERSION}.zip

# Cleanup build artifacts
RUN rm -rf /tmp/src /tmp/webapp /tmp/war

EXPOSE 8080