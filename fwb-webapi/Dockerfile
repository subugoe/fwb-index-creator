FROM java:8

RUN apt-get update && apt-get install -y \
	man \
	less \
	vim-nox \
	git \
	sudo \
	mailutils

RUN git config --global alias.lg "log --oneline --all --graph --decorate"
RUN git config --global alias.ss "status -s"
RUN git config --global core.pager "less"
RUN git config --global user.name "Dennis Neumann"
RUN git config --global user.email "neumann@sub.uni-goettingen.de"
RUN git config --global push.default simple

COPY target/fwb-webapi.jar /tmp/fwb-webapi.jar
CMD java -jar /tmp/fwb-webapi.jar
