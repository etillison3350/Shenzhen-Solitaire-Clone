package ricky3350.shenzhen_solitaire_clone;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

//	private VelocityTracker velocityTracker = null;

	private RelativeLayout layout;
	private BidirectionalHashMap<Location, Card> cards = new BidirectionalHashMap<>();
	private BidirectionalHashMap<Location, Card> unplacedCards = new BidirectionalHashMap<>();

	private Card movingCard = null;

	private Game game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		game = new Game();
		for (int s = 0; s < 8; s++) {
			for (int i = 0; i < 5; i++) {
				Card card = new Card(getApplicationContext(), game.cardAt(s, i));
				unplacedCards.put(new Location(s, i), card);
			}
		}

		layout = new RelativeLayout(getApplicationContext()) {

			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				relayout(w, h);
			}
		};

		layout.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//MainActivity.this.
						break;
					case MotionEvent.ACTION_MOVE:
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
						params.topMargin = (int) event.getY();
						params.leftMargin = (int) event.getX();
						findViewById(R.id.imageView).setLayoutParams(params);
				}

				return true;
			}

		});

		setContentView(layout);
	}

	private void relayout(int width, int height) {

	}
}
