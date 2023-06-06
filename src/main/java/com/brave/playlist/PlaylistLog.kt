/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist

import android.util.Log
import com.brave.playlist.util.ConstantUtils.TAG

object PlaylistLog {
    /** Log Level Error  */
    fun e(message: String) {
        if (BuildConfig.DEBUG) Log.e(TAG, message)
    }

    /** Log Level Warning  */
    fun w(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, message)
    }

    /** Log Level Information  */
    fun i(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    /** Log Level Debug  */
    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    /** Log Level Verbose  */
    fun v(message: String) {
        if (BuildConfig.DEBUG) Log.v(TAG, message)
    }
}