package dscoshpe.drnick;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Stethascope extends Service {
    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();
    private static final String TAG = "DrNick";
    
    private CellTowerManager cellTowerManager;
	private TelephonyManager telephonyManager;
//	private LocationManager locationManager;
	private ConnectivityManager connectivityManager;
	private SignalStrength mSignalStrength;
    
    public class LocalBinder extends Binder {
        Stethascope getService() {
            return Stethascope.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public void onCreate() {
    	// Start it up in the foreground to keep it alive
    	Notification notification = new Notification(R.drawable.icon, "Don't worry. You won't feel a thing... ", System.currentTimeMillis());
        
    	Intent notificationIntent = new Intent(this, DrNick.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	notification.setLatestEventInfo(this, "Dr. Nick", "Hi everybody!", pendingIntent);
    	
    	this.cellTowerManager = new CellTowerManager();
    	
    	this.connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	this.telephonyManager.listen(this.phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    	
//    	this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//    	this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this.locationListener);
    	
    	this.startForeground(Notification.FLAG_ONGOING_EVENT |
    			             Notification.FLAG_NO_CLEAR |
    			             Notification.FLAG_FOREGROUND_SERVICE, notification);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return Service.START_STICKY;
    }
    
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
    	
    	@Override
    	public void onCellLocationChanged(CellLocation location) {
    		super.onCellLocationChanged(location);
    		cellTowerManager.register(location);
    	}
    	
    	@Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    		super.onSignalStrengthsChanged(signalStrength);
    		mSignalStrength = signalStrength;
    	}
    };
	
	public static enum Config {
		TRUST_CACHE
	}
    
	public static enum Evaluation_Status {
		OK,	INCONCLUSIVE, SUSPICIOUS
	}
	
	private class CellTowerDetails {
		private HashMap<String, String> features;
		private Evaluation_Status status;
		
		public CellTowerDetails(Evaluation_Status status, HashMap<String, String> features) {
			this.features = features;
			this.status = status;
		}
		
//		// TODO: i dont know about this API... probably needs changing
//		// do all of the actual evaluation in the get status?
//		public Evaluation_Status getStatus(boolean forceRefresh) {
//			if (forceRefresh) {
//				//doSecureEvalPing()
//			}
//			
//			return status;
//		}
		
	}

    private class CellTowerManager {
        // cell tower cache
    	private HashMap<String, CellTowerDetails> cellTowerList = new HashMap<String, CellTowerDetails>();
    	
    	// operational settings
    	//private HashMap<Config, Object> opConfig = new HashMap<Config, Object>();
    	
    	// called by the event listeners, initiates tower validation process
        public void register(CellLocation location) {
        	
        	// populate the current tower association's details
        	HashMap<String, String> details = getDetails(location);
        	
        	String id = getTowerId(details);
        	if (cellTowerList.containsKey(id)) {
        		cellTowerList.remove(id);
        	}
        	cellTowerList.put(id, new CellTowerDetails(Evaluation_Status.INCONCLUSIVE, details));
        	
        	//postData(details);
        }
        
        // this populates all of the data for the server post
    	public HashMap<String, String> getDetails(CellLocation location) {
    		HashMap<String, String> details = new HashMap<String, String>();
    		
    		ReflectionUtils.dumpDetails(details, location.getClass(), location, "");
    		ReflectionUtils.dumpDetails(details, SignalStrength.class, mSignalStrength, "");
    		ReflectionUtils.dumpDetails(details, TelephonyManager.class, telephonyManager, "");
    		
    		getLocalIpAddresses(details);
    		details.put("request_timestamp", String.valueOf(new Date().getTime()));
    		
    		//Log.e(TAG, details.toString());
    		appendLog(details.toString());
    		return details;
    	}
    	
    	private void appendLog(String text) {
    		File logFile = new File("sdcard/DrNick.log");
    		if (!logFile.exists()) {
    			try {
    				logFile.createNewFile();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		try {
    			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
    			buf.append(text);
    			buf.newLine();
    			buf.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	private List<NameValuePair> convertParameters(HashMap<String, String> parameters) {
    	    List<NameValuePair> result = new ArrayList<NameValuePair>();

    	    for (Entry<String, String> entry : parameters.entrySet()) {
    	        result.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
    	    }

    	    return result;
    	}
    	
    	public HttpResponse postData(HashMap<String, String> details) {
    		List<NameValuePair> postDetails = convertParameters(details);
    		
    	    // Create a new HttpClient and Post Header
    	    HttpClient httpclient = new DefaultHttpClient();
    	    HttpPost httppost = new HttpPost("http://somesite.com:19999/check");
    	    
    	    try {
    	        // Execute HTTP Post Request
    	    	httppost.setEntity(new UrlEncodedFormEntity(postDetails));
    	    	Log.e(TAG, httppost.getEntity().getContent().toString());
    	        HttpResponse response = httpclient.execute(httppost);
    	        return response;
    	    } catch (ClientProtocolException e) {
    	        // TODO Auto-generated catch block
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
    	    }
    	    
    	    return null;
    	} 
    	
    	private void getLocalIpAddresses(HashMap<String, String> details) {
    		Enumeration<NetworkInterface> interfaces = null;
    	    try {
    	        //the WiFi network interface will be one of these.
    	        interfaces = NetworkInterface.getNetworkInterfaces();
    	    } catch (SocketException e) {
    	        return;
    	    }
    	    
    	    while (interfaces.hasMoreElements()) {

    	        NetworkInterface iface = interfaces.nextElement();
    	        String prefix = iface.getName() + "_";
    	        
//    	        try {
    	        	details.put(prefix + "getName", iface.getName());
//				details.put(prefix + "getMTU", String.valueOf(iface.getMTU()));
//					details.put(prefix + "isLoopback", String.valueOf(iface.isLoopback()));
//					details.put(prefix + "isPointToPoint", String.valueOf(iface.isPointToPoint()));
//					details.put(prefix + "isUp", String.valueOf(iface.isUp()));
//					details.put(prefix + "isVirtual", String.valueOf(iface.isVirtual()));
//					details.put(prefix + "supportsMulticast", String.valueOf(iface.supportsMulticast()));
//				} catch (SocketException e) {}
    	        
    	        //since each interface could have many InetAddresses...
    	        Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
    	        int count = -1;
    	        while (inetAddresses.hasMoreElements()) {
    	        	count++;
    	            InetAddress nextElement = inetAddresses.nextElement();
    	            String aPrefix = prefix + String.valueOf(count) + "_";
//    	            ReflectionUtils.dumpDetails(details, InetAddress.class, nextElement, aPrefix);
    	            details.put(aPrefix + "getHostAddress", nextElement.getHostAddress());
    	        }
    	    }
    	    
    	}
    	
    	// TODO: need to make the tower ID a hash of easy info
    	private String getTowerId(HashMap<String, String> details) {
    		String id = "";
    		
    		if (details.containsKey("mBaseStationId"))
    			id = details.get("mBaseStationId");
    		if (details.containsKey("blah"))
    			id = details.get("blah");
    		
    		return id;
    	}
    	
    	public String printDetails() {
    		StringBuilder output = new StringBuilder();

    		for (Map.Entry<String, CellTowerDetails> entry : this.cellTowerList.entrySet()) {
    		    output.append(entry.getKey() + ": " + entry.getValue().features + "\n");
    		}

    		return output.toString();
    	}
    	
    }
    
    public String getCellTowerList() {
    	return this.cellTowerManager.printDetails();
    }
    
    public int getCellTowerCount() {
    	return this.cellTowerManager.cellTowerList.size();
    }

    /** method for clients */
    public int getRandomNumber() {
      return mGenerator.nextInt(100);
    }


}
