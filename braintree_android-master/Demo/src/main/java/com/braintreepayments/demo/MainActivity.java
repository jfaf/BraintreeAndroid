package com.braintreepayments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.braintreepayments.api.models.LocalPaymentResult;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.VenmoAccountNonce;
import com.braintreepayments.api.models.VisaCheckoutNonce;
import com.google.android.gms.wallet.ShippingAddressRequirements;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import org.json.*;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends BaseActivity {

    static final String EXTRA_PAYMENT_RESULT = "payment_result";
    static final String EXTRA_DEVICE_DATA = "device_data";
    static final String EXTRA_COLLECT_DEVICE_DATA = "collect_device_data";

    private static final int DROP_IN_REQUEST = 1;
    private static final int GOOGLE_PAYMENT_REQUEST = 2;
    private static final int CARDS_REQUEST = 3;
    private static final int PAYPAL_REQUEST = 4;
    private static final int VENMO_REQUEST = 5;
    private static final int VISA_CHECKOUT_REQUEST = 6;
    private static final int LOCAL_PAYMENTS_REQUEST = 7;
    private static final int PAYPAL_TWO_FACTOR_REQUEST = 8;
    private static final int PREFERRED_PAYMENT_METHODS_REQUEST = 9;

    private static final String KEY_NONCE = "nonce";

    private PaymentMethodNonce mNonce;

    private ImageView mNonceIcon;
    private TextView mNonceString;
    private TextView mNonceDetails;
    private TextView mDeviceData;

    private Button mDropInButton;
    private Button mGooglePaymentButton;
    private Button mCardsButton;
    private Button mPayPalButton;
    private Button mVenmoButton;
    private Button mVisaCheckoutButton;
    private Button mCreateTransactionButton;
    private Button mLocalPaymentsButton;
    private Button mPayPalTwoFactorAuthButton;
    private Button mPreferredPaymentMethods;
    String mclientToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mNonceIcon = findViewById(R.id.nonce_icon);
        mNonceString = findViewById(R.id.nonce);
        mNonceDetails = findViewById(R.id.nonce_details);
        mDeviceData = findViewById(R.id.device_data);

        mDropInButton = findViewById(R.id.drop_in);
        mGooglePaymentButton = findViewById(R.id.google_payment);
        mCardsButton = findViewById(R.id.card);
        mPayPalButton = findViewById(R.id.paypal);
        mVenmoButton = findViewById(R.id.venmo);
        mVisaCheckoutButton = findViewById(R.id.visa_checkout);
        mLocalPaymentsButton = findViewById(R.id.local_payments);
        mPreferredPaymentMethods = findViewById(R.id.preferred_payment_methods);
        mCreateTransactionButton = findViewById(R.id.create_transaction);
        mPayPalTwoFactorAuthButton = findViewById(R.id.paypal_two_factor);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_NONCE)) {
                mNonce = savedInstanceState.getParcelable(KEY_NONCE);
            }
        }

        requestClientTokenFromServer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNonce != null) {
            outState.putParcelable(KEY_NONCE, mNonce);
        }
    }

    //jf implementation

    private void requestClientTokenFromServer(){

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://thecocktailbike.com/generate-client-token", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("Client token request", "onFailure: " + statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String clientToken) {
                 mclientToken = clientToken;
                 enableButtons(true);
            }
        });

    }

    public void launchDropIn(View v) {

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(mclientToken);
        startActivityForResult(dropInRequest.getIntent(this), DROP_IN_REQUEST);
    }


    public void launchCards(View v) {
        Intent intent = new Intent(this, CardActivity.class)
                .putExtra(EXTRA_COLLECT_DEVICE_DATA, Settings.shouldCollectDeviceData(this));
        startActivityForResult(intent, CARDS_REQUEST);
    }

    public void launchPayPal(View v) {
        Intent intent = new Intent(this, PayPalActivity.class)
                .putExtra(EXTRA_COLLECT_DEVICE_DATA, Settings.shouldCollectDeviceData(this));
        startActivityForResult(intent, PAYPAL_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == DROP_IN_REQUEST) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                displayNonce(result.getPaymentMethodNonce(), result.getDeviceData());
                postNonceToServer(result.getPaymentMethodNonce().getNonce());

            } else {
                Parcelable returnedData = data.getParcelableExtra(EXTRA_PAYMENT_RESULT);
                String deviceData = data.getStringExtra(EXTRA_DEVICE_DATA);
                if (returnedData instanceof PaymentMethodNonce) {
                    displayNonce((PaymentMethodNonce) returnedData, deviceData);
                }

                mCreateTransactionButton.setEnabled(true);
            }
        } else if (resultCode != RESULT_CANCELED) {
            showDialog(((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage());
        }
    }

    @Override
    protected void reset() {
        enableButtons(false);
        mCreateTransactionButton.setEnabled(false);

        clearNonce();
    }

    @Override
    protected void onAuthorizationFetched() {
        enableButtons(true);
    }

    private void displayNonce(PaymentMethodNonce paymentMethodNonce, String deviceData) {
        mNonce = paymentMethodNonce;
        Log.d("displayNonce", mNonce.getNonce());

        mNonceIcon.setImageResource(PaymentMethodType.forType(mNonce).getDrawable());
        mNonceIcon.setVisibility(VISIBLE);


        mNonceString.setText(getString(R.string.nonce_placeholder, mNonce.getNonce()));
        mNonceString.setVisibility(VISIBLE);

        String details = "";
        if (mNonce instanceof CardNonce) {
            details = CardActivity.getDisplayString((CardNonce) mNonce);
        } else if (mNonce instanceof PayPalAccountNonce) {
            details = PayPalActivity.getDisplayString((PayPalAccountNonce) mNonce);
        }

        mNonceDetails.setText(details);
        mNonceDetails.setVisibility(VISIBLE);

        mDeviceData.setText(getString(R.string.device_data_placeholder, deviceData));
        mDeviceData.setVisibility(VISIBLE);

        mCreateTransactionButton.setEnabled(true);
    }

    private void clearNonce() {
        mNonceIcon.setVisibility(GONE);
        mNonceString.setVisibility(GONE);
        mNonceDetails.setVisibility(GONE);
        mDeviceData.setVisibility(GONE);
        mCreateTransactionButton.setEnabled(false);
    }

    private void enableButtons(boolean enable) {
        mDropInButton.setEnabled(enable);
        mGooglePaymentButton.setEnabled(enable);
        mCardsButton.setEnabled(enable);
        mPayPalButton.setEnabled(enable);
        mVenmoButton.setEnabled(enable);
        mVisaCheckoutButton.setEnabled(enable);
        mLocalPaymentsButton.setEnabled(enable);
        mPayPalTwoFactorAuthButton.setEnabled(enable);
        mPreferredPaymentMethods.setEnabled(enable);
    }

    //jf implementation
    void postNonceToServer(String nonce) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("payment_method_nonce", nonce);
        client.post("https://thecocktailbike.com/checkout", params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        Log.d("postNonceToServer", "onSuccess: " + responseBody);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        Log.d("postNonceToServer", "onFailure: " + responseBody);
                    }
                }
        );
    }
}
