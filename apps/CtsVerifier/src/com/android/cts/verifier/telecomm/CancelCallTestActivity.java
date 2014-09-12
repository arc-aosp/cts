/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.cts.verifier.telecomm;

import android.os.SystemClock;
import android.telecomm.Connection;
import android.telecomm.ConnectionRequest;
import android.telecomm.PhoneAccountHandle;

import com.android.cts.verifier.R;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Tests that a call can be canceled by a ConnectionService (and that the cancelation is respected).
 * Once the ConnectionService returns a canceled connection, the test verifies that Telecomm does
 * not have any active calls. If this is the case, the test will pass.
 */
public class CancelCallTestActivity extends TelecommBaseTestActivity {
    private static final Semaphore sLock = new Semaphore(0);

    @Override
    protected int getTestTitleResource() {
        return R.string.telecomm_cancel_call_title;
    }

    @Override
    protected int getTestInfoResource() {
        return R.string.telecomm_cancel_call_info;
    }

    @Override
    protected Class<? extends android.telecomm.ConnectionService> getConnectionService() {
        return ConnectionService.class;
    }

    @Override
    protected String getConnectionServiceLabel() {
        return "Call Cancel Manager";
    }

    @Override
    protected boolean onCallPlacedBackgroundThread() {
        try {
            if (!sLock.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                return false;
            }

            // Wait for the listeners to be fired so the call is cleaned up.
            SystemClock.sleep(1000);

            // Make sure that there aren't any ongoing calls.
            return !getTelecommManager().isInCall();
        } catch (Exception e) {
            return false;
        }
    }

    public static class ConnectionService extends android.telecomm.ConnectionService {
        @Override
        public Connection onCreateOutgoingConnection(
                PhoneAccountHandle connectionManagerPhoneAccount,
                ConnectionRequest request) {
            sLock.release();
            return Connection.createCanceledConnection();
        }
    }
}