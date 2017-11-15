package ricky3350.shenzhen_solitaire_clone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;

public class Card extends AppCompatTextView {

	public final int card;

	public Card(Context context, int card) {
		super(context);

		this.card = card;

		this.setText(nameOfCard(card));
		this.setTextColor(cardColor(card));

		this.setBackgroundColor(Color.LTGRAY);
	}

	// Cards:
	// Numerical: 0b0xxyyyy, xx = color, yyyy = value
	// Dragons: 0b1xx0000, xx = color
	// Solved Dragons: 0b1xx1111, xx = color
	// Rose: 0b1111111
	// Empty: -1
	public static String nameOfCard(final int card) {
		if ((card & 0b1000000) > 0) {
			if (card == Game.ROSE) {
				return "Rose";
			} else if ((card & 0b1001111) == Game.DRAGON_MOD) {
				return "Dragon";
			} else {
				return "X";
			}
		} else {
			return Integer.toString(1 + (card & 0b1111));
		}
	}

	public int cardColor(final int card) {
		switch (card >> 4 & 0b11) {
			case 0b00:
				return ContextCompat.getColor(getContext(), R.color.red);
			case 0b01:
				return ContextCompat.getColor(getContext(), R.color.green);
			default:
				return ContextCompat.getColor(getContext(), R.color.black);
		}
	}

}
