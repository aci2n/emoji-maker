FROM openjdk:16-alpine
RUN apk add chromium
RUN apk add ffmpeg
RUN apk add nodejs
RUN apk add npm
RUN apk add curl
RUN apk add git

WORKDIR /emoji-maker/deps/tgs-to-gif
RUN git clone https://github.com/ed-asriyan/tgs-to-gif .
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true
RUN npm install

WORKDIR /emoji-maker
COPY ./src ./src
RUN javac -d out -cp ./src ./src/**/*.java
COPY ./src/resources ./out/resources

COPY ./deps/gifski /usr/local/bin

ENV USE_SANDBOX false
ENV CHROMIUM_PATH /usr/bin/chromium-browser
CMD ["java", "-cp", "./out", "launcher.Launcher", "80"]

