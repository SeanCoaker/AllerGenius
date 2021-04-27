package com.coaker.foodlabelapp


import com.google.ar.core.*
import com.google.ar.sceneform.ux.ArFragment

/**
 * A class made to customise certain aspects of the ARCore session in order for it to work more
 * effectively with this application.
 *
 * @author Sean Coaker
 * @since 1.0
 */
class CustomArFragment: ArFragment() {

    /**
     * A function used to set specific configurations for the ARCore session
     *
     * @param[session] The current ARCore session
     * @return[Config] A config class instance
     */
    override fun getSessionConfiguration(session: Session?): Config {
        // Hides the hand floating instruction
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)
        // Sets auto focus for ARCore
        config.focusMode = Config.FocusMode.AUTO
        // Disables the use of plane finding
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        session?.configure(config)
        arSceneView.setupSession(session)
        return config
    }


    /**
     * A function called when the window focus changes. I have kept this blank in order to stop
     * ARCore from going into full screen.
     *
     * @param[hasFocus] Whether the window is currently in focus or not
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }

}

