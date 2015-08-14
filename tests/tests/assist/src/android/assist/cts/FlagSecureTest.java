/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.assist.cts;

import android.assist.common.Utils;

import java.util.concurrent.CountDownLatch;

/**
 * Test we receive proper assist data (root assistStructure with no children) when the assistant is
 * invoked on an app with FLAG_SECURE set.
 */
public class FlagSecureTest extends AssistTestBase {

    static final String TAG = "DisableContextTest";

    private static final String TEST_CASE_TYPE = Utils.FLAG_SECURE;

    public FlagSecureTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startTestActivity(TEST_CASE_TYPE);
        waitForBroadcast();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSecureActivity() throws Exception {
        verifyAssistDataNullness(false, false, false, true);

        // verify that we have only the root window and not its children.
        verifyAssistStructure(Utils.getTestAppComponent(TEST_CASE_TYPE), true);
    }
}