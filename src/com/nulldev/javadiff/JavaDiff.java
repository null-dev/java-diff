package com.nulldev.javadiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

/*
 * +=======================+
 * + java-diff by: nulldev +
 * +                       +
 * + A java utilty to      +
 * + apply IDA .diff files +
 * + to any file.          +
 * +                       +
 * + Version: v1.0         +
 * +=======================+
 * 
 */

class JavaDiff {
	static Scanner inputScanner = new Scanner(System.in);
	static boolean alwaysPatch = false;
	static boolean alwaysAbort = false;
	static boolean revert = false;
	public static void main(String[] args) throws IOException {	
		File binary = null;
		File diff = null;
		boolean backup = false;
		HashMap<BigInteger, PatchEntry> toPatch = new HashMap<BigInteger, PatchEntry>();
		//new BigInteger(hex, 16);
		if(args.length == 0) {
			//Print help
			System.out.println("Usage:");
			System.out.println("	Patch file: java -jar java-diff.jar patch <binary> <diff> [options]");
			System.out.println("	Revert patch: java -jar java-diff.jar revert <binary> <diff> [options]");
			System.out.println("");
			System.out.println("Options:");
			System.out.println("	y: Always patch file even if warnings appear.");
			System.out.println("	n: Abort on any warning.");
			System.out.println("	b: Backup original file (with a .bak extension).");
			System.out.println("");
			System.out.println("Examples:");
			System.out.println("	java -jar java-diff.jar patch binary.exe difffile.diff");
			System.out.println("	java -jar java-diff.jar revert binary.exe difffile.diff y");
			System.out.println("	java -jar java-diff.jar patch binary.exe difffile.diff nb");
		} else {
			//Check if we are reverting or patching...
			if(args[0].equalsIgnoreCase("patch")) {
				//Patching
				revert = false;
			} else if(args[0].equalsIgnoreCase("revert")) {
				//Reverting
				revert = true;
			} else {
				//User being dumb
				System.out.println("Invalid operation, must be 'patch' or 'revert'");
				System.exit(1);
			}

			//Check if binary is specified and set it
			if(args.length >= 2 && args[1] != null && args[1] != "") {
				binary = new File(args[1]);
				if(!binary.exists()) {
					//User gave me a non-existing file...
					System.out.println("Invalid binary file!");
					System.exit(2);
				}
			} else {
				//User gave me a non-existing file...
				System.out.println("Must specify binary file!");
				System.exit(1);
			}
			//Check if binary is specified and set it
			if(args.length >= 3 && args[2] != null && args[2] != "") {
				diff = new File(args[2]);
				if(!diff.exists()) {
					//User gave me a non-existing file...
					System.out.println("Invalid diff file!");
					System.exit(3);
				}
			} else {
				//User gave me a non-existing file...
				System.out.println("Must specify diff file!");
				System.exit(1);
			}
			//Process options
			if(args.length >= 4 && args[3] != null && args[3] != "") {
				char options[] = args[3].toCharArray();
				for(char option : options) {
					//Is option y selected?
					if(option == 'y') {
						if(alwaysAbort == false)
							alwaysPatch = true;
						else {
							//User being dumb
							System.out.println("You cannot use 'always patch' and 'abort on any warning' at the same time!");
							System.exit(4);
						}
						//Is option n selected?
					} else if(option == 'n') {
						if(alwaysPatch == false)
							alwaysAbort = true;
						else {
							//User being dumb
							System.out.println("You cannot use 'always patch' and 'abort on any warning' at the same time!");
							System.exit(5);
						}
						//Is option b selected?
					} else if(option == 'b') {
						backup = true;
					}
				}
			}

			//WE ARE DONE WITH THE INPUT :)
			System.out.println("Processing .diff file...");

			//Process diff file
			try(BufferedReader br = new BufferedReader(new FileReader(diff))) {
				for(String line; (line = br.readLine()) != null; ) {
					//Process the entry
					DiffEntry diffEntry = processEntry(line);
					if(diffEntry != null) {
						if(toPatch.containsKey(diffEntry.offset)) {
							//They are full?
							String input = "";
							if(alwaysPatch) {
								System.out.println("WARNING: Duplicate entry detected, replace existing entry with this one? (Y/N) y");
								input = "y";
							} else if(alwaysAbort) {
								//Invalid entry to abort
								System.out.println("Always abort has been enabled so ABORTING...");
								System.exit(7);
							} else {
								System.out.println("WARNING: Duplicate entry detected, replace existing entry with this one? (Y/N)");
								input = inputScanner.next();
							}

							if(input.equalsIgnoreCase("y")) {
								//Replace entry
								System.out.println("Replacing entry...");
								toPatch.remove(diffEntry.offset);
								toPatch.put(diffEntry.offset, diffEntry.entry);
							}
							System.out.println("Ignoring entry...");
						} else {
							//Add the entry
							toPatch.put(diffEntry.offset, diffEntry.entry);
						}
					}
				}
			}

			//We are done with the diff file
			if(revert)
				System.out.println("Reverting patch...");
			else 
				System.out.println("Applying patch...");

			//APPLY/REVERT THE PATCH
			try {
				applyPatch(binary, backup, toPatch);
				System.out.println("Successfully applied the patch!");
			} catch (IOException ioException) {
				System.out.println("Unable to apply the patch, an error occured!");
				ioException.printStackTrace();
			}
		}
	}

	//Huge long very careful function to process one line of the diff file
	public static DiffEntry processEntry(String entry) {
		if(!entry.contains(":")) {
			//Invalid entry
			return null;
		} else {
			String diff[] = entry.split(":");
			if(diff.length != 2) {
				//Invalid entry
				return null;
			} else {
				BigInteger offset = new BigInteger(diff[0].replace(" ", ""), 16);
				String replaced[] = diff[1].split(" ");
				if(replaced.length < 2) {
					//Invalid entry
					return null;
				} else {
					//Process the right end of the patch
					String from = "empty";
					String to = "empty";
					for(String hex : replaced) {
						if(hex != null && !hex.equalsIgnoreCase("") && !hex.equalsIgnoreCase(" ")) {
							if(from.equals("empty")) {
								from = hex;
							} else if(to.equals("empty")) {
								to = hex;
							} else {
								//They are full?
								String input = "";
								if(alwaysPatch) {
									System.out.println("WARNING: An invalid entry has been found however it still contains enough information to be used for patching, use the entry? (Y/N) y");
									input = "y";
								} else if(alwaysAbort) {
									//Invalid entry so abort
									System.out.println("Always abort has been enabled so ABORTING...");
									System.exit(6);
								} else {
									System.out.println("WARNING: An invalid entry has been found however it still contains enough information to be used for patching, use the entry? (Y/N)");
									input = inputScanner.next();
								}

								if(input.equalsIgnoreCase("n")) {
									//Invalid entry
									System.out.println("Entry ignored.");
									return null;
								}
								System.out.println("Entry is being used for patch.");
							}
						}
					}
					if(from != "empty" && to != "empty") {
						//Generate a patchEntry
						PatchEntry patchEntry;
						if(revert)
							patchEntry = new PatchEntry(to, from);
						else
							patchEntry = new PatchEntry(from, to);

						//RETURN IT YAY WAHOO
						return new DiffEntry(offset, patchEntry);
					} else {
						//Invalid entry
						return null;
					}
				}
			}
		}
	}

	//Function to apply patch
	public static void applyPatch(File file, boolean backup, HashMap<BigInteger, PatchEntry> toPatch) throws IOException {
		BigInteger byteID = new BigInteger("0");
		InputStream hexStream = new FileInputStream(file);
		OutputStream outStream = new FileOutputStream(new File(file.getAbsolutePath() + ".patch"));
		int value;

		while ((value = hexStream.read()) != -1) {
			//convert to hex value with "X" formatter
			String hex = String.format("%02X", value);
			if(toPatch.containsKey(byteID)) {
				//Got something to patch sir :)
				if(!toPatch.get(byteID).from.equals(hex)) {
					//FROM DOES NOT MATCH!!
					String input = "";
					if(alwaysPatch) {
						System.out.println("WARNING: Expected "+toPatch.get(byteID).from+" but got " + hex +"! Apply this entry? (Y/N) y");
						input = "y";
					} else if(alwaysAbort) {
						//Invalid entry to abort
						System.out.println("Always abort has been enabled so ABORTING...");
						//Delete temp file
						Files.deleteIfExists(new File(file.getAbsolutePath() + ".patch").toPath());
						System.exit(6);
					} else {
						System.out.println("WARNING: Expected "+toPatch.get(byteID).from+" but got " + hex +"! Apply this entry? (Y/N)");
						input = inputScanner.next();
					}

					if(input.equalsIgnoreCase("n")) {
						//Ignore the entry
						System.out.println("Ignoring this entry...");
						outStream.write(Integer.parseInt(hex, 16));
					} else {
						//Apply the entry
						System.out.println("Applying this entry...");
						outStream.write(Integer.parseInt(toPatch.get(byteID).to, 16));
					}
				} else {
					//Apply the entry
					outStream.write(Integer.parseInt(toPatch.get(byteID).to, 16));
				}
				//Remove the entry to make things a little faster
				toPatch.remove(byteID);
			} else {
				//Nothing to patch here, move on...
				outStream.write(Integer.parseInt(hex, 16));
			}
			//Increment byte counter
			byteID = byteID.add(new BigInteger("1"));
		}

		//No resource leaks please
		outStream.close();
		hexStream.close();

		if(backup) {
			//Backup the binary
			System.out.println("Creating backup...");
			Files.copy(file.toPath(), Paths.get(file.getAbsolutePath()+".bak"));
		}
		//Delete the binary
		Files.deleteIfExists(file.toPath());

		//Rename the file
		new File(file.getAbsolutePath() + ".patch").renameTo(file);
	}
}

//A tuple to hold the offset and the patchEntry
class DiffEntry {
	BigInteger offset;
	PatchEntry entry;
	public DiffEntry(BigInteger offset, PatchEntry entry) {
		this.offset = offset;
		this.entry = entry;
	}
}

//A tuple to hold the from and to hex info
class PatchEntry {
	String from = "";
	String to = "";
	public PatchEntry(String from, String to) {
		this.from = from;
		this.to = to;
	}
}
