package com.west.bas;

public class BoundingBox { 
	
	private float mMinX; 
	private float mMinY; 
	private float mWidth; 
	private float mHeight; 

	/**
	 * Create a rectangular bounding box based on the coordinates 
	 * of two points (located in opposite corners)
	 * @param x1 x coordinate of one point
	 * @param y1 y coordinate of one point
	 * @param x2 x coordinate of a second point
	 * @param y2 y coordinate of a second point
	 */
	public BoundingBox(float x1, float y1, float x2, float y2) { 
		mMinX = Math.min(x1, x2); 
		mWidth = Math.abs(x1 - x2); 
		mMinY = Math.min(y1, y2); 
		mHeight = Math.abs(y1 - y2); 
	} 
	
	/**
	 * Create a rectangular bounding box based on the coordinates
	 * of the bottom left corner, its width, and its height
	 * @param bottomLeft ordered pair of values indicating x,y coordinates
	 * @param width horizontal size of the bounding box
	 * @param height vertical size of the bounding box
	 */
	public BoundingBox(float[] bottomLeft, float width, float height) { 
		mMinX = bottomLeft[0]; 
		mMinY = bottomLeft[1]; 
		mWidth = width; 
		mHeight = height; 
	} 
	
	/** 
	 * Project a pair of coordinates into the rectangular bounding box
	 * @param x horizontal position within the bounding box [0,1]
	 * @param y vertical position within the bounding box [0,1]
	 * @return array containing location of the point within the bounding box
	 */
	public float[] getSample(float x, float y) { 
		float x_bb = mWidth * x + mMinX; 
		float y_bb = mHeight * y + mMinY; 
		return (new float[] { x_bb, y_bb }); 
	} 
	
	/** 
	 * Project an ordered coordinate pair (given as an array) into 
	 * the rectangular bounding box
	 * @param point x,y values [0,1]
	 * @return array containing location of the point within the bounding box
	 */
	public float[] getSample(float[] point) { 
		return getSample(point[0], point[1]); 
	} 
	
	/**
	 * Calculate the area of the bounding box 
	 * @return area as square units
	 */
	public float getArea() { 
		return mWidth * mHeight; 
	} 
	
}
