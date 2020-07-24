package com.braintreepayments.api;

import com.braintreepayments.api.interfaces.PaymentMethodNonceCallback;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;

/**
 * Used to tokenize credit or debit cards using a {@link CardBuilder}. For more information see the
 * <a href="https://developers.braintreepayments.com/guides/credit-cards/overview">documentation</a>
 */
public class Card {

    /**
     * Create a {@link com.braintreepayments.api.models.CardNonce}.
     * <p>
     * On completion, returns the {@link com.braintreepayments.api.models.PaymentMethodNonce} to
     * {@link com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener}.
     * <p>
     * If creation fails validation, {@link com.braintreepayments.api.interfaces.BraintreeErrorListener#onError(Exception)}
     * will be called with the resulting {@link com.braintreepayments.api.exceptions.ErrorWithResponse}.
     * <p>
     * If an error not due to validation (server error, network issue, etc.) occurs, {@link
     * com.braintreepayments.api.interfaces.BraintreeErrorListener#onError(Exception)}
     * will be called with the {@link Exception} that occurred.
     *
     * @param fragment {@link BraintreeFragment}
     * @param cardBuilder {@link CardBuilder}
     */
    public static void tokenize(final BraintreeFragment fragment, final CardBuilder cardBuilder) {
        TokenizationClient.tokenize(fragment, cardBuilder, new PaymentMethodNonceCallback() {
            @Override
            public void success(PaymentMethodNonce paymentMethodNonce) {
                DataCollector.collectRiskData(fragment, paymentMethodNonce);

                fragment.postCallback(paymentMethodNonce);
                fragment.sendAnalyticsEvent("card.nonce-received");
            }

            @Override
            public void failure(Exception exception) {
                fragment.postCallback(exception);
                fragment.sendAnalyticsEvent("card.nonce-failed");
            }
        });
    }

    /**
     * Create a {@link com.braintreepayments.api.models.CardNonce}.
     * <p>
     * The tokenization result is returned via a {@link PaymentMethodNonceCallback} listener.
     *
     * <p>
     * On success, the callback listener's {@link PaymentMethodNonceCallback#success} method will
     * be invoked with a nonce.
     *
     * <p>
     * If creation fails validation, the callback listener's {@link PaymentMethodNonceCallback#failure}
     * method will be invoked with an {@link com.braintreepayments.api.exceptions.ErrorWithResponse} exception.
     *
     * <p>
     * If an error not due to validation (server error, network issue, etc.) occurs, the callback
     * listener's {@link PaymentMethodNonceCallback#failure} method will be invoked with
     * an {@link Exception} describing the error.
     *
     * @param fragment {@link BraintreeFragment}
     * @param cardBuilder {@link CardBuilder}
     * @param callback {@link PaymentMethodNonceCallback}
     */
    public static void tokenize(final BraintreeFragment fragment, final CardBuilder cardBuilder, final PaymentMethodNonceCallback callback) {
        TokenizationClient.tokenize(fragment, cardBuilder, callback);
    }
}
