package ru.slavabulgakov.busesspb;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.maps.model.UrlTileProvider;

public class MyUrlTileProvider extends UrlTileProvider {

	public MyUrlTileProvider(int arg0, int arg1) {
		super(arg0, arg1);
	}

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
		String url = "http://transport.orgp.spb.ru/tms/1.0.0/ru/{z}/{x}/{y}.png";
		URL newUrl = null;
		try {
			newUrl = new URL(url.replace("{z}", ""+zoom).replace("{x}",""+x).replace("{y}",""+y));
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    return newUrl;
	}

}
