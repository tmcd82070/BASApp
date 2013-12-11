package com.west.bas.ui;

import com.west.bas.database.SampleDatabaseHelper.Status;

public abstract class UpdateSampleCallback {
	public abstract void onTaskComplete(
			Status status,
			String comment);
}
