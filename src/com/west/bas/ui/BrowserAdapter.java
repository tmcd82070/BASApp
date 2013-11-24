package com.west.bas.ui;

import java.io.File;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.west.bas.R;

public class BrowserAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private File mCurrentDirectory;
	
	private static File mRootSD;
	private static File mRootContext;
	
	public BrowserAdapter(Context context, int layoutViewId, List<String> listRootDirectory){
		// TODO R.layout.row_folder
		super(context,layoutViewId,listRootDirectory);
		mContext = context;
		
		mRootSD = Environment.getExternalStorageDirectory();
		mRootContext = context.getFilesDir();
		
		this.setNotifyOnChange(true);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView;
		if(new File(getItem(position)).isDirectory()){
			rowView = inflater.inflate(R.layout.row_folder, parent, false);
		}else{
			rowView = inflater.inflate(R.layout.row_filename, parent, false);
		}
		TextView textView = (TextView) rowView.findViewById(R.id.text_fileName);
		String[] filenameFields = getItem(position).split("/");
		textView.setText(filenameFields[filenameFields.length-1]);
		return rowView;
	}
	
	/** Move from the current directory up one level in the
	 * file hierarchy.  No action if already at the top level. */
	public void ascend(){
		String currentString = mCurrentDirectory.getAbsolutePath().toString();
		boolean isRoot = currentString.equals(mRootSD.getAbsolutePath().toString());
		isRoot = isRoot || currentString.equals(mRootContext.getAbsolutePath().toString());

		if(!isRoot){
			mCurrentDirectory = mCurrentDirectory.getParentFile();
			update();
		}
	}

	public void ascendToRoot(boolean useExternalStorage){
		if(useExternalStorage){
			mCurrentDirectory = mRootSD;
		}else{
			mCurrentDirectory = mRootContext;
		}
		update();
	}
	
	public void descendToDir(File dir){
		// TODO should have a check that the application has access to the given directory!
		if(dir.isDirectory()){
			mCurrentDirectory=dir;
			update();
		}
	}
	
	public static Vector<String> getDirectoryList(File directory){
		if(directory==null) directory = mRootContext;
		File[] files = directory.listFiles(new KMLFilter());
		if(files==null) return null;
		Vector<String> filelist = new Vector<String>(files.length);
		for(int i=0;i<files.length;i++){
			filelist.add(files[i].getAbsolutePath());
		}
		Log.d("Browser","actually update the display");
		return filelist;
	}

	public void update() {
		clear();
		for(String s : getDirectoryList(mCurrentDirectory)){
			add(s);
		}
	}
	
}
