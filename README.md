# java-diff
A simple java program to apply IDA .diff files to any file.

# Usage #
```
Basic Usage:<br>
&emsp;&emsp;Patch file: java -jar java-diff.jar patch <binary> <diff> [options]
&emsp;&emsp;Revert patch: java -jar java-diff.jar revert <binary> <diff> [options]
Options:<br>
&emsp;&emsp;y: Always patch file even if warnings appear.
&emsp;&emsp;n: Abort on any warning.
&emsp;&emsp;b: Backup original file (with a .bak extension).
Examples:<br>
&emsp;&emsp;java -jar java-diff.jar patch binary.exe difffile.diff
&emsp;&emsp;java -jar java-diff.jar revert binary.exe difffile.diff y
&emsp;&emsp;java -jar java-diff.jar patch binary.exe difffile.diff nb
```

# NOTE #
Use this responsibly, I will not take responsibility of anything that happens to your program because someone has modified your program with this.!
