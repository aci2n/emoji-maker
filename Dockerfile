FROM openjdk:16-alpine
RUN apk add chromium
RUN apk add ffmpeg
RUN apk add nodejs
RUN apk add npm
RUN apk add curl
RUN apk add git
RUN apk add cargo
RUN cargo install gifski

WORKDIR /emoji-maker/deps/tgs-to-gif
RUN git clone https://github.com/ed-asriyan/tgs-to-gif .
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true
RUN npm install

WORKDIR /emoji-maker
COPY ./src/java ./src/java
RUN javac -d out -cp ./src/java ./src/java/**/*.java
RUN rm -rf ./src
COPY ./src/resources ./out

ENV USE_SANDBOX false
ENV CHROMIUM_PATH /usr/bin/chromium-browser
ENV PATH /root/.cargo/bin:${PATH}
CMD ["java", "-cp", "./out", "launcher.Launcher", "80"]

