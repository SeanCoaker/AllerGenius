package com.coaker.foodlabelapp

import android.app.Application

/**
 * A class to be run when the application is started.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class ApplicationStartup: Application() {


    /**
     * A function to be run when the application is started. This method starts the NetworkCallbackMonitor
     */
    override fun onCreate() {
        super.onCreate()
        NetworkCallbackMonitor(this).start()
    }


    /**
     * A function to be run when the application is terminated. This method stops the NetworkCallbackMonitor
     */
    override fun onTerminate() {
        super.onTerminate()
        NetworkCallbackMonitor(this).stop()
    }
}