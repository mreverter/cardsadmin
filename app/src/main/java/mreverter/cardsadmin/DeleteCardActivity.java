package mreverter.cardsadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.mercadopago.uicontrollers.savedcards.SavedCardsListView;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.List;

public class DeleteCardActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected TextView mTitle;
    protected ViewGroup mSavedCardsContainer;

    private List<Card> mCards;
    private String mCustomTitle;
    private String mCustomFooterMessage;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        getActivityParameters();

        setContentView();
        try {
            validateActivityParameters();
            initializeControls();
            onValidStart();
        } catch (IllegalStateException exception) {
            onInvalidStart(exception.getMessage());
        }
    }

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
    }

    protected void validateActivityParameters() throws IllegalStateException {
        if (mCards == null) {
            throw new IllegalStateException("cards not set");
        }
    }

    protected void setContentView() {
        setContentView(R.layout.activity_delete_card);
    }

    protected void initializeControls() {
        initializeToolbar();
        mSavedCardsContainer = (ViewGroup) findViewById(R.id.cardsList);
    }

    protected void onValidStart() {
        fillData();
    }

    protected void onInvalidStart(String message) {
        ErrorUtil.startErrorActivity(this, message, false);
    }

    private void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void fillData() {

        Drawable selection = ContextCompat.getDrawable(this, android.R.drawable.ic_input_delete);
        selection.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light) ,android.graphics.PorterDuff.Mode.MULTIPLY);

        SavedCardsListView savedCardsView = new MercadoPagoUI.Views.SavedCardsListViewBuilder()
                .setContext(this)
                .setCards(mCards)
                .setFooter(mCustomFooterMessage)
                .setOnSelectedCallback(getOnSelectedCallback())
                .setSelectionImage(selection)
                .build();

        savedCardsView.drawInParent(mSavedCardsContainer);

    }

    private OnSelectedCallback<Card> getOnSelectedCallback() {
        return new OnSelectedCallback<Card>() {
            @Override
            public void onSelected(Card card) {
                // Return to parent
                if (card != null) {
                    resolveCardResponse(card);
                }
            }
        };
    }
    private void resolveCardResponse(final Card card) {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Borrar tarjeta")
                .setMessage("¿Seguro que desea borrar " + card.getPaymentMethod().getName()  + " terminada en " + card.getLastFourDigits() + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DummyCardsCollection.removeCard(card);
                        mCards = DummyCardsCollection.getCards();
                        fillData();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();

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
