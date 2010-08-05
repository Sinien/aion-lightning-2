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

# Choice between build and clean
asktype()
{
   echo "Choose build (b) for building GameServer, LoginServer or Commons project"
   echo "Choose clean (c) for cleaning GameServer, LoginServer or Commons project"
   echo "Choose quit (q) to quit this script"
   echo "Your choice, type: (b) build, (c) clean or (q) quit?"
   read TYPEPROMPT
   case "$TYPEPROMPT" in
      "b"|"B") askbuild;;
      "c"|"C") askclean;;
      "q"|"Q") finish;;
      *) asktype;;
   esac
}
# Choice between GS, LS and CM build
askbuild()
{
   echo "Choose Commons (c) for install Commons project (first Step)"
   echo "Choose GameServer (g) for building GameServer project"
   echo "Choose LoginServer (l) for building LoginServer project"
   echo "Choose quit (q) to quit this script"
   echo "Your choice, type: (g) GameServer, (l) LoginServer, (c) Commons or (q) quit?"
   read BUILDPROMPT
   case "$BUILDPROMPT" in
      "g"|"G") gsbuild;;
      "l"|"L") lsbuild;;
      "c"|"C") cmbuild;;
      "q"|"Q") finish;;
      *) askbuild;;
   esac
}

# Choice between GS, LS and cm clean
askclean()
{
   echo "Choose GameServer (g) for cleaning GameServer project"
   echo "Choose LoginServer (l) for cleaning LoginServer project"
   echo "Choose Commons (c) for cleaning Commons project"
   echo "Choose quit (q) to quit this script"
   echo "Your choice, type: (g) GameServer, (l) LoginServer, (c) Commons or (q) quit?"
   read CLEANPROMPT
   case "$CLEANPROMPT" in
      "g"|"G") gsclean;;
      "l"|"L") lsclean;;
      "c"|"C") cmclean;;
      "q"|"Q") finish;;
      *) askclean;;
   esac
}

# To finish
finish()
{
   echo ""
   echo "Script finished."
   echo "Made by Mystick Based on Resp@wner Work"
   exit 0
}

# LoginServer Build
lsbuild()
{
   echo "Moving to folder $pomxmlLSPath"
   cd $pomxmlLSPath
   mvn assembly:assembly
}

# GameServer Build
gsbuild()
{
   echo "Moving to folder $pomxmlGSPath"
   cd $pomxmlGSPath
   mvn assembly:assembly
}

# Commons Build
cmbuild()
{
   echo "Moving to folder $buildxmlCMPath"
   cd $buildxmlCMPath
   mvn install
}

# LoginServer Clean
lsclean()
{
   echo "Moving to folder $pomxmlLSPath"
   cd $pomxmlLSPath
   mvn clean
}

# GameServer Clean
gsclean()
{
   echo "Moving to folder $pomxmlGSPath"
   cd $pomxmlGSPath
   mvn clean
}

# Commons Clean
cmclean()
{
   echo "Moving to folder $buildxmlCMPath"
   cd $buildxmlCMPath
   mvn clean
}

asktype
