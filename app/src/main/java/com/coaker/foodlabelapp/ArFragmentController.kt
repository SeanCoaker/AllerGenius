package com.coaker.foodlabelapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
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

class ArFragmentController : Fragment() {

    private val client = OkHttpClient()

    private lateinit var root: View
    private lateinit var arFragment: CustomArFragment
    private lateinit var parent: MainActivity
    private lateinit var arSwitch: SwitchCompat


    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

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


    // Code from https://developers.google.com/ar/develop/java/enable-arcore#ar-optional
    private fun setupArButton() {
        arSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                parent.switchToCamFragment()
            }
        }

    }


    override fun onResume() {
        super.onResume()

        arFragment.setOnSessionInitializationListener {

            root.findViewById<Button>(R.id.scanButton).setOnClickListener {
                Log.i("AR", "Worked")

                val image = arFragment.arSceneView.arFrame?.acquireCameraImage()
                val barcodeScanner = BarcodeScanning.getClient()
                barcodeScanner.process(
                    InputImage.fromMediaImage(
                        image!!,
                        getRotationCompensation()
                    )
                )
                    .addOnSuccessListener {
                        if (it != null && it.size > 0) {
                            for (barcode in it) {
                                val pos = floatArrayOf(0f, 0f, -1.5f)
                                val rot = floatArrayOf(0f, 0f, 0f, 0f)

                                setupLabelData(
                                    barcode.rawValue,
                                    arFragment,
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


    private fun getRotationCompensation(): Int {
        val deviceRotation = requireContext().display!!.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        val cameraManager = parent.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        val sensorOrientation = cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360

        return rotationCompensation
    }


    private fun placeLabel(
        fragment: CustomArFragment,
        anchor: Anchor,
        productName: String,
        barcode: String,
        foodScore: String
    ) {
        ViewRenderable.builder()
            .setView(requireContext(), R.layout.ar_label)
            .build()
            .thenAccept {
//                it.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                it.view.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
                it.isShadowReceiver = false
                it.isShadowCaster = false
                it.view.findViewById<TextView>(R.id.productNameText).text = productName
                it.view.findViewById<TextView>(R.id.barcodeIdText).text = barcode

                val foodScoreText = it.view.findViewById<TextView>(R.id.textViewFoodScore)

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

                addLabelToScene(fragment, anchor, it)
            }
    }


    private fun addLabelToScene(
        fragment: CustomArFragment,
        anchor: Anchor,
        renderable: Renderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
//        node.localRotation = Quaternion.axisAngle(Vector3(-1.0f, 0f, 0f), 90f)
        fragment.arSceneView.scene.addChild(anchorNode)
    }


    private fun setupLabelData(rawValue: String?, fragment: CustomArFragment, anchor: Anchor) {
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

    private fun getProduct(rawValue: String?): JSONObject {
        val url = "https://en.openfoodfacts.org/api/v0/product/$rawValue.json"
        val request = Request.Builder().url(url).build()

        val response: String = client.newCall(request).execute().body()!!.string()

        return JSONObject(response)
    }


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


    private fun showBottomSheet(rawValue: String) {
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