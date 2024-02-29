package com.example.videoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.google.gson.Gson
import java.io.*
import android.util.Base64
import android.util.Log
import com.example.videoapp.entities.VideoDescriptionEntity
import com.google.gson.reflect.TypeToken

class VideoUtils {
    companion object {
        @JvmStatic
        fun calculateTime(time: Int): String {
            var minute = 0
            var second = 0
            if (time >= 60) {
                minute = time / 60;
                second = time % 60;
                //分钟在0~9
                return if (minute < 10) {
                    //判断秒
                    if (second < 10) {
                        "0$minute:0$second";
                    } else {
                        "0$minute:$second";
                    }
                } else {
                    //分钟大于10再判断秒
                    if (second < 10) {
                        "$minute:0$second";
                    } else {
                        "$minute:$second";
                    }
                }
            } else {
                second = time;
                return if (second in 0..9) {
                    "00:0$second";
                } else {
                    "00:$second";
                }
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @JvmStatic
        fun vectorDrawableToBitmap(context: Context, vectorDrawableId: Int): Bitmap {
            val vectorDrawable = context.getDrawable(vectorDrawableId)
            val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)

            return bitmap
        }

        @JvmStatic
        fun saveDescriptionToJson(map: Map<String, List<VideoDescriptionEntity>>, file: File) {
            val jsonStr = Gson().toJson(map)
            val outputStream = FileOutputStream(file)
            try {
                outputStream.write(jsonStr.toByteArray())
            } catch (e : IOException) {
                Log.i("czg", "saveDescriptionToJson:")
            } finally {
                outputStream.close()
            }
        }

        @JvmStatic
        fun getJsonToMap(file: File): Map<String, List<VideoDescriptionEntity>> {
            val json = file.readText()
            val type = object : TypeToken<Map<String, List<VideoDescriptionEntity>>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun base64StrToBitmap(base64String: String): Bitmap {
            val dec = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(dec, 0, dec.size)
            return bitmap
        }
    }
}