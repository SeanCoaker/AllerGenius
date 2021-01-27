package com.coaker.foodlabelapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias ValueListener = (rawValue: String, displayValue: String) -> Unit

class ScannerFragment : Fragment() {

    private val client = OkHttpClient()

    private var imageAnalyser: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var parent: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        parent = activity as MainActivity

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(parent, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageAnalyser = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(1280, 720))
                .build()
            setupAnalyser()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyser)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }



        }, ContextCompat.getMainExecutor(context))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            parent.baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showBottomSheet(rawValue: String, displayValue: String) {
        var response: JSONObject? = null
        var product: JSONObject?

        var productId = ""
        var brand = ""
        var productName = ""

        var energyUnit = ""

        var servingSize = ""
        var energy100 = 0.0
        var fat100 = 0.0
        var saturatedFat100 = 0.0
        var carbs100 = 0.0
        var sugar100 = 0.0
        var fibre100 = 0.0
        var protein100 = 0.0
        var salt100 = 0.0

        var ingredients = ""
        var additives = ""

        var allergens = ""
        var traces = ""

        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                response = getProduct(rawValue)
            }
            operation.await()

            val bottomSheet = BottomSheetBarcodeResult(this@ScannerFragment)
            val bundle = Bundle()

            val status = response!!.getInt("status")

            if (status == 1) {
                product = response!!.getJSONObject("product")

                println(product!!.toString(5))

                productId = product!!.getString("id")

                brand = product!!.getString("brands")
                productName = product!!.getString("product_name")

                val nutriments = product!!.getJSONObject("nutriments")

                energyUnit = nutriments.getString("energy_unit")

                energy100 = if (energyUnit == "kJ") {
                    nutriments.getDouble("energy")
                } else {
                    nutriments.getDouble("energy-kcal_100g")
                }

                servingSize = product!!.getString("serving_size")
                fat100 = nutriments.getDouble("fat_100g")
                saturatedFat100 = nutriments.getDouble("saturated-fat_100g")
                carbs100 = nutriments.getDouble("carbohydrates_100g")
                sugar100 = nutriments.getDouble("sugars_100g")
                fibre100 = nutriments.getDouble("fiber_100g")
                protein100 = nutriments.getDouble("proteins_100g")
                salt100 = nutriments.getDouble("salt_100g")

                ingredients = product!!.getString("ingredients_text")

                val additivesArray = product!!.getJSONArray("additives_tags")
                for (i in 0 until additivesArray.length()) {
                    val additive = additivesArray.getString(i).removeRange(0,3).capitalize(Locale.UK)
                    additives += "$additive\n"
                }

                allergens = product!!.getString("allergens")
                traces = product!!.getString("traces")
            }

            bundle.putString("productId", productId)
            bundle.putString("displayValue", displayValue)

            bundle.putString("brand", brand)
            bundle.putString("product_name", productName)

            bundle.putString("servingSize", servingSize)
            bundle.putString("energyUnit", energyUnit)
            bundle.putDouble("energy100", energy100)
            bundle.putDouble("fat100", fat100)
            bundle.putDouble("saturatedFat100", saturatedFat100)
            bundle.putDouble("carbs100", carbs100)
            bundle.putDouble("sugar100", sugar100)
            bundle.putDouble("fibre100", fibre100)
            bundle.putDouble("protein100", protein100)
            bundle.putDouble("salt100", salt100)

            bundle.putString("ingredients", ingredients)
            bundle.putString("additives", additives)

            bundle.putString("allergens", allergens)
            bundle.putString("traces", traces)

            bottomSheet.arguments = bundle
            bottomSheet.show(parent.supportFragmentManager, TAG)
        }
    }

    private fun getProduct(rawValue: String): JSONObject {
        val url = "https://en.openfoodfacts.org/api/v0/product/$rawValue.json"
        val request = Request.Builder().url(url).build()

        val response: String = client.newCall(request).execute().body()!!.string()

        return JSONObject(response)
    }

    fun setupAnalyser() {
        imageAnalyser!!.setAnalyzer(cameraExecutor, BarcodeAnalyser { rawValue, displayValue ->
            showBottomSheet(rawValue, displayValue)
            imageAnalyser!!.clearAnalyzer()
        })
    }

    private class BarcodeAnalyser(private val listener: ValueListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val scanner = BarcodeScanning.getClient()

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->

                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue
                            val displayValue = barcode.displayValue

                            listener(rawValue!!, displayValue!!)

                            imageProxy.close()
                        }
                    }

                    .addOnFailureListener {
                        Log.i("ID: ", "Failed")
                        imageProxy.close()
                    }

                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }
}