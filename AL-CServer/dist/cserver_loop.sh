#!/bin/bash

err=1
until [ $err == 0 ];
do
	/home/java/bin/java -Xmx512m -cp ./libs/*:al-cserver-1.0.0.jar com.aionemu.chatserver.ChatServer > log/stdout.log 2>&1
	err=$?
	sleep 10
done