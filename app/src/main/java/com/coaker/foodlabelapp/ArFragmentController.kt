package com.coaker.foodlabelapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*

/**
 * A class used to run the augmented reality feature in the application.
 *
 * @author Sean Coaker
 * @since 1.0
 */
class ArFragmentController : Fragment() {

    private val client = OkHttpClient()

    private lateinit var root: View
    private lateinit var arFragment: CustomArFragment
    private lateinit var parent: MainActivity
    private lateinit var arSwitch: SwitchCompat


    private val ORIENTATIONS = SparseIntArray()

    // Rotations used for calculating where to display augmented labels.
    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }


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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.ar_scanner, container, false)

        parent = activity as MainActivity

        arSwitch = root.findViewById(R.id.arSwitchAR)
        arSwitch.isChecked = true

        arFragment =
            childFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment


        setupArButton()

        return root
    }


    /**
     * A function used to setup the AR button in the camera view.
     *
     * Reference: Code from https://developers.google.com/ar/develop/java/enable-arcore#ar-optional
     */
    private fun setupArButton() {
        arSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // Switches back to usual barcode scanning using CameraX API.
                parent.switchToCamFragment()
            }
        }

    }


    /**
     * A function called when the fragment resumes from a paused state.
     */
    override fun onResume() {
        super.onResume()

        arFragment.setOnSessionInitializationListener {

            root.findViewById<Button>(R.id.scanButton).setOnClickListener {

                val image = arFragment.arSceneView.arFrame?.acquireCameraImage()
                val barcodeScanner = BarcodeScanning.getClient()

                // Processes barcodes identified in the image from the AR camera.
                barcodeScanner.process(
                    InputImage.fromMediaImage(
                        image!!,
                        getRotationCompensation()
                    )
                )
                    .addOnSuccessListener {
                        if (it != null && it.size > 0) {
                            for (barcode in it) {
                                // Creates a position at 1.5 metres away and at the centre of the camera.
                                val pos = floatArrayOf(0f, 0f, -1.5f)
                                // Creates a rotation identical to the device's rotation.
                                val rot = floatArrayOf(0f, 0f, 0f, 0f)

                                setupLabelData(
                                    barcode.rawValue,
                                    arFragment,
                                    // Creates an ARCore anchor.
                                    arFragment.arSceneView.session!!.createAnchor(
                                        arFragment.arSceneView.arFrame!!.camera.pose.compose(
                                            Pose(pos, rot)
                                        ).extractTranslation(),
                                    )
                                )
                            }
                        }

                        image.close()
                    }

                    .addOnFailureListener {
                        image.close()
                    }
            }
        }
    }


    /**
     * A function called when the fragemnt is being stopped.
     *
     * Reference: https://stackoverflow.com/questions/59103282/how-to-reset-arcore
     */
    override fun onStop() {
        super.onStop()
        val session = arFragment.arSceneView.session

        if (session != null) {

            GlobalScope.launch {     // launch a new coroutine in BG and continue
                delay(2000L)         // non-blocking delay for 2 sec
                session.close()
                println("Closing...")
            }
            session.pause()
            println("Paused...")     // main thread continues while coroutine's delayed
            Thread.sleep(2000L)      // block main thread for 2 sec to keep JVM alive
        }
    }


    /**
     * A function called to get the rotation compensation from the camera's orientation.
     *
     * Reference: https://firebase.google.com/docs/ml-kit/android/recognize-text#kotlin+ktx_1
     * 
     * return[Int] Return the rotation compensation.
     */
    private fun getRotationCompensation(): Int {
        // Gets portrait orientation
        var rotationCompensation = ORIENTATIONS.get(0)

        val cameraManager = parent.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        val sensorOrientation = cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Creates the rotation compensation based on the rear camera.
        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360

        return rotationCompensation
    }


    /**
     * A fragment used to place the label on the AR camera view.
     *
     * Reference: https://kristisimakova.medium.com/how-to-add-ui-elements-to-ar-scene-in-arcore-d2ba64454478
     * 
     * @param[fragment] The AR fragment.
     * @param[anchor] The anchor used to display labels on.
     * @param[productName] The name of the product that has been scanned.
     * @param[barcode] The barcode value of the product that has been scanned.
     * @param[foodScore] The calculated food score of the product that has been scanned.
     */
    private fun placeLabel(
        fragment: CustomArFragment,
        anchor: Anchor,
        productName: String,
        barcode: String,
        foodScore: String
    ) {
        // Builds a renderable to be displayed in AR.
        ViewRenderable.builder()
            .setView(requireContext(), R.layout.ar_label)
            .build()
            .thenAccept {
                // Creates a rounded shape for the renderable.
                it.view.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
                it.isShadowReceiver = false
                it.isShadowCaster = false
                it.view.findViewById<TextView>(R.id.productNameText).text = productName
                it.view.findViewById<TextView>(R.id.barcodeIdText).text = barcode
                val crossButton: ImageButton = it.view.findViewById(R.id.crossButtonAR)

                val foodScoreText = it.view.findViewById<TextView>(R.id.textViewFoodScore)

                // Sets the correct colour for each food score.
                when (foodScore) {
                    "A" -> {
                        foodScoreText.text = foodScore
                        foodScoreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        foodScoreText.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightA))
                    }

                    "B" -> {
                        foodScoreText.text = foodScore
                        foodScoreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        foodScoreText.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightB))
                    }

                    "C" -> {
                        foodScoreText.text = foodScore
                        foodScoreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        foodScoreText.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightC))
                    }

                    "X" -> {
                        foodScoreText.text = foodScore
                        foodScoreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        foodScoreText.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(Variables.trafficLightX))
                    }

                    "?" -> {
                        foodScoreText.text = foodScore
                        foodScoreText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        foodScoreText.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }

                it.view.setOnClickListener {
                    showBottomSheet(barcode)
                }

                crossButton.setOnClickListener {
                    anchor.detach()
                }

                addLabelToScene(fragment, anchor, it)
            }
    }


    /**
     * A function called to add the label to AR view.
     *
     * Reference: https://kristisimakova.medium.com/how-to-add-ui-elements-to-ar-scene-in-arcore-d2ba64454478
     * 
     * @param[fragment] The AR fragment.
     * @param[anchor] The anchor to display the label onto.
     * @param[renderable] The renderable to be displayed to the AR view.
     */
    private fun addLabelToScene(
        fragment: CustomArFragment,
        anchor: Anchor,
        renderable: Renderable
    ) {

        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)

        node.localPosition = Vector3(0f, 0f, -0.5f)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
    }


    /**
     * A function called to setup the data needed to appropriately display the label in AR view.
     * 
     * @param[rawValue] The value of the barcode to be searched for.
     * @param[fragment] The AR fragment.
     * @param[anchor] The anchor to display the label onto. 
     */
    private fun setupLabelData(rawValue: String?, fragment: CustomArFragment, anchor: Anchor) {
        var response: JSONObject? = null
        var product: JSONObject?

        var productName: String

        var fat100: Double
        var saturatedFat100: Double
        var sugar100: Double
        var salt100: Double

        var ingredients: String
        var additives = ""

        // Fetches the JSON data on a seperate thread to the UI thread.
        lifecycleScope.launch {

            try {
                val operation = async(Dispatchers.IO) {
                    response = getProduct(rawValue)
                }
                operation.await()

                val status = response!!.getInt("status")

                if (status == 1) {
                    product = response!!.getJSONObject("product")

                    productName = product!!.getString("product_name")

                    val nutriments = product!!.getJSONObject("nutriments")

                    fat100 = nutriments.getDouble("fat_100g")
                    saturatedFat100 = nutriments.getDouble("saturated-fat_100g")
                    sugar100 = nutriments.getDouble("sugars_100g")
                    salt100 = nutriments.getDouble("salt_100g")

                    ingredients = product!!.getString("ingredients_text")

                    val additivesArray = product!!.getJSONArray("additives_tags")
                    for (i in 0 until additivesArray.length()) {
                        val additive =
                            additivesArray.getString(i).removeRange(0, 3).capitalize(Locale.UK)
                        additives += "$additive\n"
                    }

                    // Sets the food score to X if one of the user's allergens is found in the ingredients.
                    val foodScore: String = if (findAllergens(ingredients)) {
                        "X"
                    } else {
                        calculateFoodScore(fat100, saturatedFat100, sugar100, salt100)
                    }

                    placeLabel(fragment, anchor, productName, rawValue!!, foodScore)

                } else {

                    placeLabel(fragment, anchor, "Not Found", rawValue!!, "?")
                }

            } catch (e: org.json.JSONException) {

            }
        }
    }


    /**
     * A function that fetches the data associated with the barcode from the open food facts database.
     * 
     * @param[rawValue] The value of the barcode that was scanned.
     * @return[JSONObject] The JSON object returned from the http request.
     */
    private fun getProduct(rawValue: String?): JSONObject {
        val url = "https://en.openfoodfacts.org/api/v0/product/$rawValue.json"
        val request = Request.Builder().url(url).build()

        val response: String = client.newCall(request).execute().body()!!.string()

        return JSONObject(response)
    }


    /**
     * A function used to calculate the food score of a product. It uses a point based system to calculate 
     * a food score for the product.
     * 
     * @param[fat] The fat per 100g in the food product
     * @param[saturates] The saturated fat per 100g in the food product
     * @param[sugar] The sugar per 100g in the food product
     * @param[salt] The salt per 100g in the food product
     * @return[String] The food score assigned to the product
     */
    private fun calculateFoodScore(
        fat: Double,
        saturates: Double,
        sugar: Double,
        salt: Double
    ): String {
        var greenValue = 0
        var amberValue = 0
        var redValue = 0

        when {
            fat <= Variables.fatLow -> greenValue++
            fat > Variables.fatHigh -> redValue++
            else -> amberValue++
        }

        when {
            saturates <= Variables.saturatesLow -> greenValue++
            saturates > Variables.saturatesHigh -> redValue++
            else -> amberValue++
        }

        when {
            sugar <= Variables.sugarLow -> greenValue++
            sugar > Variables.sugarHigh -> redValue++
            else -> amberValue++
        }

        when {
            salt <= Variables.saltLow -> greenValue++
            salt > Variables.saltHigh -> redValue++
            else -> amberValue++
        }

        when (greenValue) {
            4 -> {
                return "A"
            }
            3 -> {
                return if (redValue == 1) {
                    "B"
                } else {
                    "A"
                }
            }
            2 -> {
                return "B"
            }
            1 -> {
                return if (amberValue >= 2) {
                    "B"
                } else {
                    "C"
                }
            }
            else -> {
                return "C"
            }
        }
    }


    /**
     * A function used to display the digital food label as a bottom sheet.
     * 
     * @param[rawValue] The barcode of the food product.
     */
    private fun showBottomSheet(rawValue: String) {
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

            val bottomSheet = BottomSheetBarcodeResult(null)
            val bundle = Bundle()

            try {
                val operation = async(Dispatchers.IO) {
                    response = getProduct(rawValue)
                }
                operation.await()

                val status = response!!.getInt("status")

                if (status == 1) {
                    product = response!!.getJSONObject("product")

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
                bundle.putString("displayValue", productId)

                bottomSheet.arguments = bundle
                bottomSheet.show(parent.supportFragmentManager, TAG)
            }
        }
    }


    companion object {
        private const val TAG = "AR"
    }


    /**
     * A function called to find the user's allergens in the food product's ingredients.
     * 
     * @param[ingredients] The list of ingredients to search through
     * @return[Boolean] True if an allergen was found, False otherwise
     */
    private fun findAllergens(ingredients: String): Boolean {
        val ingredientList = ingredients.split(",")

        ingredientList.forEach { ingredient ->
            Variables.allergyList.forEach { allergy ->
                if (ingredient.contains(allergy, true)) {
                    return true
                }
            }
        }

        return false
    }
}