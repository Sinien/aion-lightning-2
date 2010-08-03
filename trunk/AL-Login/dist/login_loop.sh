#!/bin/bash

err=1
until [ $err == 0 ];
do
	/home/java/bin/java -Xms8m -Xmx32m -ea -Xbootclasspath/p:./libs/jsr166.jar -javaagent:libs/ae_commons.jar -cp ./libs/*:ae_login.jar com.aionemu.loginserver.LoginServer > log/stdout.log 2>&1
	err=$?
	sleep 10
done