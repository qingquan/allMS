package yl.demo.pathHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import wh.TestGetWifiInformation.MapView.OnTargetSettledListner;
import yl.demo.pathHelper.db.DBManager;
import yl.demo.pathHelper.db.model.Corner;
import yl.demo.pathHelper.pathSearching.algrorithm.SearchPathAlgorithm;
import yl.demo.pathHelper.pathSearching.location.Location;
import yl.demo.pathHelper.util.FileUtil;
import yl.demo.pathHelper.util.PreferenceUtils;
import yl.demo.pathHelper.view.MapView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class MainActivity extends Activity 
implements SensorEventListener{
	
	private MapView mMapView;
	private ImageView mZoomInButton;
	private ImageView mZoomOutButton;
	private Button mSearchPathButton;
	private SearchPathAlgorithm mSearchAlgorithm;
	private Location mMyLocation;
	private Context mContext;
	private EditText mEditTextD;
	private EditText mDistanceY;
	
	private Sensor mAccelerometer;
	private Sensor mGyro;
	private Sensor mLinearAcc;
	private Sensor mMagnet;
	private SensorManager mSensorManager;

    private float[] magnet = new float[3];
    private float[] accel = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] accMagOrientation = new float[3];
    
    //for sampling data
    private ArrayList<float[]> accList = new ArrayList<float[]>();
    private ArrayList<float[]> gyroList = new ArrayList<float[]>();
    private ArrayList<float[]> smoothedAccList = new ArrayList<float[]>();
    private ArrayList<float[]> accSampleList = new ArrayList<float[]>();
    private ArrayList<float[]> velocityList = new ArrayList<float[]>();
    private ArrayList<float[]> current1sList = new ArrayList<float[]>();
    private ArrayList<float[]> previous1sList = new ArrayList<float[]>();
    //for test
    private ArrayList<float[]> rawAccList = new ArrayList<float[]>();

    private float[] previousRawAcc = new float[4];
    private float[] previousSmoothedAcc = new float[4];
    private float[] previousFinalAcc = new float[4];
    private float[] previousVelocity = new float[4];
    private float[] previousDistance = new float[4];
    private float[] previous1s = new float[4];
    private boolean mInit;
    private boolean initDistanceVar;
    //for test
	private static final String FILENAME = "gyro_data.txt";
    private ArrayList<float[]> previousList = new ArrayList<float[]>();
    private ArrayList<float[]> distanceList = new ArrayList<float[]>();


    private float startTime;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mInit = false;
		initDistanceVar = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		initSensors();
		initComponents();
		initViews();
		initListeners();
		initMapResource();
	}
	
	private void initSensors(){
		mLinearAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	
	private void initDb() {
//		PreferenceUtils.saveBooleanValue(null, PreferenceUtils.KEY_HAS_NO_DB, true);
		if(PreferenceUtils.getBooleanValue(null, PreferenceUtils.KEY_HAS_NO_DB)) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					FileUtil.initDatabase(getApplicationContext(), "ap_data.db");
				}
			}).start();
			PreferenceUtils.saveBooleanValue(null, PreferenceUtils.KEY_HAS_NO_DB, false);
		}
	}
	
	private void initComponents() {
		// TODO Auto-generated method stub
		mSearchAlgorithm = new SearchPathAlgorithm(); 
		mMyLocation = new Location(1,2,500,500);
		PreferenceUtils.initPreference(mContext);
		DBManager.setContext(this);
		initDb();
		
	}

	private void initViews() {
		mMapView = (MapView) findViewById(R.id.mapview);
		mZoomInButton = (ImageView) findViewById(R.id.zoomin_button);
		mZoomOutButton = (ImageView) findViewById(R.id.zoomout_button);
		mSearchPathButton = (Button) findViewById(R.id.path_button);
		mEditTextD = (EditText) findViewById(R.id.distance);
		mDistanceY = (EditText) findViewById(R.id.distance_Y);
	}
	
	private void initListeners() {
		View.OnClickListener lsn = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.zoomin_button:
					mMapView.upMapScaleRate();
					break;
				case R.id.zoomout_button:
					mMapView.downMapScaleRate();
				case R.id.path_button:
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							PointF sourcepointF = mMapView.getMyPosition();
							Location sourceLocation = new Location(1, 2, sourcepointF.x, sourcepointF.y);
							PointF targetpointF = mMapView.getDestinationPosition();
							Location targetLocation = new Location(1, 2, targetpointF.x, targetpointF.y);
							List<Corner> list = mSearchAlgorithm.findPathBySPFA(sourceLocation, targetLocation);
							
							if ( list != null ) {
								List<PointF> pointFs = new ArrayList<PointF>();
								pointFs.add(new PointF(sourcepointF.x, sourcepointF.y));
								for ( int i = 0; i < list.size(); i++ ) {
									pointFs.add(new PointF(list.get(i).getX().floatValue(), list.get(i).getY().floatValue()));
								}
								pointFs.add(new PointF(targetpointF.x,targetpointF.y));
								mMapView.setPath(pointFs);
							}
						}
					}).start();
					break;
				default:
					break;
				}
				
			}
		};

		mZoomInButton.setOnClickListener(lsn);
		mZoomOutButton.setOnClickListener(lsn);
		mSearchPathButton.setOnClickListener(lsn);
		
		sensorListener();
	}

	private void initMapResource() {
		// TODO Auto-generated method stub
		mMapView.setMapBitmap(FileUtil.getBitmapFromRes(mContext, R.drawable.map));
		mMapView.setMyPositionBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.user_maker));
		mMapView.setDestinationBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.car_maker));
		mMapView.setScaleRate(1f);
		mMapView.setMyPosition((float)mMyLocation.x, (float)mMyLocation.y);
//		mMapView.seDt
	}
	
	protected void onResume() {
	      super.onResume();
	      sensorListener();
	}

	private void sensorListener(){
	      mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
	      mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	      mSensorManager.registerListener(this, mLinearAcc, SensorManager.SENSOR_DELAY_NORMAL);
	      mSensorManager.registerListener(this, mMagnet, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	protected void onPause() {
	      super.onPause();
	      mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// can be safely ignored for this demo
	}
		
	@Override
	public void onSensorChanged(SensorEvent event){
		float time = event.timestamp;
		
		if(!mInit){
			startTime = time;
			initVar(time);
			mInit = true;
		}
		
		synchronized (this) {
			switch(event.sensor.getType()) {
//			case Sensor.TYPE_ACCELEROMETER:
//
//		        // copy new accelerometer data into accel array and calculate orientation
//		        System.arraycopy(event.values, 0, accel, 0, 3);
//		        calculateAccMagOrientation();
//		        break;
		        
			case Sensor.TYPE_LINEAR_ACCELERATION:
				
				float [] linearAcc = new float[3];
				// copy new accelerometer data into accel array and calculate orientation
				System.arraycopy(event.values, 0, linearAcc, 0, 3);
				getAccSamplingData(time, linearAcc);
				break;
				
//			case Sensor.TYPE_LINEAR_ACCELERATION:
//
//				float [] linearAcc = new float[3];
//				// copy new accelerometer data into accel array and calculate orientation
//				System.arraycopy(event.values, 0, linearAcc, 0, 3);
//				getAccSamplingData(time, linearAcc);
//				break;
		 
		    case Sensor.TYPE_GYROSCOPE:

		        // process gyro data
//		        gyroFunction(event);
		        break;
		 
		    case Sensor.TYPE_MAGNETIC_FIELD:
		        // copy new magnetometer data into magnet array
		        System.arraycopy(event.values, 0, magnet, 0, 3);

		        break;
		    }
		}
	}
	
	//for test
	public void sensorPause(View view){
		// pause the sensor change
		writeToFile();
		onPause();
	}

	public void writeToFile(){
		String myString = convertToString();
		File file = new File(Environment.getExternalStorageDirectory() + File.separator + "acc_data.txt");
		try{
			file.createNewFile();
			OutputStream fo = new FileOutputStream(file);              
		    fo.write(myString.getBytes());
		    fo.close();
			
//			FileOutputStream fOut = openFileOutput(FILENAME, Context.MODE_PRIVATE);
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
//			outputStreamWriter.write(myString);
//			outputStreamWriter.close();
//			fOut.close();
		}
		catch(IOException e){
			Log.e("acc", "File write failed: "+e.toString());
		}
		sendEmail();
	}
	
	
	public void sendEmail(){
		
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		File f = new File(dir+"/acc_data.txt");
		if(f.exists()){
			Log.d("myapp", "file exist!");
		}else{
			Log.d("myapp", "file not exist!");
		}	
		
		Log.d("the app-path", dir);
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"boji.hit@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject of Email");
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+dir+"/acc_data.txt"));
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Enjoy the mail");
        startActivity(Intent.createChooser(sendIntent, "Email:"));   
	}

	public String convertToString(){
		String str = "";
//		for(int i=0; i<accList.size(); i++){
//			str += String.format("%f", accList.get(i)[0])+" ";
//			str += String.format("%f", accList.get(i)[1])+" ";
//			str += String.format("%f", accList.get(i)[2])+" ";
//			str += String.format("%f", accList.get(i)[3])+"\n";
//		}
		for(int i=0; i<rawAccList.size(); i++){
			str += String.format("%f", rawAccList.get(i)[0])+" ";
			str += String.format("%f", rawAccList.get(i)[1])+" ";
			str += String.format("%f", rawAccList.get(i)[2])+" ";
			str += String.format("%f", rawAccList.get(i)[3])+"\n";
		}
//		for(int i=0; i<previousList.size(); i++){
//			str += String.format("%f", previousList.get(i)[0])+" ";
//			str += String.format("%f", previousList.get(i)[1])+" ";
//			str += String.format("%f", previousList.get(i)[2])+" ";
//			str += String.format("%f", previogetAccSamplingDatausList.get(i)[3])+"\n";
//		}
		
//		for(int i=0; i<smoothedAccList.size(); i++){
//			str += String.format("%f", smoothedAccList.get(i)[0])+" ";
//			str += String.format("%f", smoothedAccList.get(i)[1])+" ";
//			str += String.format("%f", smoothedAccList.get(i)[2])+" ";
//			str += String.format("%f", smoothedAccList.get(i)[3])+"\n";
//		}
//		for(int i=0; i<velocityList.size(); i++){
//			str += String.format("%f", velocityList.get(i)[0])+" ";
//			str += String.format("%f", velocityList.get(i)[1])+" ";
//			str += String.format("%f", velocityList.get(i)[2])+" ";
//			str += String.format("%f", velocityList.get(i)[3])+"\n";
//		}
//		for(int i=0; i<distanceList.size(); i++){
//			str += String.format("%f", distanceList.get(i)[0])+" ";
//			str += String.format("%f", distanceList.get(i)[1])+" ";
//			str += String.format("%f", distanceList.get(i)[2])+" ";
//			str += String.format("%f", distanceList.get(i)[3])+"\n";
//		}

		return str;
	}
	
	private void initDistanceVar(float time){
		previous1s[0] = time;
		previous1s[1] = (float)0; previous1s[2] = (float)0; previous1s[3] = (float)0;
		
		previousFinalAcc[0] = time;
		previousFinalAcc[1] = (float)0; previousFinalAcc[2] = (float)0; previousFinalAcc[3] = (float)0;

		previousVelocity[0] = time;
		previousVelocity[1] = (float)0; previousVelocity[2] = (float)0; previousVelocity[3] = (float)0;
		
		previousDistance[0] = time;
		previousDistance[1] = (float)0; previousDistance[2] = (float)0; previousDistance[3] = (float)0;
		
//		float initGyro[] = {time, (float)0, (float)0, (float)0};
//		gyroList.add(initGyro);
	}

	private void initVar(float time){

//		
//		float initGyro[] = {time, (float)0, (float)0, (float)0};
//		gyroList.add(initGyro);
	}
	
	//calculate the distance using sampling data
	public void calculateDistance(float[] currentAcc){
		float alpha = (float)0.39;
		float dt = (currentAcc[0]-previousSmoothedAcc[0])/1000000000;
		float lowpassAcc[] = new float[4];
		float mPreviousAcc[] = new float[4];
		float finalAcc[] = new float[4];
		float velocity[] = new float[4];
		float distance[] = new float[4];
		lowpassAcc[0] = currentAcc[0];
		finalAcc[0] = currentAcc[0];
		mPreviousAcc[0] = currentAcc[0];
		
		//for test 
		rawAccList.add(currentAcc);
		
		//get rid of the offset first
//		currentAcc[1] -= 0.0147267; currentAcc[2] += 0.04636421; currentAcc[3] += 0.1491198;
		
		//use low-pass filter to smooth the data, previous data is smoothed data
//		for(int i=1; i<4; i++){
//			lowpassAcc[i] = alpha*currentAcc[i]+(1-alpha)*previousSmoothedAcc[i];
//		}
		
//		Log.d("myapp", "previous is"+Float.toString(previousSmoothedAcc[1])+"  "+Float.toString(previousSmoothedAcc[2]));
//		Log.d("myapp", "raw acc is"+Float.toString(currentAcc[1])+"  "+Float.toString(currentAcc[2]));
//		Log.d("myapp", "the smoothed acc"+Float.toString(lowpassAcc[1])+"  "+Float.toString(lowpassAcc[2]));
		
		//for test
//		accList.add(currentAcc);


		/*//for test
		for(int i=1; i<4; i++){
			mPreviousAcc[i] = previous1s[i];
		}
		Log.d("myapp", "smoothing acc is"+Float.toString(lowpassAcc[1])+"  "+Float.toString(lowpassAcc[2]));
		Log.d("myapp", "the previous 1s mean is"+Float.toString(previous1s[1])+"  "+Float.toString(previous1s[2]));
*/
		
//		//for test
//		previousList.add(mPreviousAcc);
		
		//minus the previous one, only for stationary condition
//		for(int i=1; i<4; i++){
//			finalAcc[i] = lowpassAcc[i] - previous1s[i];
//		}
//		Log.d("myapp", "the final acc is"+Float.toString(finalAcc[1])+"  "+Float.toString(finalAcc[2]));

		
		//for test
//		smoothedAccList.add(finalAcc);
		
		//reduce the offset first
		currentAcc[1] = currentAcc[1] - (float)0.0821;
		currentAcc[2] = currentAcc[2] + (float)0.4764;
		
		velocity[0] = currentAcc[0];
		for(int i=1; i<4; i++){
//			velocity[i] = (previousAcc[i]+(currentAcc[i]-previousAcc[i])/2)*dt;
//			velocity[i] = previousVelocity[i] + (previousFinalAcc[i]+(finalAcc[i]-previousFinalAcc[i])/2)*dt;
			velocity[i] = previousVelocity[i] + (previousRawAcc[i]+(currentAcc[i]-previousRawAcc[i])/2)*dt;
		}
		
		//for test
		velocityList.add(velocity);
		
		//second integration
		distance[0] = currentAcc[0];
		for(int i=1; i<4; i++){
//			distance[i] = (previousVelocity[i]+(velocity[i]-previousVelocity[i])/2)*dt;
			distance[i] = previousDistance[i] + (previousVelocity[i]+(velocity[i]-previousVelocity[i])/2)*dt;
		}
		
		//for test
		distanceList.add(distance);
//		Log.d("myapp", "the distance"+Float.toString(distance[1])+Float.toString(distance[2]));
		System.arraycopy(lowpassAcc, 0, previousRawAcc, 0, currentAcc.length);
		System.arraycopy(lowpassAcc, 0, previousSmoothedAcc, 0, lowpassAcc.length);
		System.arraycopy(finalAcc, 0, previousFinalAcc, 0, finalAcc.length);
		System.arraycopy(velocity, 0, previousVelocity, 0, previousVelocity.length);
		System.arraycopy(distance, 0, previousDistance, 0, previousDistance.length);

		mEditTextD.setText(distance[1]+"");
		mDistanceY.setText(distance[2]+"");
		//redraw the map
		mMapView.setMyPosition((float)(mMyLocation.x+distance[1]), (float)(mMyLocation.y+distance[2]));
//		mMapView.seDtS
		
	}

	//get the sampling data in the timespan
	public void getAccSamplingData(float time, float[] linearAcc){
		int temp = 0;
		
		float mAcc[] = {time, linearAcc[0], linearAcc[1], linearAcc[2]};


//		//when the list is empty or less than
//		if(accList.isEmpty() || (time-accList.get(0)[0])/1000000000 < 0.2){
//			float mAcc[] = {time, linearAcc[0], linearAcc[1], linearAcc[2]};
//			accList.add(mAcc);
//		}else{ //only use the mean of the data in this timespan
		
		
		//bug:lost the new coming data in the sampling period
//		float sum_x = 0; float sum_y = 0; float sum_z = 0;
//		for(int i = 0; i < accList.size(); i++){
//			sum_x += accList.get(i)[1];
//			sum_y += accList.get(i)[2];
//			sum_z += accList.get(i)[3];		    					
//		}
//		float mean_x = sum_x/accList.size();
//		float mean_y = sum_y/accList.size();
//		float mean_z = sum_z/accList.size();
//		float mMean[] = {time, mean_x, mean_y, mean_z};
//		accSampleList.add(mMean);
		//clear the accList data
//		accList.clear();
		
		//ignore the 15s since starting
//		if((time-startTime)/1000000000 > 4){
			//init some variable
			if(!initDistanceVar){
				initDistanceVar(time);
				System.arraycopy(mAcc, 0, previousSmoothedAcc, 0, mAcc.length);
//				Log.d("myapp", "raw previous acc is"+Float.toString(previousSmoothedAcc[1])+"  "+Float.toString(previousSmoothedAcc[2]));
				initDistanceVar = true;
				return;
			}
			
			//algorithm only for stationary
			//init previous1slist, collect the previous data
//			if(previous1sList.isEmpty()){
//				previous1sList.add(mAcc);
//			}else{
//				//to get the data falling into the required time interval
//				//potential bug for i+1 index
//				for(int i=0; i<previous1sList.size(); i++){
//					if((time-previous1sList.get(i)[0])/1000000000 < 1){
//						temp = i;
//						break;
//					}else{
//						temp = previous1sList.size()-1;
//					}
//				}
////				for(int i=0; i<previous1sList.size(); i++){
////					if(((time-previous1sList.get(i)[0])/1000000000 > 1) && ((time-previous1sList.get(i+1)[0])/1000000000 < 1)){
////						temp = i+1;
////						break;
////					}else{
////						temp = 0;
////					}
////				}
////				Log.d("myapp", "temp is"+Float.toString(temp)+"  "+Float.toString(previous1sList.size()));
//
//				//calculate the previous1s mean data for ervery new coming data
//				float previous1s_x = 0; float previous1s_y = 0; float previous1s_z = 0;
//				for(int i = temp; i < previous1sList.size(); i++){
//					previous1s_x += previous1sList.get(i)[1];
//					previous1s_y += previous1sList.get(i)[2];
//					previous1s_z += previous1sList.get(i)[3];		    					
//				}
//				previous1s[1] = previous1s_x/(previous1sList.size()-temp);
//				previous1s[2] = previous1s_y/(previous1sList.size()-temp);
//				previous1s[3] = previous1s_z/(previous1sList.size()-temp);
//				
////				Log.d("myapp", "smoothing acc is"+Float.toString(previous1s[1])+"  "+Float.toString(previous1s[2]));
//
//				//copy the previous1s data into a new array, and add the new coming data into
//				for(int i=temp; i<previous1sList.size(); i++){
//					current1sList.add(previous1sList.get(i));
//				}
//				Log.d("myapp", "temp is"+Float.toString(temp)+"  "+Float.toString(previous1sList.size()));
//
//				current1sList.add(mAcc);
//				previous1sList.clear();
//				previous1sList = (ArrayList<float[]>)current1sList.clone();
//				current1sList.clear();
//					System.arraycopy(previous1sList, temp, current1sList, 0, previous1sList.size()-temp);
//					System.arraycopy(current1sList, 0, previous1sList, 0, current1sList.size());
//			}
			
			//call drawing function to redraw the map
			calculateDistance(mAcc);
//		}
//		}
	}
	
	//calculate angle using magnet and accelerometer
	public void calculateAccMagOrientation(){
	    if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
	        SensorManager.getOrientation(rotationMatrix, accMagOrientation);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
