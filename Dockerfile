FROM maven:3.5

WORKDIR /project

CMD mvn clean package -Dmaven.repo.local=/project/.m2