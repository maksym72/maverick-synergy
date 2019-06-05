package com.sshtools.common.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.commons.tests.util.fileSystem.InMemoryFile;
import com.sshtools.commons.tests.util.fileSystem.InMemoryAbstractFileFactory;
import com.sshtools.commons.tests.util.fileSystem.InMemoryAbstractFile;

public class InMemoryFileTest extends AbstractFileTest {

	private AbstractFileFactory<InMemoryAbstractFile> factory = new InMemoryAbstractFileFactory();
	private SshConnection sshConnection = new MockConnection("Doe", UUID.randomUUID().toString(), 
			new InetSocketAddress("127.0.0.1", 22), new InetSocketAddress("127.0.0.1", 5555));
	
	@Override
	public AbstractFile getAbstractFile(String path, SshConnection con) throws PermissionDeniedException, IOException {
		return factory.getFile(path, con);
	}

	@Override
	public AbstractFile getMainTestDirectory() throws PermissionDeniedException, IOException {
		getAbstractFile("/home", sshConnection); //create directory home
		getAbstractFile("/home/user", sshConnection); //create directory user in home 
		AbstractFile testDirectory =  getAbstractFile("/home/user/test", sshConnection); // create directory test in user
		return testDirectory;
	}

	@Override
	public String getPathSeperator() {
		return InMemoryFile.getPathSeperator();
	}

	@Override
	public AbstractFile getHomeDirectory() throws PermissionDeniedException, IOException {
		getAbstractFile("/home", sshConnection); //create directory home
		return getAbstractFile("/home/Doe", sshConnection); //create directory Doe in home 
	}

	@Override
	public List<AbstractFile> getListOfFilesRecursive(AbstractFile folder)
			throws PermissionDeniedException, IOException {
		
		if (folder.isFile()) {
			throw new IOException("Cannot list children of a file.");
		}
		
		return folder.getChildren();
	}

	@Override
	public AbstractFile getHiddenFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + ".ssh.txt", sshConnection); 
	}

	@Override
	public AbstractFile getNonHiddenFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + "non_hidden.txt", sshConnection); 
	}

	@Override
	public AbstractFile getReadableFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + "readable_file.txt", sshConnection); 
	}

	@Override
	public AbstractFile getNonReadableFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + "_ignore_.txt", sshConnection); 
	}

	@Override
	public AbstractFile getWritableFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + "writable_file.txt", sshConnection); 
	}

	@Override
	public AbstractFile getNonWritableFile() throws PermissionDeniedException, IOException {
		return getAbstractFile(getMainTestDirectory() + getPathSeperator() + "_ignore_.txt", sshConnection); 
	}

	@Override
	public SshConnection getSshConnection() {
		return sshConnection;
	}

}
