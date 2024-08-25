# ZeroMQ CLI

`zeromq` is a lightweight command line development tool for interacting with ZeroMQ peers using various socket types. It
is written in Kotlin and requires an installation of the Java Runtime Environment (JRE) to run (version 17+).

```console
$ zeromq --pub tcp://localhost:5555
HTTP Port: 8080
CONN - Listening
CONN - Client connected
PUB  - foo : {"message": "Hello World!"}
```

```console
$ zeromq --sub tcp://localhost:5555
CONN - Connected
SUB  - foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}'
```

### Multiple Endpoints

Multiple endpoint options can be specified to set up multiple ZeroMQ sockets.

```console
$ zeromq --pub tcp://localhost:5555 --router tcp://localhost:5556
HTTP Port: 8080
CONN [tcp://localhost:5555] - Listening
CONN [tcp://localhost:5556] - Listening
CONN [tcp://localhost:5555] - Client connected
CONN [tcp://localhost:5556] - Client connected
X    [*] - foo : {"message": "Hello World!"}
```

```console
$ zeromq --sub tcp://localhost:5555 --dealer tcp://localhost:5556 --http-port 8081 
HTTP Port: 8081
Auto-generated client id: 30c8c7e72acb
CONN [tcp://localhost:5555] - Connected
CONN [tcp://localhost:5556] - Connected
SUB  [tcp://localhost:5555] - foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}'
```

### Named Endpoints

Endpoints can be named using a name prefix followed by a colon.

```console
$ zeromq --pub pub1:tcp://localhost:5555 --router router2:tcp://localhost:5556
HTTP Port: 8080
CONN [pub1] - Listening
CONN [router2] - Listening
CONN [pub1] - Client connected
CONN [router2] - Client connected
X    [*] - foo : {"message": "Hello World!"}
```

```console
$ zeromq --sub sub1:tcp://localhost:5555 --dealer dealer2:tcp://localhost:5556 --http-port 8081 
HTTP Port: 8081
CONN [sub1] - Connected
CONN [dealer2] - Connected
SUB  [sub1] - foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}'
```

### Sending Messages

For sending-capable endpoints, messages can be sent using a simple HTTP interface - any POST request will be interpreted
as a message to publish or send, with the request body being the message payload and the URL path used as the
destination. The meaning of destination depends on the socket type:
 - PUB: message topic.
 - ROUTER: client id to send to.
 - DEALER: ignored.

The HTTP interface will be created on port 8080 by default. This port can be modified using the -p|--http-post option.

```console
# Publish or send a message to destination foo with payload {"message": "Hello World!"}
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}' 
```

### Limiting Messages By Endpoint Name

By default, messages will be sent to all sending-capable endpoints. To limit the message to a specific endpoint, the
endpoint name can be used as a prefix in the HTTP url.

```console
$ zeromq --pub pub1:tcp://localhost:5555 --pub pub2:tcp://localhost:5556
HTTP Port: 8080
CONN [pub1] - Listening
CONN [pub2] - Listening
PUB  [pub1] - foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/pub1:foo --json '{"message": "Hello World!"}' 
```
