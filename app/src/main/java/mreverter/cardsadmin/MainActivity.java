package mreverter.cardsadmin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.model.Card;
import com.mercadopago.model.DecorationPreference;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
import com.mercadopago.uicontrollers.savedcards.SavedCardView;
import com.mercadopago.util.JsonUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int DELETE_CARD_REQUEST = 132;
    private final String DELETE = "delete";
    private final String SELECT = "select";
    private final String NEW = "new";

    private String mCardSelectionPurpose;
    private DecorationPreference mSelectDecorationPreference;

    private ViewGroup mNewCardContainer;
    private ViewGroup mNewCardLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSelectDecorationPreference = new DecorationPreference();
        mSelectDecorationPreference.setBaseColor(ContextCompat.getColor(this, R.color.bacolor));

        mNewCardLayout = (ViewGroup) findViewById(R.id.newCardAddedLayout);
        mNewCardContainer = (ViewGroup) findViewById(R.id.newCardContainer);

        ViewGroup myCards = (ViewGroup) findViewById(R.id.myCards);
        myCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCardSelectionPurpose = SELECT;
                startCustomerCards(mSelectDecorationPreference);
            }
        });
        ViewGroup newCard = (ViewGroup) findViewById(R.id.newCard);
        newCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCardSelectionPurpose = NEW;
                startNewCardFlow();
            }
        });
        ViewGroup deleteCard = (ViewGroup) findViewById(R.id.deleteCards);
        deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDeleteCardFlow();
            }
        });
    }

    private void startDeleteCardFlow() {
        Intent customerCardsIntent = new Intent(this, DeleteCardActivity.class);
        Gson gson = new Gson();
        customerCardsIntent.putExtra("cards", gson.toJson(DummyCardsCollection.getCards()));
        this.startActivityForResult(customerCardsIntent, DELETE_CARD_REQUEST);
    }

    private void startNewCardFlow() {
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey(ExampleUtils.MY_PUBLIC_KEY)
                .setDecorationPreference(mSelectDecorationPreference)
                .setShowBankDeals(false)
                .startCardVaultActivity();
    }

    private void startCustomerCards(DecorationPreference decorationPreference) {
        List<Card> cards = DummyCardsCollection.getCards();
        new MercadoPagoUI.Activities.SavedCardsActivityBuilder()
                .setActivity(this)
                .setCards(cards)
                .setDecorationPreference(decorationPreference)
                .setTitle("Mis Tarjetas")
                .setFooter("Agregar tarjeta…")
                .startActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if(requestCode == MercadoPago.CARD_VAULT_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);
                Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
                Token token = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);

                Card card = DummyCardsCollection.createCard(paymentMethod, issuer, token);
                DummyCardsCollection.addCard(card);
                if(mCardSelectionPurpose.equals(SELECT)) {
                    startCustomerCards(mSelectDecorationPreference);
                } else {
                    showNewCardAdded(card);
                }

            } else {
                if ((data != null) && (data.hasExtra("mpException"))) {
                    MPException exception = JsonUtil.getInstance()
                            .fromJson(data.getStringExtra("mpException"), MPException.class);
                }
            }
        } else if(requestCode == MercadoPago.CUSTOMER_CARDS_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Card card = JsonUtil.getInstance().fromJson(data.getStringExtra("card"), Card.class);
                resolveCardResponse(card);

            } else {
                if ((data != null) && (data.hasExtra("mpException"))) {
                    MPException exception = JsonUtil.getInstance()
                            .fromJson(data.getStringExtra("mpException"), MPException.class);
                }
            }
        }
    }

    private void showNewCardAdded(Card card) {

        Drawable selection = ContextCompat.getDrawable(this, R.drawable.ic_approved);
        selection.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark) ,android.graphics.PorterDuff.Mode.MULTIPLY);

        SavedCardView savedCardView = new MercadoPagoUI.Views.SavedCardViewBuilder()
                .setContext(this)
                .setCard(card)
                .setSelectionDrawable(selection)
                .build();

        mNewCardContainer.setVisibility(View.VISIBLE);

        savedCardView.inflateInParent(mNewCardLayout, true);
        savedCardView.initializeControls();
        savedCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewCardContainer.setVisibility(View.GONE);
            }
        });
        savedCardView.draw();
    }

    private void resolveCardResponse(final Card card) {
        if(card == null) {
            startNewCardFlow();
        }
    }
}
