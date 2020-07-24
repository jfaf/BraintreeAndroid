package com.braintreepayments.api.exceptions;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static com.braintreepayments.testutils.FixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class BraintreeErrorUnitTest {

    @Test
    public void parcelsCorrectly() throws JSONException {
        JSONObject errorResponse = new JSONObject(stringFromFixture("errors/credit_card_error_response.json"));
        List<BraintreeError> errors = BraintreeError.fromJsonArray(errorResponse.getJSONArray("fieldErrors"));
        assertEquals(1, errors.size());
        BraintreeError error = errors.get(0);

        Parcel parcel = Parcel.obtain();
        error.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BraintreeError parceled = BraintreeError.CREATOR.createFromParcel(parcel);

        assertEquals(error.getField(), parceled.getField());
        assertEquals(error.getMessage(), parceled.getMessage());
        assertEquals(error.getFieldErrors().size(), parceled.getFieldErrors().size());
    }

    @Test
    public void graphQLErrors_parcelCorrectly() throws Exception {
        JSONObject errorResponse = new JSONObject(stringFromFixture("errors/graphql/credit_card_error.json"));
        List<BraintreeError> errors = BraintreeError.fromGraphQLJsonArray(errorResponse.getJSONArray("errors"));
        assertEquals(1, errors.size());
        BraintreeError error = errors.get(0);

        Parcel parcel = Parcel.obtain();
        error.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BraintreeError parceled = BraintreeError.CREATOR.createFromParcel(parcel);

        assertEquals(error.getField(), parceled.getField());
        assertEquals(error.getMessage(), parceled.getMessage());
        assertEquals(error.getFieldErrors().size(), parceled.getFieldErrors().size());
    }
}
