package com.mercadopago.uicontrollers.savedcards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.R;
import com.mercadopago.model.Card;
import com.mercadopago.uicontrollers.paymentmethods.card.PaymentMethodOnView;

/**
 * Created by mreverter on 5/10/16.
 */
public class SavedCardRowView extends PaymentMethodOnView implements SavedCardView {
    protected Card mCard;

    public SavedCardRowView(Context context, Card card) {
        mContext = context;
        mPaymentMethod = card.getPaymentMethod();
        mCard = card;
    }

    @Override
    protected String getLastFourDigits() {
        String lastFourDigits = "";
        if (mCard != null) {
            lastFourDigits = mCard.getLastFourDigits();
        }
        return lastFourDigits;
    }

    @Override
    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        mView = LayoutInflater.from(mContext)
                .inflate(R.layout.mpsdk_row_payment_method_card, parent, attachToRoot);
        return mView;
    }
}
