FROM mcr.microsoft.com/devcontainers/java:21

RUN apt-get update && \
    apt-get install -y \
      curl gnupg2 dirmngr \
      build-essential libssl-dev libreadline-dev zlib1g-dev \
      postgresql-client git && \
    gpg --keyserver hkp://keyserver.ubuntu.com --recv-keys \
      409B6B1796C275462A1703113804BB82D39DC0E3 \
      7D2BAF1CF37B13E2069D6956105BD0E739499BDB && \
    curl -sSL https://get.rvm.io | bash -s stable && \
    bash -lc "rvm install 3.2.3 && rvm use 3.2.3 --default && gem install bundler kamal"

SHELL ["/bin/bash", "-lc"]

RUN apt-get update && \
    apt-get install -y maven

WORKDIR /workspace