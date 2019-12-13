# Peer Processing
[![Build Status](https://travis-ci.com/IncPlusPlus/NetProgFinalProject.svg?branch=master)](https://travis-ci.com/IncPlusPlus/NetProgFinalProject)
[![codecov](https://codecov.io/gh/IncPlusPlus/NetProgFinalProject/branch/master/graph/badge.svg)](https://codecov.io/gh/IncPlusPlus/NetProgFinalProject)
[![Release](https://jitpack.io/v/IncPlusPlus/NetProgFinalProject.svg)](https://jitpack.io/#IncPlusPlus/NetProgFinalProject)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=IncPlusPlus/NetProgFinalProject)](https://dependabot.com)

## Description

This is the final project for Network Programming (COMP-2100). The goal is to create a prototype distributed processing system. A main server will receive math requests which it will offload onto a pool of slaves. They will then process this request and send the results back to the client via the server.

## Getting Started

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

To use this as a library in your own project, you may either add it by importing it using your build tool of choice on [JitPack](https://jitpack.io/#IncPlusPlus/NetProgFinalProject) or by downloading [the JAR from the releases page](https://github.com/IncPlusPlus/NetProgFinalProject/releases/latest) and adding it to your classpath.

There aren't many functions available yet and it's certainly not polished. However, there is a [JavaDoc available online at JitPack](https://jitpack.io/com/github/IncPlusPlus/NetProgFinalProject/latest/javadoc/).

## Features 

Features in order of priority
- [x] The server can communicate with a client
- [x] The server can communicate with slave nodes
- [x] A user can send individual math expressions to be processed.
- [x] An API user can provide some sort of list of commands to the server which will be processed and returned
- [x] The server can offload individual commands to slaves and send back the aggregate result to the client
- [x] Have the server offload work onto the least busy slave
- [x] The server will be able to deal with big matrices.
- [ ] The commands can be sophisticated and invoked using reflection (likely won’t happen (didn't happen))
- [ ] The tasks can be transported and executed using bytecode manipulation (way too ambitious (**way too ambitious**))

## Team members

* Ryan Cloherty ([@IncPlusPlus](https://github.com/IncPlusPlus)), Team Lead, Developer
* Joey Demeo([@demeoj1](https://github.com/demeoj1)), Developer

## Contributing

Pull requests are welcome but not expected. If you make one, please branch from `develop` and make the base of the PR `develop`.
This project **_strictly_** conforms to [Google Java Style](https://google.github.io/styleguide/javaguide.html).
This project also conforms to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).
