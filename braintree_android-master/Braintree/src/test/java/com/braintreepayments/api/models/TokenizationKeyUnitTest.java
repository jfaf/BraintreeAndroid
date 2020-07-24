package com.braintreepayments.api.models;

import com.braintreepayments.api.BuildConfig;
import com.braintreepayments.api.exceptions.InvalidArgumentException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TokenizationKeyUnitTest {

    private static final String TOKENIZATION_KEY = "development_testing_integration_merchant_id";

    @Test
    public void fromString_acceptsATokenizationKey() throws InvalidArgumentException {
        Authorization tokenizationKey = Authorization.fromString(TOKENIZATION_KEY);

        assertEquals(TOKENIZATION_KEY, tokenizationKey.getBearer());
    }

    @Test(expected = InvalidArgumentException.class)
    public void fromString_throwsInvalidArgumentExceptionForNonTokenizationKeys() throws InvalidArgumentException {
        Authorization.fromString("{}");
    }

    @Test
    public void fromString_parsesEnvironment() throws InvalidArgumentException {
        TokenizationKey tokenizationKey = (TokenizationKey) Authorization.fromString(TOKENIZATION_KEY);

        assertEquals("development", tokenizationKey.getEnvironment());
    }

    @Test
    public void fromString_parsesMerchantId() throws InvalidArgumentException {
        TokenizationKey tokenizationKey = (TokenizationKey) Authorization.fromString(TOKENIZATION_KEY);

        assertEquals("integration_merchant_id", tokenizationKey.getMerchantId());
    }

    @Test
    public void fromString_setsUrlForDevelopment() throws InvalidArgumentException {
        TokenizationKey tokenizationKey = (TokenizationKey) Authorization.fromString(TOKENIZATION_KEY);

        assertEquals(BuildConfig.DEVELOPMENT_URL + "merchants/integration_merchant_id/client_api/",
                tokenizationKey.getUrl());
        assertEquals(BuildConfig.DEVELOPMENT_URL + "merchants/integration_merchant_id/client_api/v1/configuration",
                tokenizationKey.getConfigUrl());
    }

    @Test
    public void fromString_setsUrlForSandbox() throws InvalidArgumentException {
        TokenizationKey tokenizationKey = (TokenizationKey) Authorization.fromString(
                "sandbox_fjajdkd_integration_merchant_id");

        assertEquals("https://api.sandbox.braintreegateway.com/merchants/integration_merchant_id/client_api/",
                tokenizationKey.getUrl());
        assertEquals("https://api.sandbox.braintreegateway.com/merchants/integration_merchant_id/client_api/v1/configuration",
                tokenizationKey.getConfigUrl());
    }

    @Test
    public void fromString_setsUrlForProduction() throws InvalidArgumentException {
        TokenizationKey tokenizationKey = (TokenizationKey) Authorization.fromString(
                "production_fjajdkd_integration_merchant_id");

        assertEquals("https://api.braintreegateway.com/merchants/integration_merchant_id/client_api/",
                tokenizationKey.getUrl());
        assertEquals("https://api.braintreegateway.com/merchants/integration_merchant_id/client_api/v1/configuration",
                tokenizationKey.getConfigUrl());
    }

    @Test(expected = InvalidArgumentException.class)
    public void fromString_throwsInvalidArgumentExceptionForInvalidEnvironments() throws InvalidArgumentException {
        Authorization.fromString("test_fjajdkd_integration_merchant_id");
    }

    @Test
    public void getBearer_returnsTokenizationKey() throws InvalidArgumentException {
        assertEquals(TOKENIZATION_KEY, Authorization.fromString(TOKENIZATION_KEY).getBearer());
    }
}
