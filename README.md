# Permuter Project

## Basics

Uses the SimpleFramework (see resources section) to create an HTTP server that
runs classes from the `com.newsbank.permuter.permutation` package by name, i.e.
if the URL is `http://{server}:{port}/yadda{.format}` it is expected that there 
will be a class called `com.newsbank.permuter.permutation.yadda`that implements
the `com.newsbank.permuter.permutation.Permutation` interface.

Each Permutation class receives the body of the POST request as a string along
with a "format", which is any extension that was supplied at the end of the URL
invoking the class.

In return each Permutation class returns a `com.newsbank.permuter.PermutedResult`
that encapsulates the return HTTP content-type and data.

## Building

The Ant build.xml file responds to the following:

- clean - removes the "permuter" directory
- compile - compiles the sources into classes
- jar	- creates permuter.jar from the compiled classes
- distro - creates a distribution directory using the jar file, dependent
libraries, and a simple startup script `permuter-server.sh`
- sftp-distro - sends the distro to a specified server via sftp

## Resources
- [Simpleframework](http://www.simpleframework.org) Note that this is a royal 
pain to make into jar files if you're not familiar with maven. Ended up using
the ant build files and manually adding the project jar files to the dependent
sub-projects - i.e. `transport` needs `core`, and `http` needs `core` and 
`transport`.

## Participants
- Colleen O'Brien - cobrian@newsbank.com
- Tatiana Fernandes - tfernandes@newsbank.com
- Matthew E. Axsom - maxsom@newsbank.com
