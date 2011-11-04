/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.theme.cts;

import com.android.cts.stub.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;

import junit.framework.Assert;

/**
 * This class runs the series of tests for a specific theme in the activity.
 *
 * <p>
 * Additionally, this class can also generate the good versions of bitmaps to use for testing.
 */
public class ThemeTester {
    private Activity mActivity;
    private String mThemeName;
    private TesterViewGroup mRoot;
    private boolean mShouldAssert;

    /**
     * Creates a ThemeTester to run all of the tests.
     * @param activity Activity that serves as a test harness for this theme test.
     * @param themeName Name of the theme being tested.
     */
    public ThemeTester(Activity activity, String themeName) {
        mActivity = activity;
        mThemeName = themeName;
        mRoot = (TesterViewGroup) mActivity.findViewById(R.id.test_group);
        mShouldAssert = true;
    }

    public void setShouldAssert(boolean shouldAssert) {
        mShouldAssert = shouldAssert;
    }

    /**
     * Run all of the tests.
     */
    public void runTests() {
        ThemeTestInfo[] tests = ThemeTests.getTests();
        for (final ThemeTestInfo test : tests) {
            runTest(test);
        }
    }

    /**
     * Run an individual test based upon its position in the tests.
     * @param position The position of the test that you wish to run.
     */
    public void runTest(int position) {
        runTest(ThemeTests.getTests()[position]);
    }

    /**
     * Run an individual test.
     * @param test The {@link ThemeTestInfo} to use for this test.
     */
    private void runTest(final ThemeTestInfo test) {
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                testViewFromId(test);
            }
        });
    }

    /**
     * Generate all of the tests, saving them to Bitmaps in the application's data folder.
     */
    public void generateTests() {
        ThemeTestInfo[] tests = ThemeTests.getTests();
        for (final ThemeTestInfo test : tests) {
            mRoot.post(new Runnable() {
                @Override
                public void run() {
                    generateViewFromId(test);
                }
            });
        }
    }

    private void testViewFromId(ThemeTestInfo test) {
        processBitmapFromViewId(test, false,
                new BitmapComparer(mActivity, mThemeName + "_" + test.getTestName(), false));
    }

    private void generateViewFromId(ThemeTestInfo test) {
        processBitmapFromViewId(test, true,
                new BitmapSaver(mActivity, mThemeName + "_" + test.getTestName(), false));
    }

    private void processBitmapFromViewId(final ThemeTestInfo test,
            final boolean generate, final BitmapProcessor processor) {
        int resId = test.getLayoutResourceId();
        ThemeTestModifier modifier = test.getThemeModifier();
        final View view = constructViewFromLayoutId(resId, modifier);
        view.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(
                        view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                boolean success = processor.processBitmap(bitmap);
                if (!generate && !success && mShouldAssert) {
                    String testName = mThemeName + "_" + test.getTestName();
                    Assert.fail(testName);
                }

                bitmap.recycle();

                mRoot.removeView(view);
            }
        });
    }

    /**
     * Inflates and returns the view and performs and modifications of it as required by the test.
     * @param resid The resource id of the layout that will be constructed.
     * @param modifier The ThemeTestModifier to modify the layout being tested.
     * @return The root view of the layout being tested.
     */
    private View constructViewFromLayoutId(int resid, ThemeTestModifier modifier) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);

        View view = inflater.inflate(resid, mRoot, false);
        mRoot.addView(view);

        if (modifier != null) {
            view = modifier.modifyView(view);
        }

        mRoot.measure(0, 0); // don't care about the input values - we build our reference size
        mRoot.layout(0, 0, mRoot.getMeasuredWidth(), mRoot.getMeasuredHeight());

        view.setFocusable(false);

        return view;
    }
}
