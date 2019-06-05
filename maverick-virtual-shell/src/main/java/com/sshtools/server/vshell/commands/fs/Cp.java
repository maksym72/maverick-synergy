package com.sshtools.server.vshell.commands.fs;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;

import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vshell.ShellCommand;
import com.sshtools.server.vshell.VirtualProcess;

public class Cp extends ShellCommand {
	public Cp() {
		super("cp", ShellCommand.SUBSYSTEM_FILESYSTEM, "[<srcFilePath>[ <srcFilePath2>]] [targetPath]");
		setDescription("Copy files or directories");
		getOptions().addOption("v", false, "Verbose. Display file names as they are copied.");
	}

	public void run(CommandLine cli, final VirtualProcess process) throws IOException, PermissionDeniedException {
		String[] args = cli.getArgs();
		if (args.length < 3)
			throw new IOException("Not enough file names supplied.");
		AbstractFile target = process.getCurrentDirectory().resolveFile(args[args.length - 1]);

		if (args.length > 3 && (!target.exists() || !target.isDirectory())) {
			throw new IOException("Target must exist as a folder if multiple sources are specified.");
		}

		for (int i = 1; i < args.length - 1; i++) {
			AbstractFile src = process.getCurrentDirectory().resolveFile(args[i]);
			if (src.isDirectory() && target.isFile()) {
				throw new IOException("Cannot move folder " + src + " to file " + target);
			}
			if (target.exists()) {
				target = target.resolveFile(src.getName());
			}
			target.copyFrom(src);
			if (cli.hasOption('v')) {
				process.getConsole().printStringNewline(src.toString());
			}
		}
	}
}
