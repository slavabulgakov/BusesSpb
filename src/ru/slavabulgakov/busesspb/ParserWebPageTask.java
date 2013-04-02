package ru.slavabulgakov.busesspb;

import android.os.AsyncTask;


public class ParserWebPageTask extends AsyncTask<Void, Void, Void> {
		
	private IRequest _request;
	private boolean _canceled;
	
	public int getRequestId() {
		return _request.getRequestId();
	}
	
	public ParserWebPageTask(IRequest request) {
		super();
		_request = request;
	}
	
	public interface IRequest {
		public int getRequestId();
		public boolean needExecute();
		public void nextExecute();
		public void setCanceled();
		public void finish();
	}
	
	@Override
	protected void onPreExecute(){
	   super.onPreExecute();
	   _canceled = false;
	} 
	
	@Override
	protected Void doInBackground(Void... params) {
//		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		// выполняем запросы
		while (_request.needExecute()) {
			_request.nextExecute();
			if (_canceled) {
				return null;
			}
		}
		return null;
	}
	
	

	@Override
	protected void onCancelled() {
		super.onCancelled();
		_request.setCanceled();
		_canceled = true;
	}

	@Override
	protected void onPostExecute(Void result) {
		_request.finish();
		super.onPostExecute(result);
	}
}
