package com.mercadopago;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.mptracker.MPTracker;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;

import static android.text.TextUtils.isEmpty;

public class RejectionActivity extends MercadoPagoActivity {

    //Controls
    protected MPTextView mRejectionTitle;
    protected MPTextView mRejectionSubtitle;
    protected FrameLayout mSelectOtherPaymentMethodByRejection;
    protected MPTextView mExit;

    //Params
    protected Payment mPayment;
    protected PaymentMethod mPaymentMethod;
    protected String mMerchantPublicKey;

    //Local values
    private boolean mBackPressedOnce;

    @Override
    protected void getActivityParameters() {
        mMerchantPublicKey = getIntent().getStringExtra("merchantPublicKey");
        mPayment = JsonUtil.getInstance().fromJson(getIntent().getExtras().getString("payment"), Payment.class);
        mPaymentMethod = JsonUtil.getInstance().fromJson(getIntent().getExtras().getString("paymentMethod"), PaymentMethod.class);
    }

    @Override
    protected void validateActivityParameters() throws IllegalStateException {
        if (mMerchantPublicKey == null) {
            throw new IllegalStateException("merchant public key not set");
        }
        if (mPayment == null) {
            throw new IllegalStateException("payment not set");
        }
        if (mPaymentMethod == null) {
            throw new IllegalStateException("payment method not set");
        }
    }

    @Override
    protected void setContentView() {
        MPTracker.getInstance().trackScreen("REJECTED", 2, mMerchantPublicKey, BuildConfig.VERSION_NAME, getActivity());
        setContentView(R.layout.mpsdk_activity_rejection);
    }

    @Override
    protected void initializeControls() {
        mRejectionTitle = (MPTextView) findViewById(R.id.mpsdkRejectionTitle);
        mRejectionSubtitle = (MPTextView) findViewById(R.id.mpsdkRejectionSubtitle);
        mSelectOtherPaymentMethodByRejection = (FrameLayout) findViewById(R.id.mpsdkSelectOtherPaymentMethodByRejection);

        mSelectOtherPaymentMethodByRejection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("selectOther", true);
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        mExit = (MPTextView) findViewById(R.id.mpsdkExitRejection);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWithOkResult();
            }
        });
    }

    @Override
    protected void onValidStart() {
        if (isStatusDetailValid() && isPaymentMethodValid()) {
            if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_OTHER_REASON)) {
                String titleMessage = String.format(getString(R.string.mpsdk_title_other_reason_rejection), mPaymentMethod.getName());
                mRejectionTitle.setText(titleMessage);
                mRejectionSubtitle.setText(getString(R.string.mpsdk_text_select_other_rejection));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_bad_filled_other_rejection));
                String subtitleMessage = String.format(getString(R.string.mpsdk_text_some_number), mPaymentMethod.getName());
                mRejectionSubtitle.setText(subtitleMessage);
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_bad_filled_other_rejection));
                String subtitleMessage = String.format(getString(R.string.mpsdk_text_some_number), mPaymentMethod.getName());
                mRejectionSubtitle.setText(subtitleMessage);
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT)) {
                String titleMessage = String.format(getString(R.string.mpsdk_text_insufficient_amount), mPaymentMethod.getName());
                mRejectionTitle.setText(titleMessage);

                if (isCardPaymentTypeCreditCard()) {
                    mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_insufficient_amount_credit_card));
                } else {
                    mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_insufficient_amount));
                }
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT)) {
                String titleMessage = String.format(getString(R.string.mpsdk_title_other_reason_rejection), mPaymentMethod.getName());
                mRejectionTitle.setText(titleMessage);
                mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_duplicated_payment));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED)) {
                String titleMessage = String.format(getString(R.string.mpsdk_text_active_card), mPaymentMethod.getName());
                mRejectionTitle.setText(titleMessage);
                mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_card_disabled));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_bad_filled_other_rejection));
                mRejectionSubtitle.setText(getString(R.string.mpsdk_title_bad_filled_security_code_rejection));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_bad_filled_other_rejection));
                mRejectionSubtitle.setText(getString(R.string.mpsdk_title_bad_filled_date_rejection));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_REJECTED_HIGH_RISK)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_rejection_high_risk));
                mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_high_risk));
            } else if (mPayment.getStatusDetail().equals(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS)) {
                mRejectionTitle.setText(getString(R.string.mpsdk_title_rejection_max_attempts));
                mRejectionSubtitle.setText(getString(R.string.mpsdk_subtitle_rejection_max_attempts));
            } else {
                mRejectionTitle.setText(R.string.mpsdk_title_bad_filled_other_rejection);
                mRejectionSubtitle.setVisibility(View.GONE);
            }
        } else {
            ErrorUtil.startErrorActivity(this, getString(R.string.mpsdk_standard_error_message), false);
        }
    }

    @Override
    protected void onInvalidStart(String errorMessage) {
        ErrorUtil.startErrorActivity(this, getString(R.string.mpsdk_standard_error_message), errorMessage, false);
    }

    private Boolean isStatusDetailValid() {
        return !isEmpty(mPayment.getStatusDetail());
    }

    private Boolean isPaymentMethodValid() {
        return isPaymentMethodIdValid() && !isEmpty(mPaymentMethod.getName()) && !isEmpty(mPaymentMethod.getPaymentTypeId());
    }

    private Boolean isPaymentMethodIdValid() {
        return !isEmpty(mPaymentMethod.getId()) && mPayment.getPaymentMethodId().equals(mPaymentMethod.getId());
    }

    private void finishWithOkResult() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private boolean isCardPaymentTypeCreditCard() {
        return !mPaymentMethod.getPaymentTypeId().isEmpty() && mPaymentMethod.getPaymentTypeId().equals("credit_card");
    }

    @Override
    public void onBackPressed() {
        MPTracker.getInstance().trackEvent("REJECTION", "BACK_PRESSED", 2, mMerchantPublicKey, BuildConfig.VERSION_NAME, this);

        if (mBackPressedOnce) {
            finishWithOkResult();
        } else {
            Snackbar.make(mExit, getString(R.string.mpsdk_press_again_to_leave), Snackbar.LENGTH_LONG).show();
            mBackPressedOnce = true;
            resetBackPressedOnceIn(4000);
        }
    }

    private void resetBackPressedOnceIn(final int mills) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mills);
                    mBackPressedOnce = false;
                } catch (InterruptedException e) {
                    //Do nothing
                }
            }
        }).start();
    }
}
