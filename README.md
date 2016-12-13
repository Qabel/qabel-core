<img align="left" width="0" height="150px" hspace="20"/>
<a href="https://qabel.de" align="left">
	<img src="https://files.qabel.de/img/qabel_logo_orange_preview.png" height="150px" align="left"/>
</a>
<img align="left" width="0" height="150px" hspace="25"/>
**The Qabel Core Library**

This project provides a core library for all Qabel Clients implementing <a href="https://qabel.de"><img alt="Qabel" src="https://files.qabel.de/img/qabel-kl.png" height="18px"/></a>.

<br style="clear: both"/>
<p>
	<a href="#introduction">Introduction</a> |
	<a href="#install">Install</a> |
	<a href="#getting_started">Getting Started</a> |
	<a href="#usage">Usage</a> |
	<a href="#contribution">Contribution</a>
</p>
<br style="clear: both"/>

[![Release](https://img.shields.io/github/release/Qabel/qabel-core.svg)](https://jitpack.io/#Qabel/qabel-core)
[![License](https://img.shields.io/badge/License-QaPL-blue.svg)](https://github.com/Qabel/qabel-core/blob/master/LICENSE)
![Build](https://teamcity.qabel.de/app/rest/builds/buildType:QabelCore_Build/statusIcon.svg)

# Introduction

For a comprehensive documentation of the whole Qabel Platform use https://qabel.de as the main source of information. https://qabel.github.io/docs/ may provide additional technical information.

Qabel consists of multiple Projects:
 * [Qabel Android Client](https://github.com/Qabel/qabel-android)
 * [Qabel Desktop Client](https://github.com/Qabel/qabel-desktop)
 * [Qabel Core](https://github.com/Qabel/qabel-core) is a library that includes the common code between both clients to keep them consistent
 * [Qabel Drop Server](https://github.com/Qabel/qabel-drop) is the target server for drop messages according to the [Qabel Drop Protocol](http://qabel.github.io/docs/Qabel-Protocol-Drop/)
 * [Qabel Accounting Server](https://github.com/Qabel/qabel-accounting) manages Qabel-Accounts that authorize Qabel Box usage according to the [Qabel Box Protocol](http://qabel.github.io/docs/Qabel-Protocol-Box/)
 * [Qabel Block Server](https://github.com/Qabel/qabel-block) serves as the storage backend according to the [Qabel Box Protocol](http://qabel.github.io/docs/Qabel-Protocol-Box/)

# Install

Distributions of the Clients are provided by the [official Qabel website](https://qabel.de) at https://qabel.de/de/download .
Everything below this line describes the usage of the Qabel Core Library for development purposes.

To use the Qabel Core as a library with gradle, add the following to your build.gradle:
```GRADLE
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.Qabel.qabel-core:core:0.19.0'
    compile 'com.github.Qabel.qabel-core:box:0.19.0'
    compile 'com.github.Qabel.qabel-core:chat:0.19.0'

}
```
replace `0.16.1` with the latest release from https://jitpack.io/#Qabel/qabel-core

# <a name="getting_started"></a>Getting started

For a reference build, the core provides a `Vagrantfile` that sets up all requirements and can do a full build.
For a manual setup, take a look at the INSTALL.md.

Vagrant build:
Install Vagrant from [vagrantup](https://www.vagrantup.com/) and then build inside the box
```BASH
vagrant up      # starts the vagrant vm (takes a while because all dependency need to be installed)
vagrant ssh     # enters the vm
cd /vagrant
./gradlew build
```

# Usage

The Qabel-Core is developed in Java 7 and [Kotlin](https://www.kotlinlang.org). The Kotlin plugin is automatically loaded in the
gradle build scripts and there are plugins for IntelliJ based IDEs, Eclipse, Vim and Emacs.
It is recommended that new code is written in Kotlin instead of Java and that the Kotlin standard library is used
instead of the Java standard library where applicable.

Currently, we do not provide a distribution of the Qabel Core Library via Maven Repositories.
Thus, you have to include it via the artifact that you may find after the build at `build/libs/qabel-core-x.y.z.jar` and `build/libs/qabel-box-x.y.z.jar`.
You also need to include the kotlin standard library in your project:

```GROOVY
buildscript {
    ext.kotlin_version = '1.0.2'
}
dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
}
```

Don't forget to include the `curve25519-linux-*.jar` (JNI) and the according libcurve implementation for your system (`build/binaries`).
Qabel does a lot of crypto that is written in C and called via JNI. When launching your java application,
add the directory containing the jars and the C library wo javas library path via `-Djava.library.path=...`.

## Entities

To understand the concepts of Qabel, please read the documentations linked at [Introduction](#Introduction).

### Identity

For most actions, you need an Identity. It consists of a KeyPair, DropURLs and an Alias. You can create one with
```JAVA
DropURL url = new DropURL("https://drop.qabel.de/123456789012345678901234567890123456789012c");
Identity identity = new Identity("My New Identity", new ArrayList<DropURL>(), new QblECKeyPair());
identity.addDrop(url);
```

### Contact

and to communicate with other people, you need your Contact:
```JAVA
Contact contact = new Contact(identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());

String serializedContact = ContactExportImport.exportContact(contact);
Files.write(Paths.get("myNewIdentity.qco"), serializedContact);
```

## Sending drop messages

For wrapper that do this work for you, take a look at the client repositories.
Otherwise sending a drop message is currently pretty complicated:
```JAVA
Identity sender = ...;
Contact receiver = ...;
DropURL dropURL = ...;  // select one dropURL from the receiver contact

// assemble the message
String jsonEncodedMessage = "{msg: \"this is a plaintext message for you\"}";
String messageType = "box_message";     // plaintext message (https://qabel.github.io explains these)
DropMessage message = new DropMessage(identity, jsonEncodedMessage, messageType);

// encrypt the message
BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(message);
final byte[] messageByteArray = binaryMessage.assembleMessageFor(receiver, sender);
HTTPResult<?> dropResult = new DropHTTP().send(dropURL.getUri(), messageByteArray);
if (dropResult.getResponseCode() < 200 || dropResult.getResponseCode() >= 300) {
    // handle failure
}
```

The receiver can then receive the message with
```JAVA
for (DropURL url : identity.getDropUrls()) {
    long messagesSince = 0; // 0 means all messages, this should be increased to the last-modified from the server response
    HTTPResult<Collection<byte[]>> result = new DropHTTP().receiveMessages(url.getUri(), messagesSince);

    if (result.getResponseCode() == 0) {
        // handle failure...
    }

    for (byte[] encryptedMessage : result.getData()) {
        byte binaryFormatVersion = encryptedMessage[0];
        if (binaryFormatVersion != 0) {
            continue;   // currently, we have spec version 0 so other messages won't be parseable
        }

        try {
            AbstractBinaryDropMessage binMessage = new BinaryDropMessageV0(encryptedMessage);
            DropMessage dropMessage = binMessage.disassembleMessage(identity);  // decrypt with identities private key

            // TODO do something with the dropMessage (dropMessage.getPayload() contains '{msg: \"this is a plaintext message for you\"}')

        } catch (QablException e) {
            // failed to read ... maybe this message was not for us
        }
    }
}
```

## Account

The accounting server handles authentication and authorization for all actions related to the Box component of Qabel.
The responsible helper class for these actions is the `AccountingHTTP`.

// TODO

## Block

The block server is the storage backend for the Box component of Qabel.
To be able to upload something, your request needs to be authenticated so that the accounting server can authorize your requests:
```JAVA
AccountingHTTP accounting = ...;
accounting.login();

HttpRequest request = ...;  // prepare your block server request
accounting.authorize(request);
// do the request, it is now authorized
```


# Contribution

For issues using one of the Clients, please use the integrated feedback features.
For the Qabel Android Client, it is integrated at `Settings` `->` `write Feedback`.
For the Qabel Desktop Client, use the **feedback button** (<img alt="feedback icon" src="https://raw.githubusercontent.com/Qabel/qabel-desktop/master/src/main/resources/img/exclamation.png" height="14px"/>) inside the application.

For issues concerning the Qabel Core Library, use the Issue tracker of GitHub.

Please read the Contribution Guidelines (CONTRIBUTING.md) carefully.
