FROM java:8

RUN apt-get update && apt-get install -y \
	git

COPY target/fwb-webapi.jar /tmp/fwb-webapi.jar
CMD java -jar /tmp/fwb-webapi.jar
