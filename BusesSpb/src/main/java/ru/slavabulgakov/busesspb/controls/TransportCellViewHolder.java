package ru.slavabulgakov.busesspb.controls;

import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.TransportKind;

public class TransportCellViewHolder {
	public boolean needInflate;
	public TextView leftText;
	public ImageView leftIcon;
	public TextView rightText;

    public Pair<Integer, Integer> backgroundAndIconByKind(TransportKind kind) {
		Integer bgResId = -1;
		Integer iconResId = -1;
		switch (kind) {
		case Bus:
			bgResId = R.drawable.listitem_bg_bus;
			iconResId = R.drawable.bus_30_30;
			break;
			
		case Trolley:
			bgResId = R.drawable.listitem_bg_trolley;
			iconResId = R.drawable.trolley_30_30;
			break;
			
		case Tram:
			bgResId = R.drawable.listitem_bg_tram;
			iconResId = R.drawable.tram_30_30;
			break;
			
		case Ship:
			bgResId = R.drawable.listitem_bg_ship;
			iconResId = R.drawable.ship_30_30;
			break;

		default:
			break;
		}
		return new Pair<Integer, Integer>(bgResId, iconResId);
	}
}