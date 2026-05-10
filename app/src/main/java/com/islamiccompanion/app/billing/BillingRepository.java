package com.islamiccompanion.app.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.islamiccompanion.app.BuildConfig;
import com.islamiccompanion.app.util.AdHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all Google Play Billing interactions (v7 API).
 *
 * SKUs (configure these in Play Console before launch — see playstore/iap_setup.md):
 *  - {@code remove_ads_lifetime}  — one-time $4.99 non-consumable, grants lifetime ad removal.
 *  - {@code sadaqah_1_usd}        — consumable donation $1.
 *  - {@code sadaqah_3_usd}        — consumable donation $3.
 *  - {@code sadaqah_5_usd}        — consumable donation $5.
 *
 * IMPORTANT: Purchases must be acknowledged within 3 days or they are automatically refunded.
 * This repository acknowledges immediately after grant verification.
 *
 * TODO (before launch):
 *  - Create all SKUs in the Play Console Internal app sharing or Production tracks.
 *  - Test with a licensed test account before enabling in production.
 *  - Wire up UMP consent — do not call MobileAds until consent obtained.
 */
public class BillingRepository implements PurchasesUpdatedListener {

    private static final String TAG = "BillingRepository";

    private final Context context;
    private BillingClient billingClient;

    private final MutableLiveData<BillingState> stateLive =
            new MutableLiveData<>(BillingState.DISCONNECTED);
    private final MutableLiveData<String>  messageLive  = new MutableLiveData<>();
    private final MutableLiveData<Boolean> adsRemovedLive = new MutableLiveData<>(false);

    private ProductDetails removeAdsProduct;

    public enum BillingState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    public BillingRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    // ── Connection ────────────────────────────────────────────────────────────

    public void connect() {
        stateLive.setValue(BillingState.CONNECTING);
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .build())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    stateLive.postValue(BillingState.CONNECTED);
                    queryProducts();
                    restorePurchases();
                } else {
                    stateLive.postValue(BillingState.ERROR);
                    Log.w(TAG, "Billing setup failed: " + result.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                stateLive.postValue(BillingState.DISCONNECTED);
            }
        });
    }

    public void disconnect() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
    }

    // ── Product query ─────────────────────────────────────────────────────────

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        // Non-consumable (lifetime)
        products.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.IAP_REMOVE_ADS_SKU)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        // Consumable donations
        for (String sku : new String[]{
                BuildConfig.IAP_SADAQAH_1_SKU,
                BuildConfig.IAP_SADAQAH_3_SKU,
                BuildConfig.IAP_SADAQAH_5_SKU }) {
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }

        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(products).build(),
                (result, productDetailsList) -> {
                    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                    for (ProductDetails pd : productDetailsList) {
                        if (BuildConfig.IAP_REMOVE_ADS_SKU.equals(pd.getProductId())) {
                            removeAdsProduct = pd;
                        }
                    }
                });
    }

    // ── Purchase flow ─────────────────────────────────────────────────────────

    /** Launch the "Remove Ads" purchase flow. Call from Settings. */
    public void launchRemoveAdsPurchase(Activity activity) {
        if (removeAdsProduct == null) {
            messageLive.postValue("Purchase unavailable — please try again shortly.");
            return;
        }
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(List.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(removeAdsProduct)
                                .build()))
                .build();
        billingClient.launchBillingFlow(activity, params);
    }

    /** Launch a sadaqah (donation) purchase. */
    public void launchSadaqahPurchase(Activity activity, String sku) {
        // TODO: query product details for the specific sadaqah SKU then launch flow
        messageLive.postValue("JazakAllah Khair — donation feature coming soon.");
    }

    // ── Purchase result handler ───────────────────────────────────────────────

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult result,
                                    List<Purchase> purchases) {
        int code = result.getResponseCode();
        if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase p : purchases) {
                handlePurchase(p);
            }
        } else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Purchase cancelled by user");
        } else {
            Log.w(TAG, "Purchase error: " + result.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;

        if (purchase.getProducts().contains(BuildConfig.IAP_REMOVE_ADS_SKU)) {
            // Acknowledge the non-consumable (required within 3 days)
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(params, ackResult -> {
                    if (ackResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        AdHelper.setAdsRemovedLifetime(context);
                        adsRemovedLive.postValue(true);
                        messageLive.postValue("Ads removed. JazakAllah Khair! 🤲");
                    }
                });
            } else {
                AdHelper.setAdsRemovedLifetime(context);
                adsRemovedLive.postValue(true);
            }
        } else {
            // Sadaqah — consumable (consume so user can donate again)
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.consumeAsync(consumeParams, (consumeResult, token) -> {
                if (consumeResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    messageLive.postValue("JazakAllah Khair for your sadaqah! 🤲");
                }
            });
        }
    }

    // ── Restore purchases ─────────────────────────────────────────────────────

    /** Re-query Play for existing purchases and restore the ads-removed state if found. */
    public void restorePurchases() {
        if (billingClient == null || !billingClient.isReady()) return;
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (result, purchases) -> {
                    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                    for (Purchase p : purchases) {
                        if (p.getProducts().contains(BuildConfig.IAP_REMOVE_ADS_SKU)
                                && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            AdHelper.setAdsRemovedLifetime(context);
                            adsRemovedLive.postValue(true);
                        }
                    }
                });
    }

    // ── Observables ───────────────────────────────────────────────────────────

    public LiveData<BillingState> getState()      { return stateLive; }
    public LiveData<String>       getMessage()     { return messageLive; }
    public LiveData<Boolean>      getAdsRemoved()  { return adsRemovedLive; }
}
