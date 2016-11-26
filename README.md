# java-diff
A simple java program to apply IDA .diff files to any file.

# Download #
Download old versions in the "releases" section or just download the latest at my Jenkins server here: http://ci.nulldev.xyz/job/java-diff/lastSuccessfulBuild/deployedArtifacts/

# Usage #
```
Basic Usage:
	Patch file: java -jar java-diff.jar patch <binary> <diff> [options]
	Revert patch: java -jar java-diff.jar revert <binary> <diff> [options]
Options:
	y: Always patch file even if warnings appear.
	n: Abort on any warning.
	b: Backup original file (with a .bak extension).
Examples:
	java -jar java-diff.jar patch binary.exe difffile.diff
	java -jar java-diff.jar revert binary.exe difffile.diff y
	java -jar java-diff.jar patch binary.exe difffile.diff nb
```

# Build Status #
[![Build Status](http://ci.nulldev.xyz/job/java-diff/badge/icon)](http://ci.nulldev.xyz/job/java-diff)

# FAQ #
<strong>1. How large can the files I patch be?</strong><br>
Java-diff prefers size over speed, it is able to patch files as large as you want, as long as the .diff file fits in memory!<br><br>
<strong>2. Why does java-diff leave a [something].patch file?</strong><br>
This is a temporary file is used while patching. It should have been deleted after the patching process but if it is still there, it means java-diff has crashed and your patch was not applied. You should remove it before starting a new operation.<br><br>

# NOTE #
Use this responsibly, I will not take responsibility of anything that happens to your program because someone has modified your program with this!
