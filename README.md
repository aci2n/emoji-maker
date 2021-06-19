# EmojiMaker
Server program that accepts a `.tgs` Telegram sticker and generates a Discord-compatible `.gif`.

### Requirements
The following programs should be in the `PATH` environment variable:
- node
- npm
- gifski

### Setup
```shell
$ git clone https://github.com/aci2n/emoji-maker.git
$ cd emoji-maker
$ git submodule init
$ git submodule update
$ cd deps/renderer
$ npm install
```

### Docker
```shell
$ docker build -t emoji-maker .
$ docker run -p ${PORT}:80 emoji-maker
```


##### hola santu