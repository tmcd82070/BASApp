package com.west.bas.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.west.bas.R;

/**
 * An adapter that connects the application or SD card directory
 * structure to a ListView.  The display shows directories (folders)
 * and files that have the KML extension.
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 * 
 * @see KMLFilter
 */
public class BrowserAdapter extends ArrayAdapter<String> {

	/** The application context, used to determine the root directory */
	private Context mContext;
	
	/** A handle to the directory that is currently displayed */
	private File mCurrentDirectory;
	
	/** A shortcut handle to the root of the application's directory structure */
	private static File mRootSD;
	
	/** A shortcut handle to the root of the SD card (external storage) */
	private static File mRootContext;
	
	/** Special case string used to indicate that there are no
	 * files available in the currently selected folder (rather
	 * than just leaving it blank).  This entry should be 
	 * ignored if selected. */
	public static final String NO_FILES_WARNING = "[No files available]";
	
	/** Special case string used to provide access to a generic action 
	 * that moves up one level in the file hierarchy. */
	public static final String UP_ONE_LEVEL = "[Up one level]";
	
	/** Constructor to initialize the adapter. 
	 * @param context application context
	 * @param layoutViewId identifier of ListView to populate (from R.id.\<name\>)
	 * @param listRootDirectory initial list of values
	 */
	public BrowserAdapter(Context context, int layoutViewId, List<String> listRootDirectory){
		super(context,layoutViewId,listRootDirectory);
		mContext = context;
		
		mRootSD = Environment.getExternalStorageDirectory();
		mRootContext = context.getFilesDir();
		
		this.setNotifyOnChange(true);
		
		update();
	}
	
	/** Initializes and returns a view that is customized based on the
	 * type of content it will display (folder or file)
	 * 
	 * @param position index into the list of displayed strings
	 * @param convertView the root View of which the row is a child
	 * @param parent closest ancestor in the View heirarchy
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView;
		
		// Check for the special cases
		String itemName = getItem(position);
		if(itemName.equals(NO_FILES_WARNING)){
			rowView = inflater.inflate(R.layout.row_text_with_icon, parent, false);
			((ImageView) rowView.findViewById(R.id.icon)).setImageResource(R.drawable.icon_folder_empty);
		}else if(itemName.equals(UP_ONE_LEVEL)){
			rowView = inflater.inflate(R.layout.row_text_with_icon, parent, false);
			((ImageView) rowView.findViewById(R.id.icon)).setImageResource(R.drawable.icon_folder_back);
		}else if(new File(itemName).isDirectory()){
			//uses the default icon
			rowView = inflater.inflate(R.layout.row_text_with_icon, parent, false);
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
	
	/** 
	 * Separates the folders from the files; folders are placed
	 * at the top of the list.
	 * @param directory
	 * @return
	 */
	public Vector<String> getDirectoryList(File directory){
		if(directory==null) directory = mRootContext;
		Vector<String> filelist = new Vector<String>();
		// get a list of folders
		File[] folders = directory.listFiles(new FolderFilter());
		if(folders!=null){
			for(int i=0;i<folders.length;i++){
				filelist.add(folders[i].getAbsolutePath());
			}
		}		
		// add a list of files
		File[] files = directory.listFiles(new KMLFilter());
		if(files!=null){
			for(int i=0;i<files.length;i++){
				filelist.add(files[i].getAbsolutePath());
			}
		}
		return filelist;
	}

	public class KMLFilter implements FilenameFilter {
		//TODO make this a spatial file filter? (.shp too?)
		public boolean accept(File f, String s){
			return s.endsWith(".kml");
		}

	}
	public class FolderFilter implements FilenameFilter{
		public boolean accept(File f, String s){
			return new File(f.getAbsoluteFile()+"/"+s).isDirectory();
		}
	}
	public void update() {
		clear();
		if(mCurrentDirectory==null) add(NO_FILES_WARNING);
		else{
			// allow back navigation
			if(!mCurrentDirectory.getAbsolutePath().equals(mRootContext.getAbsolutePath()) &&
					!mCurrentDirectory.getAbsolutePath().equals(mRootSD.getAbsolutePath())){
				add(UP_ONE_LEVEL);
			}
			for(String s : getDirectoryList(mCurrentDirectory)){
				if(s.equals(NO_FILES_WARNING)) Log.e("browser","Filename matches special case NO_FILES_WARNING");
				if(s.equals(UP_ONE_LEVEL)) Log.e("browser","Filename matches special case UP_ONE_LEVEL");
				add(s);
			}
			if(getCount()==0) add(NO_FILES_WARNING);
		}

	}

	/**
	 * Method to handle selections in the browser ListView.  This is
	 * Contained within the adapter to deal with the special case 
	 * entries in the list.
	 * @see #NO_FILES_WARNING
	 * @see #UP_ONE_LEVEL
	 * @param selectedString string representation of an absolute path in the file system
	 * @return true if the selection is a file; false otherwise
	 */
	public boolean handleSelection(String selectedString) {
		// no action for the "no files" item
		if(selectedString.equals(NO_FILES_WARNING)) return false;
		if(selectedString.equals(UP_ONE_LEVEL)){
			ascend();
			return false;
		}
			
		File selectedFile = new File(selectedString);
		if(selectedFile.isDirectory()){
			descendToDir(selectedFile);
			return false;
		}
		return true;
	}	
}
