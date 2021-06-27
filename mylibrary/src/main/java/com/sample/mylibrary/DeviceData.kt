package com.sample.mylibrary

import android.os.Parcel
import android.os.Parcelable

class DeviceData():Parcelable {
     var azimuth: Int = 0
     var pitch : Int = 0
     var roll : Int = 0

    constructor(azimuth: Int, pitch: Int,roll : Int) : this() {
        this.azimuth = azimuth
        this.pitch = pitch
        this.roll = roll
    }

    constructor(parcel: Parcel) : this() {
        azimuth = parcel.readInt()
        pitch = parcel.readInt()
        roll = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(azimuth)
        parcel.writeInt(pitch)
        parcel.writeInt(roll)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun readFromParcel(parcel: Parcel) {
        azimuth = parcel.readInt()
        pitch = parcel.readInt()
        roll = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<DeviceData> {
        override fun createFromParcel(parcel: Parcel): DeviceData {
            return DeviceData(parcel)
        }

        override fun newArray(size: Int): Array<DeviceData?> {
            return arrayOfNulls(size)
        }
    }
}