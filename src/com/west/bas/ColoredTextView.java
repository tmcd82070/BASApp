package west.sample.bas;

import west.sample.bas.SamplePoint.Status;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColoredTextView extends TextView {

	/** Keep all the default constructors */
	public ColoredTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public ColoredTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ColoredTextView(Context context) {
		super(context);
	}

    /** State by which to set the colors */
	private static final int[] STATE_SAMPLE = {R.attr.state_sample};
	private static final int[] STATE_REJECTED = {R.attr.state_rejected};
	private static final int[] STATE_COLLECTED = {R.attr.state_collected};

	private boolean mIsSample = false;
	private boolean mIsRejected = false;
	private boolean mIsCollected = false;

	public void setState(Status status){
		mIsSample = mIsRejected = mIsCollected = false;
		switch(status){
		case COLLECTED:
			mIsCollected = true;
			// intentional fall through
		case SAMPLE:
			mIsSample = true;
			break;
		case REJECT:
			mIsSample = true;
			mIsRejected = true;
			break;
		case OVERSAMPLE:
			// do nothing (isSample=false)
		}
	}
	
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		int extraNeeded = 0;
		if(mIsSample) extraNeeded++;
		if(mIsRejected) extraNeeded++;
		if(mIsCollected) extraNeeded++;
		
	    int[] drawableState = super.onCreateDrawableState(extraSpace+extraNeeded);
	    if (mIsSample) drawableState = mergeDrawableStates(drawableState, STATE_SAMPLE);
	    if (mIsRejected) drawableState = mergeDrawableStates(drawableState, STATE_REJECTED);
	    if (mIsCollected) drawableState = mergeDrawableStates(drawableState, STATE_COLLECTED);
	    return drawableState;
	}
}
