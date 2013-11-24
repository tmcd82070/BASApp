package com.west.bas.ui;

public abstract class CreateBASCallback {
	public abstract void onTaskComplete(
			String studyName,
			int nSamples,
			int nOversamples,
			String studyAreaFilename);
}
