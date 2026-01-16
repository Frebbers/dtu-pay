This is a little tool to observe the messages being exchanged between a client and a server.

You call it via "tcpmon", if it is your path, or "./tcpmon" if started directly from the directory.

Tcpmom is a Java program provided by the axis.jar file. This mean, it is possible to call the Java class directly with "java -cp axis.jar" org.apache.axis.utils.tcpmon" without using the shell script

When the client calls a URL of the form http://host:port/path, tcpmon acts as the middle man. Tcpmon listens to a free port on localhost, like 8008, and is set to forward messages to the target hostname and target port number. Here host and port. Then the client is redirected to http://localhost:8008/path.

For example, if your client calls http://localhost:8080/hello, then you can create a tcp monitor listening to port 8008 and forwarding it to port 8080 on localhost, and your client would have to be changed to call http://localhost:8008/hello

The TCP monitor is a nice tool to check, if the messages being exchanged are correct, or if you are unsure what message are exchanged exactly. Or if you just want to now how the protocol messages look like.

The tool does not work together with https.

An alternative option is to use Wireshark (https://www.wireshark.org/). While it does not require the client to use a different URL, it is more complex to use.

