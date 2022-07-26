package com.brave.braveandroidplaylist.model

import java.io.Serializable

class PlaylistOnboardingModel(
    val title: String,
    val message: String,
    val illustration: Int,
    val illustrationBg: Int = 0
) : Serializable