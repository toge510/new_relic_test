FROM httpd:2.4.55

# Install required packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libtool \
    autoconf \
    wget \
    sed \
    apache2-dev \
    && rm -r /var/lib/apt/lists/*

# Download and build mod_jk
RUN wget https://archive.apache.org/dist/tomcat/tomcat-connectors/jk/tomcat-connectors-1.2.48-src.tar.gz -O /tmp/mod_jk.tar.gz \
    && tar -xzf /tmp/mod_jk.tar.gz -C /usr/src \
    && cd /usr/src/tomcat-connectors-1.2.48-src/native \
    && ./configure --with-apxs=`which apxs` \
    && make \
    && make install \
    && rm /tmp/mod_jk.tar.gz

# Clean up build dependencies
RUN apt-get purge -y --auto-remove build-essential libtool autoconf