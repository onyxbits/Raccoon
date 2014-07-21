package de.onyxbits.raccoon.io;

import java.io.File;

/**
 * A datacontainer to hold the various components of a file path. This class is
 * intended to be used with the template engine.
 * 
 * @author patrick
 * 
 */
public class FileNode {

	public final File file;
	public final String name;
	public final String path;
	public final String parrent;

	public FileNode(File file) {
		this.file = file;
		this.name = file.getName();
		this.path = file.toURI().toString();
		this.parrent = file.getParentFile().toURI().toString();
	}
}
