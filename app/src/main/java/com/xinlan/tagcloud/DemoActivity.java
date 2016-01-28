package com.xinlan.tagcloud;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.xinlan.tagcloud.view.TagCloudView;
import com.xinlan.tagcloud.view.TagView;

public class DemoActivity extends Activity {

	private TagCloudView mTagCloudView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Step0: to get a full-screen View:
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Step1: get screen resolution:
		final Display display = getWindowManager().getDefaultDisplay();
		final int width = display.getWidth();
		final int height = display.getHeight();

		// Step2: create the required TagList:
		// notice: All tags must have unique text field
		// if not, only the first occurrence will be added and the rest will be
		// ignored
		final List<TagView> myTagList = createTags();

		// Step3: create our TagCloudview and set it as the content of our
		// MainActivity
		mTagCloudView = new TagCloudView(this, width, height, myTagList); // passing
																			// current
																			// context
		setContentView(mTagCloudView);
		//mTagCloudView.requestFocus();
		//mTagCloudView.setFocusableInTouchMode(true);
	}

	private List<TagView> createTags() {
		// create the list of tags with popularity values and related url
		final List<TagView> tempList = new ArrayList<TagView>();

		tempList.addAll(decodeTags(R.array.tags_positive,
				getColors(R.color.blue)));
		tempList.addAll(decodeTags(R.array.tags_negative, getColors(R.color.red)));
		tempList.addAll(decodeTags(R.array.tags_various,
				getColors(R.color.green)));

		return tempList;
	}

	private List<TagView> decodeTags(final int resId, final int color) {
		final List<TagView> tags = new ArrayList<TagView>();

		final String[] labels = getResources().getStringArray(resId);
		for (int index = 0; index < labels.length; index++) {
			final String label = labels[index];

			tags.add(createTag(label, labels.length - index, color));
		}

		return tags;
	}

	private TagView createTag(final String text, final int popularity,
			final int color) {
		final TagView.TagBundle bundle = new TagView.TagBundle(text, popularity, color);
		return new TagView(this, bundle);
	}

	private int getColors(final int colorRes) {
		return getResources().getColor(colorRes);
	}

}
