package com.example.perpusanaksholeh.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.perpusanaksholeh.databinding.ActivityScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var isScanned = false

    private var isContinuous = false
    private val scannedBarcodes = ArrayList<String>()
    private var lastScannedBarcode: String? = null
    private var lastScanTime = 0L

    companion object {
        const val EXTRA_SCAN_RESULT = "scan_result"
        const val EXTRA_IS_CONTINUOUS = "is_continuous"
        const val EXTRA_SCAN_RESULTS = "scan_results"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk scan barcode", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnBack.setOnClickListener {
            finish()
        }

        isContinuous = intent.getBooleanExtra(EXTRA_IS_CONTINUOUS, false)
        if (isContinuous) {
            binding.btnDone.visibility = android.view.View.VISIBLE
            binding.btnDone.setOnClickListener {
                val resultIntent = Intent().apply {
                    putStringArrayListExtra(EXTRA_SCAN_RESULTS, scannedBarcodes)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            updateDoneButtonText()
        }

        // Start scanning laser animation
        binding.scanFrame.post {
            val animator = android.animation.ObjectAnimator.ofFloat(
                binding.scanLaser,
                "translationY",
                0f,
                binding.scanFrame.height.toFloat()
            )
            animator.duration = 2000
            animator.repeatMode = android.animation.ValueAnimator.REVERSE
            animator.repeatCount = android.animation.ValueAnimator.INFINITE
            animator.start()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun updateDoneButtonText() {
        binding.btnDone.text = "Selesai (${scannedBarcodes.size})"
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImage(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (isScanned) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODABAR
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            val box = barcode.boundingBox
                            if (box != null) {
                                val imgW = imageProxy.width
                                val imgH = imageProxy.height
                                val centerX = box.centerX()
                                val centerY = box.centerY()
                                val minX = imgW * 0.2
                                val maxX = imgW * 0.8
                                val minY = imgH * 0.2
                                val maxY = imgH * 0.8
                                if (centerX < minX || centerX > maxX || centerY < minY || centerY > maxY) {
                                    continue
                                }
                            }

                            if (isContinuous) {
                                if (scannedBarcodes.contains(rawValue)) {
                                    continue
                                }
                                val now = System.currentTimeMillis()
                                if (rawValue == lastScannedBarcode && now - lastScanTime < 2000) {
                                    continue
                                }
                                lastScannedBarcode = rawValue
                                lastScanTime = now

                                scannedBarcodes.add(rawValue)
                                val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(100)
                                }
                                runOnUiThread {
                                    Toast.makeText(this, "Tercatat: $rawValue", Toast.LENGTH_SHORT).show()
                                    updateDoneButtonText()
                                }
                            } else {
                                if (!isScanned) {
                                    isScanned = true
                                    val resultIntent = Intent().apply {
                                        putExtra(EXTRA_SCAN_RESULT, rawValue)
                                    }
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
