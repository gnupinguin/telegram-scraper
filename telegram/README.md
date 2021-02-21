Required libs to run:
https://tdlib.github.io/td/build.html?language=Java

```shell script
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install make git zlib1g-dev libssl-dev gperf php cmake default-jdk g++
git clone https://github.com/tdlib/td.git
cd td
git checkout v1.6.0
```

Library itself:
https://github.com/tdlib/td/tree/master/example/java

To run do not forget:
```
-Djava.library.path=td/example/java/bin
```