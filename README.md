# Qabel documentation
For the documentation take a look at the [wiki](https://github.com/Qabel/qabel-doc/wiki/Table-of-contents) in our documentation [repository](https://github.com/Qabel/qabel-doc).

# Qabel Core

[![Build Status](https://travis-ci.org/Qabel/qabel-core.svg?branch=master)](https://travis-ci.org/Qabel/qabel-core)

Qabel Core is the core component of a Qabel Client.

## building source

0. Make sure you have a working [git client](http://git-scm.com/) installed

0. clone the source
   ```
   git clone https://github.com/Qabel/qabel-core.git
   cd qabel-core
   git submodule init
   git submodule update
   ```
   
0. build the project
   ```
   ./gradlew build
   ```

## start developing

0. It is recommended to use Eclipse with Qabel-Core. Make sure you have
   [Eclipse](https://www.eclipse.org/home/index.php) with
   [Gradle Support](https://github.com/spring-projects/eclipse-integration-gradle/) up and running. Also you need a [git client](http://git-scm.com/)

0. clone the source
   ```
   git clone https://github.com/Qabel/qabel-core.git
   ```
   
0. Fire up Eclipse and import the project using ```File``` -> ```Import...``` -> ```Gradle Project```.


## testing

You need a qabel-drop server at port 8000, a qabel-storage server at port 6000
and a qabel-accounting server at port 9696
(running with `./manage.py testserver testdata.json --addrport 9696`)
