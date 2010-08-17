#!/bin/bash
################################################################################
###
###             By Mystick, Based on Resp@wner script on L2jfree
###
################################################################################
trap finish 2
pomxmlLSPath="../AL-Login"
pomxmlGSPath="../AL-Game"
pomxmlCMPath="../AL-Commons"
pomxmlCSPath="../AL-CServer"

if [ "$pomxmlLSPath" = "$pomxmlGSPath" ]; then
   echo "Problem with Path Server"
else
   echo "The path of the LoginServer project is $pomxmlLSPath"
   echo "The path of the GameServer project is $pomxmlGSPath"
   echo "The path of the Commons project is $pomxmlCMPath"
   echo "The path of the Commons project is $pomxmlCSPath"
fi
   echo "Moving to folder $buildxmlCMPath"
   cd $pomxmlCMPath
   mvn clean
   echo "Moving to folder $pomxmlLSPath"
   cd $pomxmlLSPath
   mvn clean
   echo "Moving to folder $pomxmlGSPath"
   cd $pomxmlGSPath
   mvn clean
   echo "Moving to folder $pomxmlCSPath"
   cd $pomxmlCSPath
   mvn clean
