package mreverter.cardsadmin;

import com.mercadopago.model.Card;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mreverter on 3/10/16.
 */
public class DummyCardsCollection {

    private static List<Card> cards;

    public static List<Card> getCards() {
        if(cards == null) {
            cards = new ArrayList<>();
        }
        return cards;
    }

    public static void addCard(Card card) {
        if(cards == null) {
            cards = new ArrayList<>();
        }
        cards.add(card);
    }

    public static void removeCard(Card card) {
        if(cards == null) {
            cards = new ArrayList<>();
        }
        for(Card currentCard : cards) {
            if(currentCard.getId().equals(card.getId())) {
                cards.remove(currentCard);
                break;
            }
        }
    }

    public static Card createCard(PaymentMethod paymentMethod, Issuer issuer, Token token) {
        Card card = new Card();
        card.setId(String.valueOf(Math.random()));
        card.setPaymentMethod(paymentMethod);
        card.setIssuer(issuer);
        card.setLastFourDigits(token.getLastFourDigits());
        return card;
    }
}
