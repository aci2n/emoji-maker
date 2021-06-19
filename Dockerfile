FROM openjdk:16-alpine
RUN apk add chromium
RUN apk add nodejs
RUN apk add npm
RUN apk add git
RUN apk add cargo
RUN cargo install gifski

WORKDIR /emoji-maker/deps/renderer
RUN git clone https://github.com/aci2n/tgs-to-gif .
RUN git checkout ba45558fca31716b210b190e9ea93c6460a9fa77
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true
RUN npm install

WORKDIR /emoji-maker
COPY src/java java
RUN javac -d bin -cp java java/**/*.java
RUN rm -rf java
COPY src/resources bin

ENV USE_SANDBOX false
ENV CHROMIUM_PATH /usr/bin/chromium-browser
ENV GIFSKI_PATH /root/.cargo/bin/gifski
CMD ["java", "-cp", "bin", "launcher.Launcher", "80"]

