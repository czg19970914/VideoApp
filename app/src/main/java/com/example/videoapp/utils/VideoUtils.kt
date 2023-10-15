package com.example.videoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.ywl5320.wlmedia.WlMediaUtil
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets

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

        @JvmStatic
        fun getVideoImage(url: String, mediaUtil: WlMediaUtil, bitmap: Bitmap): Bitmap{
            mediaUtil.setSource(url)
            var res = mediaUtil.openSource()
            if(res == 0) {
                res = mediaUtil.openCodec()
                if(res == 0) {
                    return mediaUtil.getVideoImg(0.0, false)
                }
            }
            mediaUtil.release()
            return bitmap
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
        fun parseJSON(jsonStream: InputStream): JsonArray {
            val writer: Writer = StringWriter()
            val buffer: CharArray = CharArray(jsonStream.available())
            val reader: Reader = BufferedReader(InputStreamReader(jsonStream, StandardCharsets.UTF_8))

            var n = -1
            while (true) {
                n = reader.read(buffer)
                if (n == -1) {
                    break
                } else {
                    writer.write(buffer, 0, n)
                }
            }
            val json = writer.toString()
            reader.close()
            writer.close()
            return Gson().fromJson(json, JsonArray::class.java)
        }

        fun parseJSONtoDict(jsonStream: InputStream): JSONObject {
            val writer: Writer = StringWriter()
            val buffer: CharArray = CharArray(jsonStream.available())
            val reader: Reader = BufferedReader(InputStreamReader(jsonStream, StandardCharsets.UTF_8))

            var n = -1
            while (true) {
                n = reader.read(buffer)
                if (n == -1) {
                    break
                } else {
                    writer.write(buffer, 0, n)
                }
            }
            val json = writer.toString()
            reader.close()
            writer.close()
            return JSONObject(json)
        }
    }
}