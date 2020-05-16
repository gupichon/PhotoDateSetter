package com.gupichon.photodatesetter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javafx.concurrent.Task;

public class ExiftoolTask extends Task<Void> {

	private static final String dateTAG = "-DateTimeOriginal=";
	private static final String descTAG = "-ImageDescription=";
	private static final String overwriteTAG = "-overwrite_original";

	private String directoryPath;
	private LocalDate startDate;
	private String desc;
	private volatile int nbFileTreated;
	private int nbFilesToTreat;
	private Object syncObj;
	private int nbOptions;

	/**
	 * @param directoryPath
	 */
	public ExiftoolTask(LocalDate startDate, String desc, String directoryPath) {
		syncObj = new Object();
		this.startDate = startDate;
		this.directoryPath = directoryPath;
		this.desc = null;
		nbOptions = 3;
		if( desc!=null && !desc.trim().isEmpty()) {
			this.desc = desc.trim();
			nbOptions = 4;
		}
		this.nbFileTreated = 0;
	}

	@Override
	protected Void call() throws Exception {
		File dirFile = new File(directoryPath);
		FilenameFilter filter = (dir, name) -> (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".JPG")
				|| name.endsWith(".JPEG") || name.endsWith(".bmp") || name.endsWith(".BMP") || name.endsWith(".tif")
				|| name.endsWith(".tiff") || name.endsWith(".TIF") || name.endsWith(".TIFF"));
		File[] files = dirFile.listFiles(filter);
		Arrays.sort(files);
		nbFilesToTreat = files.length;
		updateProgress(0, nbFilesToTreat);
		updateMessage("");

		int nbProcs = Runtime.getRuntime().availableProcessors();
		if (nbProcs > 1) {
			nbProcs--;
		}
		ExecutorService executor = Executors.newFixedThreadPool(nbProcs);
		int index = 1;
		int y = startDate.getYear();
		int m = startDate.getMonthValue();
		int d = startDate.getDayOfMonth();
		String date = Integer.toString(y) + ":" + twoDigitsInt(m) + ":" + twoDigitsInt(d);
		List<Future<?>> futures = new ArrayList<>(nbFilesToTreat);
		for (File file : files) {
			String dateTime = date + " " + toTimeFromCounter(index);
			ExiftoolRunnable exiftoolRunnable = new ExiftoolRunnable(file, dateTime);
			Future<?> futur = executor.submit(exiftoolRunnable);
			futures.add(futur);
			index++;
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		executor.awaitTermination(3, TimeUnit.SECONDS);
		ExiftoolTask.this.updateMessage("Fin");

		return null;
	}

	private static String toTimeFromCounter(int i) {
		int minFull = i / 60;
		int hour = minFull / 60;
		int min = minFull - hour * 60;
		int sec = i - minFull * 60;
		String time = Integer.toString(hour) + ":" + twoDigitsInt(min) + ":" + twoDigitsInt(sec);
		return time;
	}

	private static String twoDigitsInt(int i) {
		if (i < 10) {
			return "0" + Integer.toString(i);
		}
		return Integer.toString(i);
	}

	private class ExiftoolRunnable implements Runnable {
		private File f;
		private String dateTimeToSet;

		public ExiftoolRunnable(File f, String dateTimeToSet) {
			this.f = f;
			this.dateTimeToSet = dateTimeToSet;
		}

		@Override
		public void run() {
			ExiftoolTask.this.updateMessage(f.getName());
			String filePath = null;
			try {
				filePath = f.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (filePath != null) {
				int opt = 0;
				String[] options = new String[nbOptions];
				String dateOption = dateTAG + dateTimeToSet;
				options[opt]=dateOption;
				opt++;
				if (desc != null) {
					String descOption = descTAG + desc;
					options[opt]=descOption;
					opt++;
				}
				options[opt]=overwriteTAG;
				opt++;
				options[opt]=filePath;
				opt++;
				ExiftoolExecutor.runExiftool(options);
				synchronized (syncObj) {
					nbFileTreated++;
					ExiftoolTask.this.updateProgress(nbFileTreated, nbFilesToTreat);
				}
			}
		}
	}

}
