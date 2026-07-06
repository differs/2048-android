package com.differs.game2048.billing

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** A purchasable product. */
data class Product(
    val id: String,
    val title: String,
    val price: String
)

/**
 * Monetization seam for in-app purchases. This is a stub so the app ships
 * without the Google Play Billing library. Swap in a real implementation to
 * enable "Remove Ads" and other purchases.
 */
interface BillingManager {
    /** Whether the user has purchased the ad-free upgrade. */
    val isPremium: StateFlow<Boolean>

    /** Products available for purchase. */
    fun availableProducts(): List<Product>

    /** Launch the purchase flow for [productId]. */
    fun purchase(activity: Activity, productId: String)

    /** Restore previously owned purchases. */
    fun restorePurchases()

    companion object {
        const val PRODUCT_REMOVE_ADS = "remove_ads"
        const val PRODUCT_THEME_PACK = "theme_pack_pro"
    }
}

/** Default stub: nothing is owned and purchases are ignored. */
class NoOpBillingManager : BillingManager {
    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium

    override fun availableProducts(): List<Product> = listOf(
        Product(BillingManager.PRODUCT_REMOVE_ADS, "Remove Ads", "$2.99"),
        Product(BillingManager.PRODUCT_THEME_PACK, "Pro Theme Pack", "$1.99")
    )

    override fun purchase(activity: Activity, productId: String) {
        Log.d(TAG, "purchase($productId) (no-op)")
    }

    override fun restorePurchases() {
        Log.d(TAG, "restorePurchases (no-op)")
    }

    private companion object {
        const val TAG = "NoOpBillingManager"
    }
}
