# Peer Processing
[![Build Status](https://travis-ci.com/IncPlusPlus/NetProgFinalProject.svg?branch=master)](https://travis-ci.com/IncPlusPlus/NetProgFinalProject)
[![codecov](https://codecov.io/gh/IncPlusPlus/NetProgFinalProject/branch/master/graph/badge.svg)](https://codecov.io/gh/IncPlusPlus/NetProgFinalProject)
[![Release](https://jitpack.io/v/IncPlusPlus/NetProgFinalProject.svg)](https://jitpack.io/#IncPlusPlus/NetProgFinalProject)

## Description

This is the final project for Network Programming (COMP-2100). The goal is to create a prorotype distributed processing system. A main server will receive math requests which it will offload onto a pool of slaves. They will then process this request and send the results back to the client via the server.

## Deliverables

- A client class that can send groups of math commands
- A server class that can distribute commands to slaves
- A slave class that can process various commands

## Features 

Features in order of priority
- [x] The server can communicate with a client
- [x] The server can communicate with slave nodes
- [x] A user can send individual math expressions to be processed.
- [ ] An API user can provide some sort of list of commands to the server which will be processed and returned
- [ ] The server can offload individual commands to slaves and send back the aggregate result to the client
- [ ] Have the slaves each send a constant heartbeat to the server which will also tell the server the utilization of the slave. The server will then determine how many tasks to give to each slave to optimize utilization.
- [ ] The commands can be sophisticated and invoked using reflection (likely won’t happen)
- [ ] The tasks can be transported and executed using bytecode manipulation (way too ambitious)
- [ ] The server will be able to deal with big matrices.

## Team members

* Ryan Cloherty ([@IncPlusPlus](https://github.com/IncPlusPlus)), Team Lead, Developer
* Joey Demeo([@demeoj1](https://github.com/demeoj1)), Developer

## Usage

There are two ways to use this project. The first, is manually through a console window. The second, is as a usable API in your own Java code.

### Console usage

To run this project, clone this repository yourself and follow the steps below. Alternatively, you can just download it with the big green button or [this link](https://github.com/IncPlusPlus/NetProgFinalProject/archive/master.zip). Extract and open the `NetProgFinalProject-master` folder from the zip if you chose to download this repo.

If you see `XYZ.bat` named in these steps, use `XYZ.sh` instead if you are on Mac OS or Linux.

1. Run `server.bat` (henceforth referred to as "the server") (at the moment, it doesn't prompt for a port and defaults to 1234). Give it some time as it compiles for the first time.
1. Run `client.bat` (henceforth referred to as "the client(s)") and enter the IP and port when prompted. As for what IP and port to enter, that will be displayed when `server.bat` is done starting.
1. Run `slave.bat` (henceforth referred to as "the slave(s)") and enter the same server information as you did for `client.bat`.
1. If all goes well, you'll see information about these connections being established in the server's log. At this point, you can now enter simple math expressions into the console of the client. Below are some example expressions.
    - 1+1
    - log(1)
    - 89^2
    - sqrt(2209)

### API Usage

The Java library portion of this project is in a very rough state and is merely for demo purposes so please don't use it to calculate rocket trajectories!

To use this as a library in your own project, you will have to clone this project and use Maven to generate your own artifacts. Once you have the artifact as a JAR, you can simply include that JAR as a dependency. Instructions for this, whether or not you're using maven, will not be presented here as it is out of scope for this README. JARs will be available for download after they start being hosted at JitPack.

Currently, the only usable APIs are:
- The constructor for `Slave`
- The constructor for `Client`
- `int Server.start(int port)`
- `int Server.start(int port, boolean verbose)`
- The `init()` and `begin()` methods for `Slave` and `Client`
- `Client`'s `FutureTask<BigDecimal> evaluateExpression(String expression)`

This list will disappear as soon as JavaDoc is hosted somewhere like JitPack.