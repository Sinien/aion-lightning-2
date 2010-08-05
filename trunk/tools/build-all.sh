#!/bin/bash
################################################################################
###
###             By Mystick, Based on Resp@wner script on L2jfree
###
################################################################################
trap finish 2
pomxmlLSPath="../AL-Login"
pomxmlGSPath="../AL-Game"
buildxmlCMPath="../AL-Commons"

if [ "$pomxmlLSPath" = "$pomxmlGSPath" ]; then
   echo "Problem with Path Server"
else
   echo "The path of the LoginServer project is $pomxmlLSPath"
   echo "The path of the GameServer project is $pomxmlGSPath"
   echo "The path of the Commons project is $buildxmlCMPath"
fi
   echo "Moving to folder $buildxmlCMPath"
   cd $buildxmlCMPath
   mvn install
   echo "Moving to folder $pomxmlLSPath"
   cd $pomxmlLSPath
   mvn assembly:assembly
   echo "Moving to folder $pomxmlGSPath"
   cd $pomxmlGSPath
   mvn assembly:assembly
