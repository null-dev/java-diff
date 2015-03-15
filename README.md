# java-diff
A simple java program to apply IDA .diff files to any file.

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

# NOTE #
Use this responsibly, I will not take responsibility of anything that happens to your program because someone has modified your program with this.!
