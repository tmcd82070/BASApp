package com.west.bas.ui;

import com.west.bas.database.SampleDatabaseHelper.Status;

public abstract class UpdateSampleCallback {
	public abstract void onTaskComplete(
			int itemID, 
			Status status,
			String comment);
}
