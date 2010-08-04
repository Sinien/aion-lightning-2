#!/bin/bash

err=1
until [ $err == 0 ];
do
	/home/java/bin/java -Xms8m -Xmx32m -ea -cp ./libs/*:al-login-1.0.0.jar com.aionemu.loginserver.LoginServer > log/stdout.log 2>&1
	err=$?
	sleep 10
done