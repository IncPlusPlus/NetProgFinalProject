# Peer Processing

## Description

This is the final project for Network Programming (COMP-2100). The goal is to create a prorotype distributed processing system. A main server will receive math requests which it will offload onto a pool of slaves. They will then process this request and send the results back to the client via the server.

## Deliverables

- A client class that can send groups of math commands
- A server class that can distribute commands to slaves
- A slave class that can process various commands

## Features 
Features in order of priority
1.	The server can communicate with a client
1.	The server can communicate with slave nodes
1.	A client can provide some sort of list of commands to the server which will be processed and returned
1.	The server can offload individual commands to slaves and send back the aggregate result to the client
1.	Have the slaves each send a constant heartbeat to the server which will also tell the server the utilization of the slave. The server will then determine how many tasks to give to each slave to optimize utilization.
1.	The commands can be sophisticated and invoked using reflection (likely won’t happen)
1.	The tasks can be transported and executed using bytecode manipulation (way too ambitious)
1.  The server will be able to deal with big matrices.

## Team members

* Ryan Cloherty ([@IncPlusPlus](https://github.com/IncPlusPlus)), Team Lead, Developer
* Joey Demeo([@demeoj1](https://github.com/demeoj1)), Developer

## Usage

Coming soon...