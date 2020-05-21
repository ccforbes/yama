package edu.uw.lho12.yama

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SmsInfo(
    val author: String,
    val message: String,
    val datetime: String
) : Parcelable