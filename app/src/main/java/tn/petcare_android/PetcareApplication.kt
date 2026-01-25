package tn.petcare_android

import android.app.Application
import com.stripe.android.PaymentConfiguration
import tn.petcare_android.util.Constants

/**
 * Application class for global initialization
 */
class PetcareApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Stripe SDK with publishable key
        PaymentConfiguration.init(
            applicationContext,
            Constants.Stripe.PUBLISHABLE_KEY
        )
    }
}
