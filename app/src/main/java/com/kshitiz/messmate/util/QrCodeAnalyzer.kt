package com.kshitiz.messmate.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader: MultiFormatReader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        try {
            val bufferY = image.planes[0].buffer
            val dataY = ByteArray(bufferY.remaining())
            bufferY.get(dataY)

            val width = image.width
            val height = image.height

            val source: LuminanceSource = PlanarYUVLuminanceSource(
                dataY,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result: Result? = try {
                reader.decodeWithState(binaryBitmap)
            } catch (_: Exception) {
                null
            } finally {
                reader.reset()
            }

            if (result != null) {
                onQrCodeScanned(result.text)
            }
        } finally {
            image.close()
        }
    }
}
