package com.example.perpusanaksholeh.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.UUID

object BarcodeGenerator {

    /**
     * Generate barcode image (CODE_128) dari string.
     * @param content string yang di-encode ke barcode
     * @param width lebar barcode dalam pixel
     * @param height tinggi barcode dalam pixel
     * @return Bitmap barcode atau null jika gagal
     */
    fun generateBarcode(content: String, width: Int = 600, height: Int = 200): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.CODE_128,
                width,
                height,
                hints
            )

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK
                        else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate kode barcode unik.
     * Format: PREFIX-XXXXXXXX (8 karakter random uppercase)
     * @param prefix prefix untuk kode (mis: "STD" untuk siswa, "BK" untuk buku)
     */
    fun generateUniqueCode(prefix: String): String {
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        return "$prefix-${uuid.substring(0, 8)}"
    }
}
