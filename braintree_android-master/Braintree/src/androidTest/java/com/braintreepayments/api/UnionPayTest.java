package com.braintreepayments.api;

import android.text.TextUtils;

import androidx.test.runner.AndroidJUnit4;

import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.UnionPayListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.UnionPayCapabilities;
import com.braintreepayments.api.models.UnionPayCardBuilder;
import com.braintreepayments.api.test.BraintreeActivityTestRule;
import com.braintreepayments.api.test.TestActivity;
import com.braintreepayments.api.test.TestClientTokenBuilder;
import com.braintreepayments.testutils.CardNumber;
import com.braintreepayments.testutils.ExpirationDateHelper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.getFragmentWithAuthorization;
import static com.braintreepayments.api.test.Assertions.assertIsANonce;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_DEBIT;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_INTEGRATION_CREDIT;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_INTEGRATION_DEBIT;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_NOT_ACCEPTED;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_SINGLE_STEP_SALE;
import static com.braintreepayments.testutils.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class UnionPayTest {

    @Rule
    public final BraintreeActivityTestRule<TestActivity> mActivityTestRule = new BraintreeActivityTestRule<>(TestActivity.class);

    private BraintreeFragment mBraintreeFragment;
    private CountDownLatch mCountDownLatch;

    @Before
    public void setUp() {
        mCountDownLatch = new CountDownLatch(1);
        mBraintreeFragment = getFragmentWithAuthorization(mActivityTestRule.getActivity(),
                new TestClientTokenBuilder().build());
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_whenDebit_isDebitIsTrue() throws InterruptedException {
        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities unionPayCapabilities) {
                assertTrue(unionPayCapabilities.supportsTwoStepAuthAndCapture());
                assertTrue(unionPayCapabilities.isDebit());
                assertTrue(unionPayCapabilities.isUnionPay());
                mCountDownLatch.countDown();
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                fail("Not expecting onSmsCodeSent");
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, UNIONPAY_DEBIT);

        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_whenCredit_isDebitIsFalse() throws InterruptedException {
        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities unionPayCapabilities) {
                assertTrue(unionPayCapabilities.supportsTwoStepAuthAndCapture());
                assertFalse(unionPayCapabilities.isDebit());
                assertTrue(unionPayCapabilities.isUnionPay());
                mCountDownLatch.countDown();
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                fail("Not expecting onSmsCodeSent");
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, UNIONPAY_CREDIT);

        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPayCredit_isSupported() throws InterruptedException {
        assertSupported(UNIONPAY_CREDIT, true);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPayDebit_isSupported() throws InterruptedException {
        assertSupported(UNIONPAY_DEBIT, true);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPaySingleStepSale_isSupported() throws InterruptedException {
        assertSupported(UNIONPAY_SINGLE_STEP_SALE, true);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPayIntegrationCredit_isSupported() throws InterruptedException {
        assertSupported(UNIONPAY_INTEGRATION_CREDIT, true);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPayIntegrationDebit_isSupported() throws InterruptedException {
        assertSupported(UNIONPAY_INTEGRATION_DEBIT, true);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_unionPaySmsNotRequired_isNotSupported() throws InterruptedException {
        assertSupported(UNIONPAY_NOT_ACCEPTED, false);
    }

    @Test(timeout = 10000)
    public void fetchCapabilities_whenSingleStepSale_twoStepAuthAndCaptureIsFalse() throws InterruptedException {
        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities unionPayCapabilities) {
                assertTrue(unionPayCapabilities.isSupported());
                assertFalse(unionPayCapabilities.supportsTwoStepAuthAndCapture());
                assertTrue(unionPayCapabilities.isDebit());
                assertTrue(unionPayCapabilities.isUnionPay());
                mCountDownLatch.countDown();
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                fail("Not expecting onSmsCodeSent");
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, UNIONPAY_SINGLE_STEP_SALE);

        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void enroll_whenIsUnionPay_returnsEnrollmentId() throws InterruptedException {
        String cardNumber = UNIONPAY_CREDIT;
        final UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                .cardNumber(cardNumber)
                .expirationMonth("12")
                .expirationYear(ExpirationDateHelper.validExpirationYear())
                .mobileCountryCode("62")
                .mobilePhoneNumber("11111111111");

        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities unionPayCapabilities) {
                assertTrue(unionPayCapabilities.isUnionPay());
                UnionPay.enroll(mBraintreeFragment, unionPayCardBuilder);
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                assertFalse(TextUtils.isEmpty(enrollmentId));
                assertTrue(smsCodeRequired);
                mCountDownLatch.countDown();
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, cardNumber);

        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void enroll_whenIsUnionPayFalse_willError() throws InterruptedException {
        String cardNumber = CardNumber.VISA;
        final UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                .cardNumber(cardNumber)
                .expirationMonth("12")
                .expirationYear(ExpirationDateHelper.validExpirationYear())
                .mobileCountryCode("62")
                .mobilePhoneNumber("11111111111");

        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
                assertFalse(capabilities.isUnionPay());
                UnionPay.enroll(mBraintreeFragment, unionPayCardBuilder);
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                fail("Not expecting onSmsCodeSent");
            }
        });

        mBraintreeFragment.addListener(new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {
                assertTrue(error instanceof ErrorWithResponse);
                assertEquals("UnionPay Enrollment is invalid", error.getMessage());
                mCountDownLatch.countDown();
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, cardNumber);
        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void enroll_whenSmsCodeRequiredFalse_onSmsCodeSentReturnsFalse() throws InterruptedException {
        mCountDownLatch = new CountDownLatch(2);
        String cardNumber = UNIONPAY_SMS_NOT_REQUIRED;
        final UnionPayCardBuilder unionPayCardBuilder = new UnionPayCardBuilder()
                .cardNumber(cardNumber)
                .expirationMonth("12")
                .expirationYear(ExpirationDateHelper.validExpirationYear())
                .mobileCountryCode("62")
                .mobilePhoneNumber("11111111111");

        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
                assertTrue(capabilities.isUnionPay());
                assertTrue(capabilities.isSupported());
                UnionPay.enroll(mBraintreeFragment, unionPayCardBuilder);
                mCountDownLatch.countDown();
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                assertNotNull(enrollmentId);
                assertFalse(smsCodeRequired);
                mCountDownLatch.countDown();
            }
        });

        mBraintreeFragment.addListener(new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {
                fail("Not expecting error");
            }
        });

        UnionPay.fetchCapabilities(mBraintreeFragment, cardNumber);
        mCountDownLatch.await();
    }

    @Ignore("Sample merchant account is not set up for Union Pay")
    @Test(timeout = 10000)
    public void tokenize_unionPayCredit_withExpirationDate() throws InterruptedException {
        final UnionPayCardBuilder cardBuilder = new UnionPayCardBuilder()
                .cardNumber(CardNumber.UNIONPAY_CREDIT)
                .expirationDate("08/20")
                .cvv("123")
                .mobileCountryCode("62")
                .mobilePhoneNumber("1111111111");

        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {}

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                assertTrue(smsCodeRequired);
                cardBuilder.enrollmentId(enrollmentId);
                cardBuilder.smsCode("12345");
                UnionPay.tokenize(mBraintreeFragment, cardBuilder);

            }
        });

        mBraintreeFragment.addListener(new PaymentMethodNonceCreatedListener() {
            @Override
            public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                assertIsANonce(paymentMethodNonce.getNonce());
                assertEquals("32", ((CardNonce) paymentMethodNonce).getLastTwo());
                mCountDownLatch.countDown();
            }
        });

        UnionPay.enroll(mBraintreeFragment, cardBuilder);

        mCountDownLatch.await();
    }

    @Ignore("Sample merchant account is not set up for Union Pay")
    @Test(timeout = 30000)
    public void tokenize_unionPayCredit_withExpirationMonthAndYear() throws InterruptedException {
        final UnionPayCardBuilder cardBuilder = new UnionPayCardBuilder()
                .cardNumber(CardNumber.UNIONPAY_CREDIT)
                .expirationMonth("08")
                .expirationYear("20")
                .cvv("123")
                .mobileCountryCode("62")
                .mobilePhoneNumber("1111111111");

        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {}

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                assertTrue(smsCodeRequired);
                cardBuilder.enrollmentId(enrollmentId);
                cardBuilder.smsCode("12345");
                UnionPay.tokenize(mBraintreeFragment, cardBuilder);
            }
        });

        mBraintreeFragment.addListener(new PaymentMethodNonceCreatedListener() {
            @Override
            public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                assertIsANonce(paymentMethodNonce.getNonce());
                assertEquals("32", ((CardNonce) paymentMethodNonce).getLastTwo());
                mCountDownLatch.countDown();
            }
        });

        UnionPay.enroll(mBraintreeFragment, cardBuilder);

        mCountDownLatch.await();
    }

    private void assertSupported(final String cardNumber, final boolean expected) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        mBraintreeFragment.addListener(new UnionPayListener() {
            @Override
            public void onCapabilitiesFetched(UnionPayCapabilities capabilities) {
                boolean actual = capabilities.isSupported();
                String message = String.format("Expected %s to be supported? %b, but was %b", cardNumber, expected, actual);
                assertEquals(message, expected, actual);
                countDownLatch.countDown();
            }

            @Override
            public void onSmsCodeSent(String enrollmentId, boolean smsCodeRequired) {
                fail("Not expecting onSmsCodeSent");
            }
        });
        UnionPay.fetchCapabilities(mBraintreeFragment, cardNumber);
        countDownLatch.await();
    }
}
