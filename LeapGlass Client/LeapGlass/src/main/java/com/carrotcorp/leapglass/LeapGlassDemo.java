package com.carrotcorp.leapglass;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;

/**
 * This is a demo of the LeapGlass Project.
 * Code by Thomas Suarez, Chief Engineer @ CarrotCorp
 */
public class LeapGlassDemo extends Activity {
    private ArrayList<Card> cards; // cards for the Glass user to scroll through

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the view
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_demo);

        // Set up Glass Cards
        createCards();
        CardScrollView cardScrollView = new CardScrollView(this);
        LeapGlassCardScrollAdapter adapter = new LeapGlassCardScrollAdapter();
        cardScrollView.setAdapter(adapter);
        cardScrollView.activate();
        setContentView(cardScrollView);
    }

    /**
     * Creates the Cards for the Glass user to scroll through
     */
    private void createCards() {
        cards = new ArrayList<Card>();

        Card card;

        card = new Card(this);
        card.setText("Welcome to LeapGlass Demo!");
        card.setFootnote("Swipe to continue");
        cards.add(card);

        card = new Card(this);
        card.setText("Very Glass. Much doge.");
        card.setFootnote("Such need Leap Motion.");
        card.setImageLayout(Card.ImageLayout.FULL);
        card.addImage(R.drawable.suchdogeglass);
        cards.add(card);
    }

    /**
     * Handles scroll view cards
     */
    private class LeapGlassCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return cards.indexOf(item);
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Object getItem(int position) {
            return cards.get(position);
        }

        /**
         * Returns the amount of view types.
         */
        @Override
        public int getViewTypeCount() {
            return Card.getViewTypeCount();
        }

        /**
         * Returns the view type of this card so the system can figure out
         * if it can be recycled.
         */
        @Override
        public int getItemViewType(int position){
            return cards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            return cards.get(position).getView(convertView, parent);
        }
    }

}
