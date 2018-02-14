package com.androidsrc.headlessfragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HeadlessFragment extends Fragment {
	
	public static final String TAG_HEADLESS_FRAGMENT = "headless_fragment";

	private JSONObject jsonObject = null;

	public static  ProgressDialog pd;



	public static interface TaskStatusCallback {
		void onPreExecute();

		void onProgressUpdate(int progress);

		void onPostExecute(JSONObject result);

		void onCancelled();
	}
	
	TaskStatusCallback mStatusCallback;
	BackgroundTask mBackgroundTask;
	public static  boolean isTaskExecuting = false;
	
	/**
	 * Called when a fragment is first attached to its activity. 
	 * onCreate(Bundle) will be called after this.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mStatusCallback = (TaskStatusCallback)activity;
		pd=null;
		pd = new ProgressDialog(activity);
		pd.setTitle("Loading...");
		pd.setMessage("Please Wait...");


	}
	
	/**
	 * Called to do initial creation of a fragment. 
	 * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		pd.dismiss();
		pd=null;
		mStatusCallback = null;
	}

	private class BackgroundTask extends AsyncTask<Void, Integer, JSONObject> {

		@Override
		protected void onPreExecute() {

			if(!pd.isShowing()){
				pd.show();}

			if(mStatusCallback != null)
				mStatusCallback.onPreExecute();
		}
		
		@Override
		protected JSONObject doInBackground(Void... params) {
		/*	int progress = 0;
			while(progress < 100 && !isCancelled()){
				progress++;
				publishProgress(progress);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;*/


			return getJSONFromURL("URL", jsonMake());
		}

		public  JSONObject jsonMake() {
			JSONObject jObj = new JSONObject();


			return jObj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {

			if(mStatusCallback != null){
				pd.dismiss();
				mStatusCallback.onPostExecute(result);

			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if(mStatusCallback != null)
				mStatusCallback.onProgressUpdate(values[0]);
		}

		@Override
		protected void onCancelled(JSONObject result) {
			if(mStatusCallback != null)
				mStatusCallback.onCancelled();
		}

	}

	public void startBackgroundTask() {
		if(!isTaskExecuting){
			mBackgroundTask = new BackgroundTask();
			mBackgroundTask.execute();
			isTaskExecuting = true;
		}
	}

	public void cancelBackgroundTask() {
		if(isTaskExecuting){
			mBackgroundTask.cancel(true);
			isTaskExecuting = false;
		}		
	}
	
	public void updateExecutingStatus(boolean isExecuting){
		this.isTaskExecuting = isExecuting;
	}



	private JSONObject getJSONFromURL(String strURL, JSONObject jObj) {
		try {

			Log.e("URL is -->> ", strURL);
			Log.e("data sending is -->> ", jObj.toString());

			RequestQueue queue = Volley.newRequestQueue((Context) mStatusCallback);

			final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
					Request.Method.POST, strURL, jObj,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							jsonObject = response;
							 Log.e(" check the response here ---------------->>>> ", jsonObject.toString());
						}
					}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					jsonObject = null;
				}

			}) {

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					HashMap<String, String> headers = new HashMap<String, String>();
					headers.put("Accept", "application/json; charset=utf-8");
					headers.put("Content-Type", "application/json; charset=utf-8");
					return headers;
				}
			};

			jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
					20000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			queue.add(jsObjRequest);
			int i = 0, n = 1;
			do {

				// if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
				// Log.e(" EXCEPTION","EXCEPTION");
				// }

				if (jsonObject != null) {
					n = 0;
				}
			} while (i != n);
			return jsonObject;

		} catch (Exception ex) {
			// Log.e(" EXCEPTION here -->> ", ex.toString());
			return jsonObject;
			//return getJSONFromURL(strURL, jObj.toString());
		}

	}
}
