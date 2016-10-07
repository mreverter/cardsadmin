package com.mercadopago;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.model.Card;
import com.mercadopago.uicontrollers.savedcards.SavedCardsView;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.List;

public class CustomerCardsActivity extends MercadoPagoActivity {


    protected Toolbar mToolbar;
    protected TextView mTitle;
    protected ViewGroup mSavedCardsContainer;

    private List<Card> mCards;
    private String mCustomTitle;
    private String mCustomFooterMessage;

    @Override
    protected void getActivityParameters() {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Card>>() {
            }.getType();
            mCards = gson.fromJson(this.getIntent().getStringExtra("cards"), listType);
        } catch (Exception ex) {
            mCards = null;
        }
        mCustomTitle = this.getIntent().getStringExtra("title");
        mCustomFooterMessage = this.getIntent().getStringExtra("footerText");

        if(mDecorationPreference != null) {
            super.decorate(mToolbar);
            super.decorateFont(mTitle);
        }
    }

    @Override
    protected void validateActivityParameters() throws IllegalStateException {
        if (mCards == null) {
            throw new IllegalStateException("cards not set");
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.mpsdk_activity_customer_cards);
    }

    @Override
    protected void initializeControls() {
        initializeToolbar();
        mSavedCardsContainer = (ViewGroup) findViewById(R.id.mpsdkRegularLayout);
    }

    @Override
    protected void onValidStart() {
        fillData();
    }

    @Override
    protected void onInvalidStart(String message) {
        ErrorUtil.startErrorActivity(this, message, false);
    }

    private void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.mpsdkToolbar);
        mTitle = (TextView) findViewById(R.id.mpsdkToolbarTitle);
        if(!TextUtils.isEmpty(mCustomTitle)) {
            mTitle.setText(mCustomTitle);
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (isCustomColorSet()) {
            mToolbar.setBackgroundColor(getCustomBaseColor());
        }
        if (isDarkFontEnabled()) {
            mTitle.setTextColor(getDarkFontColor());
            Drawable upArrow = mToolbar.getNavigationIcon();
            upArrow.setColorFilter(getDarkFontColor(), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    private void fillData() {

        SavedCardsView savedCardsView = new MercadoPagoUI.Views.SavedCardsListViewBuilder()
                .setContext(this)
                .setCards(mCards)
                .setFooter(mCustomFooterMessage)
                .setOnSelectedCallback(getOnSelectedCallback())
                .build();

        savedCardsView.drawInParent(mSavedCardsContainer);

    }

    private OnSelectedCallback<Card> getOnSelectedCallback() {
        return new OnSelectedCallback<Card>() {
            @Override
            public void onSelected(Card card) {
                // Return to parent
                Intent returnIntent = new Intent();
                if (card != null) {
                    returnIntent.putExtra("card", JsonUtil.getInstance().toJson(card));
                }
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        };
    }

    public void onOtherPaymentMethodClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

}
