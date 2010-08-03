#!/bin/bash

err=1
until [ $err == 0 ];
do
	/home/java/bin/java -server -Xms128m -Xmx1536m -cp ./libs/*:ae_gameserver.jar com.aionemu.gameserver.GameServer > log/stdout.log 2>&1
	err=$?
	sleep 10
done