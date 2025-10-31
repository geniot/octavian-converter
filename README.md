`docker build -t geniot/octavian-converter:latest -f Dockerfile .`

`docker rm -f octavian-converter`

`docker run -p 8002:8002 --name octavian-converter -d --rm --cap-add SYS_ADMIN --device /dev/fuse geniot/octavian-converter:latest`