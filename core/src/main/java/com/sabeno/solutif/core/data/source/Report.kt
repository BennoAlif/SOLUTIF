package com.sabeno.solutif.core.data.source

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    var description: String? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 118.0,
    var photoUrl: String? = null,
    @field:JvmField
    var isDone: Boolean? = null,
    var createdAt: Timestamp? = null
) : Parcelable