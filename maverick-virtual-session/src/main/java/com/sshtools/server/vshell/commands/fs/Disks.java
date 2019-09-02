/**
 * (c) 2002-2019 JADAPTIVE Limited. All Rights Reserved.
 *
 * This file is part of the Maverick Synergy Java SSH API.
 *
 * Maverick Synergy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Maverick Synergy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Maverick Synergy.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sshtools.server.vshell.commands.fs;

import java.io.File;
import java.io.IOException;

import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.VirtualConsole;

public class Disks extends ShellCommand {
	public Disks() {
		super("disks", SUBSYSTEM_FILESYSTEM, "[<path>]","List local root file systems or displays information about local disk drives");
//		getOptions().addOption("m", "mb", false, "Display size in megabytes");
//		getOptions().addOption("k", "kb", false, "Display size in kilobytes");
//		getOptions().addOption("g", "gb", false, "Display size in gigabytes");
//		getOptions().addOption("b", "b", false, "Display size in bytes");
	}

	public void run(String[] args, VirtualConsole process) throws IOException {

		if (args.length == 1) {
			for (File file : File.listRoots()) {
				process.println(file.getAbsolutePath());
			}
		} else {
			for (int i = 1; i < args.length; i++) {
				File f = new File(args[i]);
				process.println(f.getAbsolutePath());
				process.println("    Total: " + format(args, f.getTotalSpace()));
				process.println("    Usable: " + format(args, f.getUsableSpace()));
				process.println("    Free: " + format(args, f.getFreeSpace()));
			}
		}

	}

	static String format(String[] args, long value) {
		if (CliHelper.hasShortOption(args, 'm')) {
			return (value / 1024 / 1024) + " MB";
		} else if (CliHelper.hasShortOption(args, 'k')) {
			return (value / 1024) + " KB";
		} else if (CliHelper.hasShortOption(args, 'b')) {
			return value + " Bytes";
		} else {
			return (value / 1024 / 1024 / 1024) + " GB";
		}
	}
}