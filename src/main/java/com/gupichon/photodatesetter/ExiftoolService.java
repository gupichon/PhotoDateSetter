package com.gupichon.photodatesetter;

import java.time.LocalDate;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ExiftoolService extends Service<Void> {
	private LocalDate startDate;
	private String desc;
	private String directoryPath;

	/**
	 * @param directoryPath
	 */
	public void setValues(LocalDate startDate, String desc, String directoryPath) {
		this.directoryPath = directoryPath;
		this.startDate = startDate;
		this.desc = desc;
	}

	@Override
	protected Task<Void> createTask() {
		return new ExiftoolTask(startDate, desc, directoryPath);
	}

}
