package com.mercadopago;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Spanned;
import android.widget.ImageView;

import com.mercadopago.model.Card;
import com.mercadopago.model.FeeDetail;
import com.mercadopago.model.Payer;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.TransactionDetails;
import com.mercadopago.util.CurrenciesUtil;
import com.mercadopago.util.JsonUtil;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

/**
 * Created by mromar on 6/21/16.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest

public class CongratsActivityTest {

    @Rule
    public ActivityTestRule<CongratsActivity> mTestRule = new ActivityTestRule<>(CongratsActivity.class, true, false);
    public Intent validStartIntent, nullPaymentIntent, nullPublicKeyIntent, nullPaymentMethodIntent;

    private Payment mPayment;
    private String mMerchantPublicKey;
    private PaymentMethod mPaymentMethod;

    @Before
    public void validStartParameters() {
        mPayment = getPayment();
        mMerchantPublicKey = "1234";
        mPaymentMethod = getPaymentMethodCard();
    }

    private PaymentMethod getPaymentMethodCard() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("master");
        paymentMethod.setName("Master");
        paymentMethod.setPaymentTypeId("credit_card");
        return paymentMethod;
    }

    private Payment getPayment(){
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(300));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        Card card = new Card();
        card.setLastFourDigits("1234");

        Payer payer = new Payer();
        payer.setId("178101336");
        payer.setEmail("juan.perez@email.com");

        Payment payment = new Payment();
        payment.setPayer(payer);
        payment.setStatus("approved");
        payment.setStatusDetail("accredited");
        payment.setCard(card);
        payment.setId(123456789L);
        payment.setPaymentMethodId("master");
        payment.setInstallments(6);
        payment.setTransactionDetails(transactionDetails);
        payment.setCurrencyId("ARS");

        return payment;
    }

    private void createIntent(){
        validStartIntent = new Intent();
        validStartIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        validStartIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
        validStartIntent.putExtra("payment", JsonUtil.getInstance().toJson(mPayment));
    }

    private void createIntentWithNullPayment(){
        nullPaymentIntent = new Intent();
        nullPaymentIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        nullPaymentIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
    }

    private void createIntentWithNullPublicKey(){
        nullPublicKeyIntent = new Intent();
        nullPublicKeyIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
        nullPublicKeyIntent.putExtra("payment", JsonUtil.getInstance().toJson(mPayment));
    }

    private void createIntentWithNullPaymentMethod(){
        nullPaymentMethodIntent = new Intent();
        nullPaymentMethodIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        nullPaymentMethodIntent.putExtra("payment", JsonUtil.getInstance().toJson(mPayment));
    }

    @Test
    public void displayWithoutInterestWhenApprovedPaymentHasZeroRate(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    private Spanned setTotalAmountDescription() {
        StringBuilder totalAmountDescription = new StringBuilder();

        totalAmountDescription.append("( ");
        totalAmountDescription.append(CurrenciesUtil.formatNumber(mPayment.getTransactionDetails().getTotalPaidAmount(), mPayment.getCurrencyId()));
        totalAmountDescription.append(" )");

        return CurrenciesUtil.formatCurrencyInText(mPayment.getTransactionDetails().getTotalPaidAmount(),
                mPayment.getCurrencyId(), totalAmountDescription.toString(), true, true);
    }

    private Spanned setInstallmentsDescription(){
        StringBuilder installmentsDescription = new StringBuilder();

        installmentsDescription.append(mPayment.getInstallments());
        installmentsDescription.append(" ");
        installmentsDescription.append(mTestRule.getActivity().getString(R.string.mpsdk_installments_of));
        installmentsDescription.append(" ");
        installmentsDescription.append(CurrenciesUtil.formatNumber(mPayment.getTransactionDetails().getInstallmentAmount(), mPayment.getCurrencyId()));

        return CurrenciesUtil.formatCurrencyInText(mPayment.getTransactionDetails().getInstallmentAmount(),
                mPayment.getCurrencyId(), installmentsDescription.toString(), true, true);
    }

    @Test
    public void displayTotalAmountWithRateWhenApprovedPaymentHasInterest(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription, totalAmountDescription;
        Bitmap bitmap, paymentBitmap;

        FeeDetail feeDetail = new FeeDetail();
        feeDetail.setType("financing_fee");
        feeDetail.setAmount(new BigDecimal(10));
        List<FeeDetail> feeDetails = new ArrayList<>();
        feeDetails.add(feeDetail);

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);
        mPayment.setFeeDetails(feeDetails);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with rate
        totalAmountDescription = setTotalAmountDescription();
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(totalAmountDescription.toString())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void displayWithoutInterestWhenApprovedPaymentHasZeroRateButFeeDetailsSizeIsOne(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        FeeDetail feeDetail = new FeeDetail();
        feeDetail.setType("test");
        feeDetail.setAmount(new BigDecimal(10));
        List<FeeDetail> feeDetails = new ArrayList<>();
        feeDetails.add(feeDetail);

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);
        mPayment.setFeeDetails(feeDetails);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void emailIsNotDisplayedWhenPaymentHasNullEmail(){
        String paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        Payer payer = new Payer();
        payer.setEmail(null);

        mPayment.setPayer(payer);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(not(isDisplayed())));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void emailIsNotDisplayedWhenPaymentHasEmptyEmail(){
        String paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        Payer payer = new Payer();
        payer.setEmail("");

        mPayment.setPayer(payer);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(not(isDisplayed())));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void emailIsNotDisplayedWhenPaymentHasNullPayer(){
        String paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setPayer(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(not(isDisplayed())));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsCardAreNotDisplayedWhenPaymentHasNullCard(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        mPayment.setCard(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsCardAreNotDisplayedWhenPaymentHasNullLastFourDigitsCard(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        Card card = new Card();
        card.setLastFourDigits(null);

        mPayment.setCard(card);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsCardAreNotDisplayedWhenPaymentHasEmptyLastFourDigitsCard(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        Card card = new Card();
        card.setLastFourDigits("");

        mPayment.setCard(card);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsCardAndPaymentMethodImageNotDisplayedWhenPaymentHasNullPaymentMethodId(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        mPaymentMethod.setId(null);

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void paymentMethodImageIsNotDisplayedWhenPaymentMethodIdHasNotImage(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Spanned installmentsDescription;

        mPaymentMethod.setId("test");
        mPayment.setPaymentMethodId("test");

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsAndPaymentMethodImageNotDisplayedWhenPaymentMethodIdIsEmpty(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        mPaymentMethod.setId("");

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void lastFourDigitsCardAreNotDisplayedWhenPaymentPaymentMethodIdIsDifferentToPaymentMethodId(){
        String subtitle, paymentIdDescription;
        Spanned installmentsDescription;

        mPaymentMethod.setId("visa");

        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(not(isDisplayed())));

        //PaymentMethod image
        onView(withId(R.id.mpsdkPaymentMethodImage)).check(matches(not(isDisplayed())));

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentInstallmentsNumberIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setInstallments(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //Total amount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentInstallmentsNumberIsNegative(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setInstallments(-1);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //Total amount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedAndTotalAmountIsDisplayedWhenPaymentInstallmentsNumberIsZero(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        StringBuilder installments = new StringBuilder();
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setInstallments(0);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description with total amount
        installments.append(CurrenciesUtil.formatNumber(mPayment.getTransactionDetails().getTotalPaidAmount(), mPayment.getCurrencyId()));
        installmentsDescription = CurrenciesUtil.formatCurrencyInText(mPayment.getTransactionDetails().getTotalPaidAmount(),
                mPayment.getCurrencyId(), installments.toString(), true, true);
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedAndTotalAmountIsDisplayedWhenPaymentInstallmentsNumberIsOne(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        StringBuilder installments = new StringBuilder();
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setInstallments(1);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installments.append(CurrenciesUtil.formatNumber(mPayment.getTransactionDetails().getTotalPaidAmount(), mPayment.getCurrencyId()));
        installmentsDescription = CurrenciesUtil.formatCurrencyInText(mPayment.getTransactionDetails().getTotalPaidAmount(),
                mPayment.getCurrencyId(), installments.toString(), true, true);
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentTransactionDetailIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setTransactionDetails(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentInstallmentAmountIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(null);
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentInstallmentAmountIsNegative(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(-10));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentInstallmentAmountIsZero(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(0));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentCurrencyIdIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(0));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setCurrencyId(null);
        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentCurrencyIdIsEmpty(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(0));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setCurrencyId("");
        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void installmentsDescriptionIsNotDisplayedWhenPaymentCurrencyIdNotExist(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(0));
        transactionDetails.setTotalPaidAmount(new BigDecimal(1800));

        mPayment.setCurrencyId("MLA");
        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void totalAmountDescriptionIsNotDisplayedWhenPaymentTransactionDetailIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setTransactionDetails(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void totalAmountDescriptionIsNotDisplayedWhenPaymentTotalPaidAmountIsNull(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(250));
        transactionDetails.setTotalPaidAmount(null);

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void totalAmountDescriptionIsNotDisplayedWhenPaymentTotalPaidAmountIsNegative(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(250));
        transactionDetails.setTotalPaidAmount(new BigDecimal(-1800));

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void totalAmountDescriptionIsNotDisplayedWhenPaymentTotalPaidAmountIsZero(){
        String subtitle, paymentIdDescription, lastFourDigitsDescription;
        Bitmap bitmap, paymentBitmap;

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setInstallmentAmount(new BigDecimal(250));
        transactionDetails.setTotalPaidAmount(new BigDecimal(0));

        mPayment.setTransactionDetails(transactionDetails);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(not(isDisplayed())));

        //TotalAmount description
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(not(isDisplayed())));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        paymentIdDescription = mTestRule.getActivity().getString(R.string.mpsdk_payment_id_description) + " " + mPayment.getId();
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(withText(paymentIdDescription)));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void paymentIdIsNotDisplayedWhenPaymentIdIsNull(){
        String subtitle, lastFourDigitsDescription;
        Spanned installmentsDescription;
        Bitmap bitmap, paymentBitmap;

        mPayment.setId(null);
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Title and subtitle
        onView(withId(R.id.mpsdkCongratulationsTitle)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_title_activity_congrat))));
        subtitle = String.format(mTestRule.getActivity().getString(R.string.mpsdk_subtitle_action_activity_congrat), mPayment.getPayer().getEmail());
        onView(withId(R.id.mpsdkCongratulationSubtitle)).check(matches(withText(subtitle)));

        //LastFourDigits Card
        lastFourDigitsDescription = mTestRule.getActivity().getString(R.string.mpsdk_last_digits_label) + " " + mPayment.getCard().getLastFourDigits();
        onView(withId(R.id.mpsdkLastFourDigitsDescription)).check(matches(withText(lastFourDigitsDescription)));

        //PaymentMethod image
        ImageView paymentMethodImage = (ImageView) mTestRule.getActivity().findViewById(R.id.mpsdkPaymentMethodImage);
        if (paymentMethodImage != null && paymentMethodImage.getDrawable() != null){
            bitmap = ((BitmapDrawable) paymentMethodImage.getDrawable()).getBitmap();
            paymentBitmap = ((BitmapDrawable) ContextCompat.getDrawable(mTestRule.getActivity(), R.drawable.master)).getBitmap();
            assertTrue(bitmap == paymentBitmap);
        }

        //Installments description
        installmentsDescription = setInstallmentsDescription();
        onView(withId(R.id.mpsdkInstallmentsDescription)).check(matches(withText(installmentsDescription.toString())));

        //Total amount description with zero rate
        onView(withId(R.id.mpsdkInterestAmountDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_zero_rate))));

        //State description
        onView(withId(R.id.mpsdkStateDescription)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_text_state_acount_activity_congrat))));

        //Correct paymentId
        onView(withId(R.id.mpsdkPaymentIdDescription)).check(matches(not(isDisplayed())));
        onView(withId(R.id.mpsdkPaymentIdSeparator)).check(matches(not(isDisplayed())));

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));
    }

    @Test
    public void finishCongratsActivityWhenClickOnExitCongrats(){
        mPayment.setStatus(Payment.StatusCodes.STATUS_APPROVED);
        mPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_ACCREDITED);

        createIntent();
        mTestRule.launchActivity(validStartIntent);

        //Exit button isDisplayed
        onView(withId(R.id.mpsdkExitCongrats)).check(matches(isDisplayed()));

        //Click on exit button
        onView(withId(R.id.mpsdkExitCongrats)).perform(click());

        //Congrats finish
        assertTrue(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void showErrorWhenStartCongratsActivityWithNullPayment() {
        createIntentWithNullPayment();
        mTestRule.launchActivity(nullPaymentIntent);

        //Error message
        onView(withId(R.id.mpsdkErrorMessage)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_standard_error_message))));

        //Retry button is displayed
        onView(withId(R.id.mpsdkExit)).check(matches(isDisplayed()));
    }

    @Test
    public void showErrorWhenStartCongratsActivityWithNullPublicKey() {
        createIntentWithNullPublicKey();
        mTestRule.launchActivity(nullPublicKeyIntent);

        //Error message
        onView(withId(R.id.mpsdkErrorMessage)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_standard_error_message))));

        //Retry button is displayed
        onView(withId(R.id.mpsdkExit)).check(matches(isDisplayed()));
    }

    @Test
    public void showErrorWhenStartCongratsActivityWithNullPaymentMethod() {
        createIntentWithNullPaymentMethod();
        mTestRule.launchActivity(nullPaymentMethodIntent);

        //Error message
        onView(withId(R.id.mpsdkErrorMessage)).check(matches(withText(mTestRule.getActivity().getString(R.string.mpsdk_standard_error_message))));

        //Retry button is displayed
        onView(withId(R.id.mpsdkExit)).check(matches(isDisplayed()));
    }

    @Test
    public void noFinishCongratsActivityWhenClickOnBackButton(){
        createIntent();
        mTestRule.launchActivity(validStartIntent);

        pressBack();

        //Congrats finish
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test (expected = NoActivityResumedException.class)
    public void finishCongratsActivityWhenClickOnBackButtonTwoTimes(){
        createIntent();
        mTestRule.launchActivity(validStartIntent);

        pressBack();
        pressBack();

        Assert.assertTrue(mTestRule.getActivity().isFinishing());
    }
}
