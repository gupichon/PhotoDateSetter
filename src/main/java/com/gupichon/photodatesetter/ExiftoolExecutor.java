package com.gupichon.photodatesetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExiftoolExecutor {

	public static boolean testExiftool() {
		String result = runExiftool("-ver");

		boolean testResult = false;
		if (result.length() > 0) {
			try {
				double versionNumber = Double.parseDouble(result);
				testResult = versionNumber>=11.88;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return testResult;
	}

	public static String runExiftool(String... options) {
		String[] fullCommand = new String[options.length+1];
		fullCommand[0] = "exiftool";
		for (int i = 0; i < options.length; i++) {
			fullCommand[i+1] = options[i];
		}
		ProcessBuilder pb = new ProcessBuilder(fullCommand);
		String result = "";
		try {
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			result = builder.toString();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
}
