#!/bin/sh
java -Xms128m -Xmx128m -ea -Xbootclasspath/p:./libs/jsr166.jar -javaagent:libs/ae_commons.jar -cp ./libs/*:ae_chat.jar com.aionemu.chatserver.ChatServer
