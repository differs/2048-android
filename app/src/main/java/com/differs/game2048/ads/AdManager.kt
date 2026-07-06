package com.differs.game2048.ads

import android.app.Activity
import android.util.Log

/**
 * Monetization seam for ads. This is a NO-OP stub so the app ships without any
 * third-party ad SDK. To integrate a real provider (e.g. Google AdMob), provide
 * an alternative [AdManager] implementation and swap it in [com.differs.game2048.di].
 */
interface AdManager {
    /** Preload an interstitial so it is ready to show later. */
    fun preloadInterstitial(activity: Activity)

    /**
     * Show an interstitial if one is ready. [onDismissed] is always invoked
     * (immediately when no ad is available) so game flow is never blocked.
     */
    fun showInterstitial(activity: Activity, onDismissed: () -> Unit)

    /**
     * Show a rewarded ad. [onReward] is invoked with `true` if the reward was
     * granted. The stub grants nothing and reports `false`.
     */
    fun showRewarded(activity: Activity, onReward: (Boolean) -> Unit)
}

/** Default no-op implementation used in the open-source build. */
class NoOpAdManager : AdManager {
    override fun preloadInterstitial(activity: Activity) {
        Log.d(TAG, "preloadInterstitial (no-op)")
    }

    override fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        Log.d(TAG, "showInterstitial (no-op)")
        onDismissed()
    }

    override fun showRewarded(activity: Activity, onReward: (Boolean) -> Unit) {
        Log.d(TAG, "showRewarded (no-op)")
        onReward(false)
    }

    private companion object {
        const val TAG = "NoOpAdManager"
    }
}
