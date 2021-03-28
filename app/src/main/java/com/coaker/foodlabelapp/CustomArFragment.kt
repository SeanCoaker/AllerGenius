package com.coaker.foodlabelapp


import com.google.ar.core.*
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment: ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        session?.configure(config)
        arSceneView.setupSession(session)
        return config
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }

}

