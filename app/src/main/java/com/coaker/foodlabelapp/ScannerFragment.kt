package com.coaker.foodlabelapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.ar.core.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// A type alias used to pass data between ML Kit and this class
typealias ValueListener = (rawValue: String, displayValue: String) -> Unit

/**
 * A class created to handle the workings of the main camera barcode scanning feature
 *
 * @author Sean Coaker
 * @since 1.0
 */
class ScannerFragment : Fragment() {

    private var connected = true
    private val client = OkHttpClient()

    private var imageAnalyser: ImageAnalysis? = null

    private lateinit var cameraView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var parent: MainActivity
    private lateinit var root: View
    private lateinit var arSwitch: SwitchCompat


    /**
     * A function that is called when the fragment is created.
     *
     * @param[inflater] Inflater used to inflate the layout in this fragment.
     * @param[container] Contains the content of the fragment.
     * @param[savedInstanceState] Any previous saved instance of the fragment.
     *
     * @return[View] The view that has been created.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_scanner, container, false)

        parent = activity as MainActivity

        arSwitch = root.findViewById(R.id.arSwitchCam)

        cameraView = root.findViewById(R.id.viewFinder)

        promptChip = root.findViewById(R.id.instructionChip)
        promptChip.text = getString(R.string.scanner_instruction)

        // Only sets up camera scanning if the device is connected to a network
        if (Variables.isConnected) {

            checkARAvailability()

            // Request camera permissions
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    parent,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }

            cameraExecutor = Executors.newSingleThreadExecutor()

            setupArButton()
        } else {
            connected = false
            arSwitch.visibility = View.GONE
            cameraView.visibility = View.GONE
            promptChip.visibility = View.GONE
        }



        // Inflate the layout for this fragment
        return root
    }


    /**
     * Checks if AR is available on the device before allowing the user to switch to AR.
     *
     * @return[Boolean] Whether AR is available or not
     */
    private fun checkARAvailability(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(requireContext())

        if (availability.isTransient) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkARAvailability()
            }, 200)
        }

        if (availability.isSupported) {
            arSwitch.visibility = View.VISIBLE
            arSwitch.isEnabled = true
        } else {
            arSwitch.visibility = View.GONE
            arSwitch.isEnabled = false
        }

        return availability.isSupported
    }


    /**
     * A function used to setup the AR button displayed in this fragment so that when the AR switch
     * is pressed, the app switches to AR mode.
     */
    private fun setupArButton() {
        arSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                parent.switchToArFragment()
            }
        }

    }


    /**
     * A function used to setup the camera for barcode scanning using ML Kit's ImageAnalysis.
     */
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
                    this, cameraSelector, preview, imageAnalyser
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }


        }, ContextCompat.getMainExecutor(context))
    }


    /**
     * Checks that all permissions are granted for camera access.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            parent.baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * A function to handle what happens to the fragment when it is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()

        if (connected) {
            cameraExecutor.shutdown()
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private lateinit var promptChip: Chip
        private var multiCodes: Boolean = false
    }


    /**
     * A function that handles what happens when the user returns from granting or not granting access
     * to their device's camera.
     *
     * @param[requestCode] The request code sent to the permission window
     * @param[permissions] The permissions asked for
     * @param[grantResults] The results of the permissions asked
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /**
     * A function used to display the digital food label as a bottom sheet.
     *
     * @param[rawValue] The barcode of the food product.
     * @param[displayValue] The name of the food item.
     */
    private fun showBottomSheet(rawValue: String, displayValue: String) {
        var response: JSONObject? = null
        var product: JSONObject?

        var productId = ""
        var brand: String
        var productName: String

        var energyUnit: String

        var servingSize: String
        var energy100: Double
        var fat100: Double
        var saturatedFat100: Double
        var carbs100: Double
        var sugar100: Double
        var fibre100: Double
        var protein100: Double
        var salt100: Double

        var ingredients: String
        var additives = ""

        var allergens: String
        var traces: String

        // Fetches the JSON data on a seperate thread to the UI thread.
        lifecycleScope.launch {

            val bottomSheet = BottomSheetBarcodeResult(this@ScannerFragment)
            val bundle = Bundle()

            try {
                val operation = async(Dispatchers.IO) {
                    response = getProduct(rawValue)
                }
                operation.await()

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
                        val additive =
                            additivesArray.getString(i).removeRange(0, 3).capitalize(Locale.UK)
                        additives += "$additive\n"
                    }

                    allergens = product!!.getString("allergens")
                    traces = product!!.getString("traces")

                    // Bundles all the data ready to be sent to the bottom sheet class to be displayed.
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
                } else {

                    bundle.clear()
                }

            } catch (e: org.json.JSONException) {

            } finally {

                bundle.putString("productId", productId)
                bundle.putString("displayValue", displayValue)

                bottomSheet.arguments = bundle
                bottomSheet.show(parent.supportFragmentManager, TAG)

            }
        }
    }


    /**
     * A function that fetches the data associated with the barcode from the open food facts database.
     *
     * @param[rawValue] The value of the barcode that was scanned.
     * @return[JSONObject] The JSON object returned from the http request.
     */
    private fun getProduct(rawValue: String): JSONObject {
        val url = "https://en.openfoodfacts.org/api/v0/product/$rawValue.json"
        val request = Request.Builder().url(url).build()

        val response: String = client.newCall(request).execute().body()!!.string()

        return JSONObject(response)
    }


    /**
     * A function called to setup the image analyser from ML Kit to scan for barcodes.
     */
    fun setupAnalyser() {
        imageAnalyser!!.setAnalyzer(cameraExecutor, BarcodeAnalyser { rawValue, displayValue ->

            // Prompt chips are shown with instructions to the viewer of how to use the feature
            if (Variables.isConnected) {
                if (multiCodes) {
                    promptChip.setTextColor(Color.RED)
                    promptChip.text = requireContext().getString(R.string.multi_barcode_error)
                } else {
                    promptChip.setTextColor(Color.BLACK)
                    promptChip.text = requireContext().getString(R.string.scanner_instruction)
                    showBottomSheet(rawValue, displayValue)
                }
            } else {
                promptChip.setTextColor(Color.RED)
                promptChip.text = requireContext().getString(R.string.network_connection_error)
            }

            imageAnalyser!!.clearAnalyzer()
        })
    }


    /**
     * A private class setup to handle the barcode scanning feature of the application.
     *
     * @param[listener] The listener data at the top of this file
     */
    private class BarcodeAnalyser(private val listener: ValueListener) : ImageAnalysis.Analyzer {

        /**
         * A function to convert ByteBuffer to a ByteArray
         *
         * @return[ByteArray] The result of the conversion
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }


        /**
         * A function called to analyse a barcode within an ImageProxy.
         *
         * Reference: https://developers.google.com/ml-kit/vision/barcode-scanning/android
         *
         * @param[imageProxy] The image proxy to be analysed
         */
        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                // Rotates the image based on the camera rotation used to create the image proxy
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val scanner = BarcodeScanning.getClient()

                // Scans the image for a barcode
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->

                        // Displays a food label if one barcode was found, displays an error otherwise
                        if (barcodes.size > 1) {
                            multiCodes = true
                            listener("", "")
                            imageProxy.close()

                        } else {
                            for (barcode in barcodes) {
                                multiCodes = false

                                val rawValue = barcode.rawValue
                                val displayValue = barcode.displayValue

                                listener(rawValue!!, displayValue!!)

                                imageProxy.close()
                            }

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