package ru.slavabulgakov.busesspb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;

public class Files {
	
	public static Object loadFromFile(String fileName, Context context) {
		return loadFromFile(fileName, false, context);
	}
	public static Object loadFromFile(String fileName, boolean isSimpleTransportView, Context context) {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(context.openFileInput(fileName));
			try {
				if (isSimpleTransportView) {
					return _readSimpleTransportView(in);
				} else {
					return in.readObject();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException)) {
				
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
		return null;
	}
	public static void saveToFile(Object object, String fileName, Context context) {
		ObjectOutputStream out = null;
		try {
			FileOutputStream fout = context.openFileOutput(fileName, 0);
			out = new ObjectOutputStream(fout);
			if (object.getClass() == SimpleTransportView.class) {
				_writeSimpleTransportView(out, (SimpleTransportView)object);
			} else {
				out.writeObject(object);
			}
			fout.getFD().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	private static SimpleTransportView _readSimpleTransportView(ObjectInputStream in) throws IOException, ClassNotFoundException {
		SimpleTransportView simpleTransportView = new SimpleTransportView();
		
		simpleTransportView.northeastLat = in.readDouble();
		simpleTransportView.northeastLng = in.readDouble();
		simpleTransportView.southwestLat = in.readDouble();
		simpleTransportView.southwestLng = in.readDouble();
		
	    int height=in.readInt();
	    int width=in.readInt();
	    int bmSize=in.readInt();
	    ByteBuffer dst = null;
		byte[] bytesar = null;
	    if(bytesar==null || bmSize > bytesar.length)
	        bytesar= new byte[bmSize];
	    int offset=0;
	    while(in.available()>0){
	        offset=offset + in.read(bytesar, offset, in.available());
	    }
	    if(dst==null || bmSize > dst.capacity())
	        dst = ByteBuffer.allocate(bmSize);
	    dst.position(0);
	    dst.put(bytesar);
	    dst.position(0);
	    simpleTransportView.image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    simpleTransportView.image.copyPixelsFromBuffer(dst);
		
		return simpleTransportView;
	}
	private static void _writeSimpleTransportView(ObjectOutputStream out, SimpleTransportView simpleTransportView) throws IOException {
		out.writeDouble(simpleTransportView.northeastLat);
		out.writeDouble(simpleTransportView.northeastLng);
		out.writeDouble(simpleTransportView.southwestLat);
		out.writeDouble(simpleTransportView.southwestLng);
		
		out.writeInt(simpleTransportView.image.getHeight());
		out.writeInt(simpleTransportView.image.getWidth());
		ByteBuffer dst = null;
		byte[] bytesar = null;
		int bmSize = simpleTransportView.image.getRowBytes() * simpleTransportView.image.getHeight();
	    if(dst==null || bmSize > dst.capacity())
	        dst= ByteBuffer.allocate(bmSize);
	    out.writeInt(dst.capacity());
	    dst.position(0);
	    simpleTransportView.image.copyPixelsToBuffer(dst);
	    if(bytesar==null || bmSize > bytesar.length)
	        bytesar=new byte[bmSize];
	    dst.position(0);
	    dst.get(bytesar);
	    out.write(bytesar, 0, bytesar.length);
	}
}
