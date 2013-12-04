package com.west.bas.ui;

public abstract class ExportCallback {
	public abstract void onTaskComplete(
			boolean exportAll,
			String exportFilename);
}
