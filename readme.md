# Dictionary Simulation

## Development Environment
* java version 1.8
* modules: import server, client, utils as 3 modules, server and client depend on utils.
* libraries: import lib, all 3 modules depend on lib.

## Run JAR
run with default arguments:
```
# server
java -jar DictionaryServer.jar

# client
java -jar DictionaryClient.jar
```

run with custom arguments:
```
# server
java -jar DictionaryServer.jar <port> <dictionary-file>

# client
java -jar DictionaryClient.jar <server-address> <port>
```
