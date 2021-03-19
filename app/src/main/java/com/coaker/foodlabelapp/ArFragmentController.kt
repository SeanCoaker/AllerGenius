package com.coaker.foodlabelapp

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArFragmentController: Fragment() {

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
            arFragment.arSceneView.setOnClickListener {
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
                                placeLabel(
                                    arFragment, arFragment.arSceneView.session!!.createAnchor(
                                        arFragment.arSceneView.arFrame!!.camera.pose.compose(
                                            Pose.makeTranslation(
                                                0F, 0F, -3f
                                            )
                                        ).extractTranslation()
                                    ), "Worked", barcode.rawValue!!
                                )
                            }
                        }
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
        barcode: String
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
}