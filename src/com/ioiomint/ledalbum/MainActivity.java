package com.ioiomint.ledalbum;



import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
//import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient; 
import alt.android.os.CountDownTimer;

/**
 * Displays images from an SD card.
 */
@SuppressLint({ "ParserError", "NewApi" })
public class MainActivity extends IOIOActivity implements OnItemClickListener, OnItemLongClickListener  {

    //look into internet loads
	//make an  directory, copy some files in there
    private int columnIndex;   
	private ioio.lib.api.RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private android.graphics.Matrix matrix2;
    private String filename;
    private static final String LOG_TAG = "pixelart";	  	
  	private int z = 1;
  	private short[] frame_ = new short[512];
  	private short[] rgb_;
  	public static final Bitmap.Config FAST_BITMAP_CONFIG = Bitmap.Config.RGB_565;
  	private Bitmap frame1;
  	private byte[] BitmapBytes;
  	private byte[] BitmayArray;
  	private byte[] dotArray;
  	private InputStream BitmapInputStream;
  	private ByteBuffer bBuffer;
  	private ShortBuffer sBuffer;
  	private SensorManager mSensorManager;
  	private Random randomGenerator = new Random();
  	private Bitmap canvasBitmap;
  	private Bitmap originalImage;
  	private int width_original;
  	private int height_original; 	  
  	private float scaleWidth; 
  	private float scaleHeight; 	  	
  	private Bitmap resizedBitmap;  	
  	private int i = 0;
  	private int deviceFound = 0;
  	private Handler mHandler;
  	
  	private SharedPreferences prefs;
	private String OKText;
	private Resources resources;
	private String app_ver;	
	private int matrix_model;
	
	///********** Timers
	private ConnectTimer connectTimer; 
	private MediaScanTimer mediascanTimer; 
	private ImageDisplayDurationTimer imagedisplaydurationTimer;
	private PauseBetweenImagesDurationTimer pausebetweenimagesdurationTimer;
	//****************
	
	private boolean scanAllPics;
	private String setupInstructionsString; 
	private String setupInstructionsStringTitle;
	private int countdownCounter;
	private static final int countdownDuration = 30;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	private  OnTouchListener gestureListener;
	private int size;  //the number of pictures
	private int slideshowPosition = 0;
	private String imagePath;
	private int slideShowRunning = 0;
	private boolean noSleep = false;
	private int resizedFlag = 0;
	public long frame_length;
	private int x = 0;
	
	/**
     * Grid view holding the images.
     */
    private GridView sdcardImages;
    /**
     * Image adapter for the grid view.
     */
    private ImageAdapter imageAdapter;
    /**
     * Display used for getting the width of the screen. 
     */
    private Display display;
    
    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
 //   private String basepath = extStorageDirectory + "/";
    private String basepath = extStorageDirectory;
    private String artpath = "/media";
    
    private Cursor cursor;
 //   private String[] projection;
    
     private Context context;
     private Context frameContext;
   // private MediaScannerConnection mConnection;
   // private String mPath;
  //  private String mMimeType; 
     
     private boolean slideShowMode;
     private int imageDisplayDuration;
     private int pauseBetweenImagesDuration;
     private GridView sdcard_;
     private TextView firstTimeSetup1_;
 	private TextView firstTimeSetup2_;
 	private TextView firstTimeInstructions_;
 	private TextView firstTimeSetupCounter_;
     private ProgressDialog pDialog = null;
  //   private boolean dimDuringSlideShow = false;
    private boolean debug_;
    private int appAlreadyStarted = 0;
    private ioio.lib.api.RgbLedMatrix matrix_;
    private static String pixelFirmware = "Not Found";
 	private static String pixelBootloader = "Not Found";
 	private static String pixelHardwareID = "Not Found";
 	private static String IOIOLibVersion = "Not Found";
 	private static VersionType v;
 	private  ProgressDialog progress;
 	private static String decodedDirPath =  Environment.getExternalStorageDirectory() + "/pixel/pngs/decoded"; 
 	//private static int selectedFileTotalFrames;
	private static int selectedFileDelay;
	private static int selectedFileResolution;
	private static int currentResolution;
	private static float fps;
     
     
     //add long click to delete an image
     

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode
        // Request progress bar
      //  requestWindowFeature(Window.FEATURE_NO_TITLE); //full screen app
        
   //     WindowManager.LayoutParams lp = getWindow().getAttributes();  //turn the screen back on
	//	lp.screenBrightness = 0 / 100.0f;  
	//	getWindow().setAttributes(lp);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.sdcard);
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        
        
        
      //  sdcard_ = (Gridview) findViewById(R.id.sdcard); 
       sdcardImages = (GridView) findViewById(R.id.sdcard);
      // firstTimeSetupCounter_ = (TextView) findViewById(R.id.firstTimeSetupCounter);
      
	   //firstTimeSetupCounter_.setVisibility(View.INVISIBLE);
	   
	  // sdcardImages = (GridView) findViewById(R.id.sdcard);
	      firstTimeSetup1_ = (TextView) findViewById(R.id.firstTimeSetup1);
	      firstTimeSetup2_ = (TextView) findViewById(R.id.firstTimeSetup2);
	      firstTimeInstructions_ = (TextView) findViewById(R.id.firstTimeInstructions);
	      firstTimeSetupCounter_ = (TextView) findViewById(R.id.firstTimeSetupCounter);
	      
	      firstTimeSetup1_.setVisibility(View.INVISIBLE);
	      firstTimeSetup2_.setVisibility(View.INVISIBLE);
	      firstTimeInstructions_.setVisibility(View.INVISIBLE);
	      firstTimeSetupCounter_.setVisibility(View.INVISIBLE);
       
    // Gesture detection 
       gestureDetector = new GestureDetector(new MyGestureDetector());
       gestureListener = new View.OnTouchListener() {
    	   public boolean onTouch(View v, MotionEvent event) { 
    		   return gestureDetector.onTouchEvent(event);
    		   }
    	   };
       
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e)
        {
            Log.v(LOG_TAG, e.getMessage());
        }
        
        //******** preferences code
        resources = this.getResources();
        setPreferences();
        //***************************
        
        if (noSleep == true) {        	      	
        	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //disables sleep mode
        }	
        
       
        
        connectTimer = new ConnectTimer(30000,5000); //pop up a message if it's not connected by this timer
 		connectTimer.start(); //this timer will pop up a message box if the device is not found
 		
 		
 		setupInstructionsString = getResources().getString(R.string.setupInstructionsString);
        setupInstructionsStringTitle = getResources().getString(R.string.setupInstructionsStringTitle);
        
        context = getApplicationContext();
        
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

            extStorageDirectory = Environment.getExternalStorageDirectory().toString();
	           
            	// File artdir = new File(basepath + "/Android/data/com.ioiomint./files");
            	File artdir = new File(basepath + "/pixel/pngs");
	            if (!artdir.exists()) { //no directory so let's now start the one time setup
	            	
	            	sdcardImages.setVisibility(View.INVISIBLE); //hide the images as they're not loaded so we can show a splash screen instead
	            	//showToast(getResources().getString(R.string.oneTimeSetupString)); //replaced by direct text on view screen
	            	
	            	new copyFilesAsync().execute();
	            	
	            	
	            }
	            else { //the directory was already there so no need to copy files or do a media re-scan so just continue on
	            	continueOnCreate();
	            }

      //  } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {

           // showToast("Sorry, your device does not have an accessible SD card, this app will not work");//Or use your own method ie: Toast
        //}
	            
        } else  {
        	AlertDialog.Builder alert=new AlertDialog.Builder(this);
 	      	alert.setTitle("No SD Card").setIcon(R.drawable.icon).setMessage("Sorry, your device does not have an accessible SD card, this app needs to copy some images to your SD card and will not work without it.\n\nPlease exit this app and go to Android settings and check that your SD card is mounted and available and then restart this app.\n\nNote for devices that don't have external SD cards, this app will utilize the internal SD card memory but you are most likely seeing this message because your device does have an external SD card slot.").setNeutralButton("OK", null).show();
            //showToast("Sorry, your device does not have an accessible SD card, this app will not work");//Or use your own method ie: Toast
        }
    }
    
    private class copyFilesAsync extends AsyncTask<Void, Integer, Void>{
		 
	     int progress_status;
	      
	     @Override
	  protected void onPreExecute() {
		   // update the UI immediately after the task is executed
		   super.onPreExecute();
		   
		   progress = new ProgressDialog(MainActivity.this);
	       //progress.setMax(selectedFileTotalFrames);
	       progress.setTitle("Initial Setup");
	       progress.setMessage("Copying images to your local storage....");
	       progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	       progress.show();
	       progress_status = 0;
	    
	  }
	      
	  @Override
	  protected Void doInBackground(Void... params) {
		  	
			File artdir = new File(basepath + "/pixel/pngs");
			artdir.mkdirs();			
			SystemClock.sleep(100);
		  	copyArt(); //copy the .png and .gif files (mainly png) because we want to decode first
	
			//had to add this delay , otherwise especially on older phones, all the images don't load the first time
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
		    
	   return null;
	  }
	  
	 /* private void copyNewAnimations() {
		    copyDecodedThread("zaquarium");
		    progress_status ++;
		    publishProgress(progress_status);
		    
		    copyDecodedThread("zarcade");
		    progress_status ++;
		    publishProgress(progress_status);
	  }*/
	  
	  @Override
	  protected void onProgressUpdate(Integer... values) {
	   super.onProgressUpdate(values);
	    
	  // progressBar.setProgress(values[0]);
	  // firstTimeSetupCounter_.setText(values[0]+"%");
	   progress.incrementProgressBy(1);
	  // firstTimeSetupCounter_.setText("0%");
	    
	  }
	   
	  @Override
	  protected void onPostExecute(Void result) {
	   super.onPostExecute(result);
	   progress.dismiss(); //we're done so now hide the progress update box
	   continueOnCreate();
	  }
	  
	
	  
		@SuppressLint("NewApi")
		private void copyArt() {
	    	
	    	AssetManager assetManager = getResources().getAssets();
	        String[] files = null;
	        try {
	            files = assetManager.list("pngs");
	        } catch (Exception e) {
	            Log.e("read clipart ERROR", e.toString());
	            e.printStackTrace();
	        }
	        
	        //let's get the total numbers of files here and set the progress bar
	        progress.setMax(files.length);
	        
	        for(int i=0; i<files.length; i++) {
	            InputStream in = null;
	            OutputStream out = null;
	            try {
	              in = assetManager.open("pngs/" + files[i]);
	              out = new FileOutputStream(basepath + "/pixel/pngs/" + files[i]);
	              copyFile(in, out);
	              in.close();
	              in = null;
	              out.flush();
	              out.close();
	              out = null;    
	            
	           progress_status ++;
	  		   publishProgress(progress_status);  
	  		   
	           MediaScannerConnection.scanFile(context,  //here is where we register the newly copied file to the android media content DB via forcing a media scan
		                        new String[] { basepath + "/pixel/pngs/" + files[i] }, null,
		                        new MediaScannerConnection.OnScanCompletedListener() {
		                    public void onScanCompleted(String path, Uri uri) {
		                        Log.i("ExternalStorage", "Scanned " + path + ":");
		                        Log.i("ExternalStorage", "-> uri=" + uri);
		                        
		                    }
		          });
	           
	           try {  //had to add this delay here otherwise all the images don't load on the first launch, not sure why
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	           
	           
	            } catch(Exception e) {
	                Log.e("copy clipart ERROR", e.toString());
	                e.printStackTrace();
	            }       
	        }
	    }
		
		
} //end async task
    
    
    private void MediaScanCompleted() {
         
    	continueOnCreate();
    }
    
    private void continueOnCreate() {
    
	     firstTimeSetupCounter_.setVisibility(View.GONE);
    	 sdcardImages.setVisibility(View.VISIBLE);
    	 setupViews();
         setProgressBarIndeterminateVisibility(true); 
         loadImages();
    }
    
    private void copyArt() {
    	
    	AssetManager assetManager = getResources().getAssets();
        String[] files = null;
        try {
            files = assetManager.list("pngs");
        } catch (Exception e) {
            Log.e("read clipart ERROR", e.toString());
            e.printStackTrace();
        }
        for(int i=0; i<files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
              in = assetManager.open("pngs/" + files[i]);
              //out = new FileOutputStream(basepath + "/Android/data/com.ioiomint.pixelart/files/" + files[i]);
              out = new FileOutputStream(basepath + "/pixel/pngs/" + files[i]);
              copyFile(in, out);
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;    
            
             
            MediaScannerConnection.scanFile(context,  //here is where we register the newly copied file to the android media content DB via forcing a media scan
	                        new String[] { basepath + "/pixel/pngs/" + files[i] }, null,
	                        new MediaScannerConnection.OnScanCompletedListener() {
	                    public void onScanCompleted(String path, Uri uri) {
	                        Log.i("ExternalStorage", "Scanned " + path + ":");
	                        Log.i("ExternalStorage", "-> uri=" + uri);
	                        
	                    }
	          });
           
            } catch(Exception e) {
                Log.e("copy clipart ERROR", e.toString());
                e.printStackTrace();
            }       
        }
        
    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }

    
    /**
     * Free up bitmap related resources.
     */
    protected void onDestroy() {
        super.onDestroy();
        final GridView grid = sdcardImages;
        final int count = grid.getChildCount();
        ImageView v = null;
        for (int i = 0; i < count; i++) {
            v = (ImageView) grid.getChildAt(i);
            ((BitmapDrawable) v.getDrawable()).setCallback(null);
        }
        
        connectTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
        imagedisplaydurationTimer.cancel();
 		pausebetweenimagesdurationTimer.cancel();
    }
    /**
     * Setup the grid view.
     */
    private void setupViews() {
        //sdcardImages = (GridView) findViewById(R.id.sdcard);
        sdcardImages.setClipToPadding(false);
       // sdcardImages.setNumColumns(display.getWidth()/95);
        sdcardImages.setNumColumns(display.getWidth()/130);
        sdcardImages.setOnItemClickListener(MainActivity.this);
        sdcardImages.setOnItemLongClickListener(MainActivity.this);
        
      //  sdcardImages.setOnClickListener((OnClickListener) MainActivity.this);
        sdcardImages.setOnTouchListener(gestureListener);
        
        imageAdapter = new ImageAdapter(getApplicationContext()); 
        sdcardImages.setAdapter(imageAdapter);
    }
    /**
     * Load images.
     */
    private void loadImages() {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
        	//new LoadImagesfromSDCard.set
        	//static Test t = new Test();
        	//public static void main(String[] args){
        	//t.Set(3);
        	
            new LoadImagesFromSDCard().execute();
        } else {
            final LoadedImage[] photos = (LoadedImage[]) data;
            if (photos.length == 0) {
                new LoadImagesFromSDCard().execute();
            }
            for (LoadedImage photo : photos) {
                addImage(photo);
            }
        }
    }
    /**
     * Add image(s) to the grid view adapter.
     * 
     * @param value Array of LoadedImages references
     */
    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            imageAdapter.addPhoto(image);
            imageAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * Save bitmap images into a list and return that list. 
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final GridView grid = sdcardImages;
        final int count = grid.getChildCount();
        final LoadedImage[] list = new LoadedImage[count];

        for (int i = 0; i < count; i++) {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new LoadedImage(((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }
    
    /**
     * Async task for loading the images from the SD card. 
     * 
     * @author Mihai Fonoage
     *
     */
    class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {
        
        /**
         * Load images from SD Card in the background, and display each image on the screen. 
         *  
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @SuppressLint("NewApi")
		@Override
        protected Object doInBackground(Object... params) {
            //setProgressBarIndeterminateVisibility(true); 
            Bitmap bitmap = null;
            Bitmap newBitmap = null;
            Uri uri = null;  
            
            String[] projection = {MediaStore.Images.Thumbnails._ID};
            if (scanAllPics == true) {
            	// Set up an array of the Thumbnail Image ID column we want
	            // Create the cursor pointing to the SDCard
	            cursor = managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                    projection, // Which columns to return
	                    null,       // Return all rows
	                    null,       
	                    null); 
            }
           else {
                 cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                 projection, 
                 MediaStore.Images.Media.DATA + " like ? ",
                 new String[] {"%pngs%"},  
                 null);
            }	
            
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
            size = cursor.getCount();
          //  showToast("numbr of pics: " + size);
            // If size is 0, there are no images on the SD Card.
            if (size == 0) {
                showToast("No images were found");//No Images available, post some message to the user
            }
            int imageID = 0;
            
            
            showPleaseWait( getString(R.string.loadingImagesPlsWait));
            //pDialog = ProgressDialog.show(MainActivity.this,getString(R.string.loadingImagesPlsWaitTitle), getString(R.string.loadingImagesPlsWait), true);
			//pDialog.setCancelable(true);
            
            
            for (int i = 0; i < size; i++) {
                cursor.moveToPosition(i);
                imageID = cursor.getInt(columnIndex);
                uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID);
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    if (bitmap != null) {
                        newBitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
                        bitmap.recycle();
                        if (newBitmap != null) {
                            publishProgress(new LoadedImage(newBitmap));
                         
                        }
                    }
                } catch (IOException e) {
                    //Error fetching image, try to recover
                }
            }
          //  cursor.close(); //this was causing crashing
            return null;
        }
        /**
         * Add a new LoadedImage in the images grid.
         *
         * @param value The image.
         */
        @Override
        public void onProgressUpdate(LoadedImage... value) {
            addImage(value);
        }
        /**
         * Set the visibility of the progress bar to false.
         * 
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Object result) {
            setProgressBarIndeterminateVisibility(false);
            pDialog.dismiss();
            showToast(getString(R.string.slideShowStartInstructions));
        }
    }

    /**
     * Adapter for our image files. 
     * 
     * @author Mihai Fonoage
     *
     */
    class ImageAdapter extends BaseAdapter {

        private Context mContext; 
        private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();

        public ImageAdapter(Context context) { 
            mContext = context; 
        } 

        public void addPhoto(LoadedImage photo) { 
            photos.add(photo); 
        } 

        public int getCount() { 
            return photos.size(); 
        } 

        public Object getItem(int position) { 
            return photos.get(position); 
        } 

        public long getItemId(int position) { 
            return position; 
        } 

        public View getView(int position, View convertView, ViewGroup parent) { 
            final ImageView imageView; 
            if (convertView == null) { 
                imageView = new ImageView(mContext); 
            } else { 
                imageView = (ImageView) convertView; 
            } 
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setImageBitmap(photos.get(position).getBitmap());
            return imageView; 
        } 
    }

    /**
     * A LoadedImage contains the Bitmap loaded for the image.
     */
    private static class LoadedImage {
        Bitmap mBitmap;

        LoadedImage(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }
    
    public void loadImage() {
  	

  		int y = 0;
  		for (int i = 0; i < frame_.length; i++) {
  			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
  			y = y + 2;
  		}
  		
  		//we're done with the images so let's recycle them to save memory
	    canvasBitmap.recycle();
	    originalImage.recycle(); 
	    
	    if ( resizedFlag == 1) {
	    	resizedBitmap.recycle(); //only there if we had to resize an image
	    }
	   		

  	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.mainmenu, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
       
		
      if (item.getItemId() == R.id.menu_instructions) {
 	    	AlertDialog.Builder alert=new AlertDialog.Builder(this);
 	      	alert.setTitle(setupInstructionsStringTitle).setIcon(R.drawable.icon).setMessage(setupInstructionsString).setNeutralButton(OKText, null).show();
 	   }
    	
      if (item.getItemId() == R.id.menu_about) {
		  
		    AlertDialog.Builder alert=new AlertDialog.Builder(this);
	      	alert.setTitle(getString(R.string.menu_about_title)).setIcon(R.drawable.icon).setMessage(getString(R.string.menu_about_summary) + "\n\n" + getString(R.string.versionString) + " " + app_ver + "\n"
	      			+ getString(R.string.FirmwareVersionString) + " " + pixelFirmware + "\n"
	      			+ getString(R.string.HardwareVersionString) + " " + pixelHardwareID + "\n"
	      			+ getString(R.string.BootloaderVersionString) + " " + pixelBootloader + "\n"
	      			+ getString(R.string.LibraryVersionString) + " " + IOIOLibVersion).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
	   }
    	
    	if (item.getItemId() == R.id.menu_prefs)
       {
    		
    		imagedisplaydurationTimer.cancel(); 
     		pausebetweenimagesdurationTimer.cancel();
    	 
    		
    		Intent intent = new Intent()
       				.setClass(this,
       				com.ioiomint.ledalbum.preferences.class);   
    				this.startActivityForResult(intent, 0);
       }
    	
    	/*if (item.getItemId() == R.id.menu_rescan)  //this crashes on android 4.4 so removed it
        {
     		
    		imagedisplaydurationTimer.cancel(); 
     		pausebetweenimagesdurationTimer.cancel();
    	 
    		
    		Intent intent = new Intent()
        				.setClass(this,
        				com.ioiomint.ledalbum.rescan.class);   
     				this.startActivityForResult(intent, 1);
        }   	*/
    	
       return true;
    }
    
    


@Override
    public void onActivityResult(int reqCode, int resCode, Intent data) //we'll go into a reset after this
    {
    	super.onActivityResult(reqCode, resCode, data);    	
    	setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
    	
    	//if (reqCode == 0 || reqCode == 1) //then we came back from the preferences menu so re-load all images from the sd card, 1 is a re-scan
    	if (reqCode == 1)
    	{
    		imagedisplaydurationTimer.cancel(); //we may have been running a slideshow so kill it
    	    pausebetweenimagesdurationTimer.cancel();
    		setupViews();
    	    setProgressBarIndeterminateVisibility(true); 
    	    loadImages();      
        }
    } 
    
    private void setPreferences() //here is where we read the shared preferences into variables
    {
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);     
    
     scanAllPics = prefs.getBoolean("pref_scanAll", false);
     slideShowMode = prefs.getBoolean("pref_slideshowMode", false);
     noSleep = prefs.getBoolean("pref_noSleep", false);     
     debug_ = prefs.getBoolean("pref_debugMode", false);
  //   dimDuringSlideShow = prefs.getBoolean("pref_dimDuringSlideShow", true);
     
     imageDisplayDuration = Integer.valueOf(prefs.getString(   
  	        resources.getString(R.string.pref_imageDisplayDuration),
  	        resources.getString(R.string.imageDisplayDurationDefault)));   
     
     fps = 1000.f / imageDisplayDuration*1000;
    // fps = 1000/5000;
    
     
     pauseBetweenImagesDuration = Integer.valueOf(prefs.getString(   
  	        resources.getString(R.string.pref_pauseBetweenImagesDuration),
  	        resources.getString(R.string.pauseBetweenImagesDurationDefault)));  
     
     matrix_model = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
    	        resources.getString(R.string.selected_matrix),
    	        resources.getString(R.string.matrix_default_value))); 
     
     if (matrix_model == 0 || matrix_model == 1) {
    	 currentResolution = 16;
     }
     else
     {
    	 currentResolution = 32;
     }
     
     
     switch (matrix_model) {  //get this from the preferences
     case 0:
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;
    	 BitmapInputStream = getResources().openRawResource(R.raw.selectpic);
    	 break;
     case 1:
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
    	 BitmapInputStream = getResources().openRawResource(R.raw.selectpic);
    	 break;
     case 2:
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1   
    	 BitmapInputStream = getResources().openRawResource(R.raw.selectpic32);
    	 break;
     case 3:
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2
    	 BitmapInputStream = getResources().openRawResource(R.raw.selectpic32);
    	 break;
     default:	    		 
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default
    	 BitmapInputStream = getResources().openRawResource(R.raw.selectpic32);
     }
         
     frame_ = new short [KIND.width * KIND.height];
	 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
	 loadRGB565(); //this function loads a raw RGB565 image to the matrix
   		
   		//here we should check if the timer is already running and cancel it if it is before instantiating a new one
   		
   		imagedisplaydurationTimer = new ImageDisplayDurationTimer(imageDisplayDuration*1000,1000); //how long the image should display
 		pausebetweenimagesdurationTimer = new PauseBetweenImagesDurationTimer(pauseBetweenImagesDuration*1000,1000); //how long to show a blank screen before showing the next image
 		slideShowRunning = 0;
 
     
     
 }
      
    
   private void loadRGB565() {
	   
		try {
   			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
   																				// the
   																				// input
   																				// stream
   																				// into
   																				// a
   																				// byte
   																				// array
   			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
   		} catch (IOException e) {
   			e.printStackTrace();
   		}

   		int y = 0;
   		for (int i = 0; i < frame_.length; i++) {
   			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
   			y = y + 2;
   		}
	   
   }
	
    
    public class ConnectTimer extends CountDownTimer
	{

		public ConnectTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				if (deviceFound == 0) {
					showNotFound (); 					
				}
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
    
    public class MediaScanTimer extends CountDownTimer
   	{

   		public MediaScanTimer(long startTime, long interval)
   			{
   				super(startTime, interval);
   			}

   		@Override
   		public void onFinish()
   			{
   				
   				MediaScanCompleted();
   				countdownCounter = countdownDuration; //reset the counter
   			}

   		@Override
   		public void onTick(long millisUntilFinished)  {
   			setfirstTimeSetupCounter(Integer.toString(countdownCounter));
   			countdownCounter--;
   		}
   	}
	
	private void showNotFound() {	
		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
}
      
	
    
    class IOIOThread extends BaseIOIOLooper {
  		//private ioio.lib.api.RgbLedMatrix matrix_;

  		@Override
  		protected void setup() throws ConnectionLostException {
  			matrix_ = ioio_.openRgbLedMatrix(KIND);
  			deviceFound = 1; //if we went here, then we are connected over bluetooth or USB
  			connectTimer.cancel(); //we can stop this since it was found
  			
  			matrix_.frame(frame_);  //write select pic to the matrix
  			
  			if (debug_ == true) {  			
	  			showToast("Bluetooth Connected");
	  			//showToast("App Started Flag: " + appAlreadyStarted);
  			}
  			
  			if (appAlreadyStarted == 1) {  //this means we were already running and had a IOIO disconnect so show let's show what was in the matrix
  				//matrix_.frame(frame_);  //this was causing a crash
  				WriteImagetoMatrix();
  			}
  			
  		//**** let's get IOIO version info for the About Screen ****
  			pixelFirmware = ioio_.getImplVersion(v.APP_FIRMWARE_VER);
  			pixelBootloader = ioio_.getImplVersion(v.BOOTLOADER_VER);
  			pixelHardwareID = ioio_.getImplVersion(v.HARDWARE_VER);
  			IOIOLibVersion = ioio_.getImplVersion(v.IOIOLIB_VER);
  			//**********************************************************
  			
  			appAlreadyStarted = 1; 
  		}

  	//	@Override
  		//public void loop() throws ConnectionLostException {
  		
  			//matrix_.frame(frame_); //writes whatever is in bitmap raw 565 file buffer to the RGB LCD
  			//}	
  		
  		@Override
		public void disconnected() {
			Log.i(LOG_TAG, "IOIO disconnected");
			
			
			if (debug_ == true) {  			
	  			showToast("Bluetooth Disconnected");
  			}
		}

		@Override
		public void incompatible() {  //if the wrong firmware is there
			//AlertDialog.Builder alert=new AlertDialog.Builder(context); //causing a crash
			//alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
			showToast("Incompatbile firmware!");
			showToast("This app won't work until you flash the IOIO with the correct firmware!");
			showToast("You can use the IOIO Manager Android app to flash the correct firmware");
			Log.e(LOG_TAG, "Incompatbile firmware!");
			}
  		}

  	@Override
  	protected IOIOLooper createIOIOLooper() {
  		return new IOIOThread();
  	}
    
    private void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
                toast.show();
			}
		});
	}  
    
    private void showToastShort(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
                toast.show();
			}
		});
	}  
    
    private void setfirstTimeSetupCounter(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				firstTimeSetupCounter_.setText(str);
			}
		});
	}

    /**
     * Adapter for our image files.
     */
    
    public  boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {  
    	 if (deviceFound == 1) {   
    	    	if (slideShowRunning == 0) { 
    	    	
    		    	// Get the data location of the image
    		        String[] projection = {MediaStore.Images.Media.DATA};
    		        
    		        if (scanAllPics == true) {
    		            
    		            cursor = managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    		                projection, // Which columns to return
    		                null,       // Return all rows
    		                null,
    		                null);
    		        }
    		        else {
    		        	cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    		                projection, 
    		                MediaStore.Images.Media.DATA + " like ? ",
    		                new String[] {"%pngs%"},  
    		                null);
    		        } 
    		        
    		       // showToast("on click");
    		        
    		        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    		        cursor.moveToPosition(position);
    		        // Get image filename
    		        imagePath = cursor.getString(columnIndex);
    		        System.gc();
    		      
    		        
    		        if (pixelHardwareID.substring(0,4).equals("PIXL")) {  
    		        	try {
							matrix_.interactive();
							matrix_.writeFile(fps);
	    		        	WriteImagetoMatrix();
	    		        	matrix_.playFile();
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					
    		        }  
    		        else {
    		        	 try {
		    					WriteImagetoMatrix();
		    				} catch (ConnectionLostException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
    		        }
    		        
    		        return true;
    	    	}
    	    	else {
    	    		showToast(getString(R.string.stopSlideShowMessage)); //tell the user we're still in slideshow mode and to right swipe to stop the slideshow
    	    		return true;  
    	    	}
    	      }
    	      else {
    	    	   showToast("PIXEL was not found, did you Bluetooth pair to PIXEL? Use Code 0000 for PIXEL V2 and 4545 for PIXEL V1.");
    			   return true;
    	      }
   	}
    
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {        
        
      if (deviceFound == 1) {   
    	  
    	  if (pixelHardwareID.substring(0,4).equals("PIXL")) {   //need to get things back into interactive mode
	        	try {
					matrix_.interactive();
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        } 
    	  
    	if (slideShowRunning == 0) { 
    	
	    	// Get the data location of the image
	        String[] projection = {MediaStore.Images.Media.DATA};
	        
	        if (scanAllPics == true) {
	            
	            cursor = managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                projection, // Which columns to return
	                null,       // Return all rows
	                null,
	                null);
	        }
	        else {
	        	cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                projection, 
	                MediaStore.Images.Media.DATA + " like ? ",
	                new String[] {"%pngs%"},  
	                null);
	        } 
	        
	       // showToast("on click");
	        
	        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToPosition(position);
	        // Get image filename
	        imagePath = cursor.getString(columnIndex);
	        System.gc();
	         
	        //***** was calling the IOIO piece from a new intent but removed this *******
	        // Intent intent = new Intent(getApplicationContext(), ViewImage.class);
	        // intent.putExtra("filename", imagePath);
	        // startActivity(intent);
	        
	        try {
				WriteImagetoMatrix();
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else {
    		showToast(getString(R.string.stopSlideShowMessage)); //tell the user we're still in slideshow mode and to right swipe to stop the slideshow
    	}
      }
      else {
    	   showToast("PIXEL was not found, did you Bluetooth pair to PIXEL? Use Code 0000 for PIXEL V2 and 4545 for PIXEL V1.");
		  // return true;
      }
       
    }
    
    private void WriteImagetoMatrix() throws ConnectionLostException {  //here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
    	
    	     originalImage = BitmapFactory.decodeFile(imagePath);   		 
    		 width_original = originalImage.getWidth();
    		 height_original = originalImage.getHeight();
    		 
    		 if (width_original != KIND.width || height_original != KIND.height) {
    			 resizedFlag = 1;
    			 //the iamge is not the right dimensions, so we need to re-size
    			 scaleWidth = ((float) KIND.width) / width_original;
 	   		 	 scaleHeight = ((float) KIND.height) / height_original;
 	   		 	 
	 	   		 // create matrix for the manipulation
	 	   		 matrix2 = new Matrix();
	 	   		 // resize the bit map
	 	   		 matrix2.postScale(scaleWidth, scaleHeight);
	 	   		 resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width_original, height_original, matrix2, true);
	 	   		 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
	 	   		 Canvas canvas = new Canvas(canvasBitmap);
	 	   		 canvas.drawRGB(0,0,0); //a black background
	 	   	   	 canvas.drawBitmap(resizedBitmap, 0, 0, null);
	 	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
	 	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
	 	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
    		 }
    		 else {
    			// then the image is already the right dimensions, no need to waste resources resizing
    			 resizedFlag = 0;
    			 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
    	   		 Canvas canvas = new Canvas(canvasBitmap);
	 	   	   	 canvas.drawBitmap(originalImage, 0, 0, null);
	 	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
	 	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
	 	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
    		 }	   		
    		 
			loadImage();  
			matrix_.frame(frame_);  //write to the matrix   
    }
    
    private class createSlideShowAsync extends AsyncTask<Void, Integer, Void>{
		 
	     int progress_status;
	      
	     @Override
	  protected void onPreExecute() {
		   // update the UI immediately after the task is executed
		   super.onPreExecute();
		   showToast("Creating Slide Show");
	  }
	      
	  @Override
	  protected Void doInBackground(Void... params) {
		  	
		  try {
			createSlideShow();
		} catch (ConnectionLostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
			//had to add this delay , otherwise especially on older phones, all the images don't load the first time
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	   return null;
	  }
	
	  
	  @Override
	  protected void onProgressUpdate(Integer... values) {
	   super.onProgressUpdate(values);
	   //progress.incrementProgressBy(1);
	 }
	   
	  @Override
	  protected void onPostExecute(Void result) {
	   super.onPostExecute(result);
	   //progress.dismiss(); //we're done so now hide the progress update box
	   //now let's put PIXEL in local playback mode
	   try {
		matrix_.interactive();
		//matrix_.writeFile(imageDisplayDuration);
		matrix_.writeFile(fps);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   writePixelAsync loadApplication = new writePixelAsync();
		   loadApplication.execute();
		}
} //end async task
    
    private void createSlideShow() throws ConnectionLostException {
    	//we'll need to 
    	
    	// Get the data location of the image
        String[] projection = {MediaStore.Images.Media.DATA};
        
	       
        	cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, 
                MediaStore.Images.Media.DATA + " like ? ",
                new String[] {"%pngs%"},  
                null);
	       
    	
	    	File decodedFileDir = new File(decodedDirPath); //pixel/decoded  ,create it if not there
			if(decodedFileDir.exists() == false) {
				decodedFileDir.mkdirs();
			}
	        	
    		File decodedFile = new File(decodedDirPath + "/slideshow.rgb565"); //decoded/slideshow.rgb565 , if it's already there, then we need to delete
    		if(decodedFile.exists()) {
    			decodedFile.delete(); //delete the file as we're going to re-create it
    		}
    		
    		File decodedFileTxt = new File(decodedDirPath + "/slideshow.txt"); //pixel/decoded/slideshow.txt  ,delete it if there
			if(decodedFileTxt.exists()) {
				decodedFileTxt.delete(); //delete the file as we're going to re-create it
			}
			
			String filetag = String.valueOf(size) + "," + String.valueOf(imageDisplayDuration*1000) + "," + String.valueOf(currentResolution);  //our format is number of frames,delay 72,60,32 equals 72 frames, 60 ms time delay, 32 resolution   resolution is 16 for 16x32 or 32 for 32x32 led matrix, we need this in case the user changes the resolution in the app, then we'd need to catch this mismatch and re-decode
	        
	        String exStorageState = Environment.getExternalStorageState();
	     	if (Environment.MEDIA_MOUNTED.equals(exStorageState)){
	     		try {
	     			
	     		   File myFile = new File(decodedDirPath + "/" + "slideshow.txt");  //decoded/slideshow.txt					       
	     		   myFile.createNewFile();
		           FileOutputStream fOut = null;
				   fOut = new FileOutputStream(myFile);
		           OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				   myOutWriter.append(filetag);
				   myOutWriter.close();
				   fOut.close();
	     			
	     		} catch (IOException e) {
	     			e.printStackTrace();
	     			//Toast.makeText(this, "Couldn't save", Toast.LENGTH_SHORT);
	     		}
	     	}	
		
    	
    		slideshowPosition = 0;
	        
	        for (i=0;i<size;i++) {
		        
		        cursor.moveToPosition(slideshowPosition);
		        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); //this is crashing things
		        //showToast("colum index: " + getString(columnIndex)); //this does not work for some reason and causes slideshow to not go
		        slideshowPosition++; //increment it so we can play the next one
		        
		        // Get image filename
		        imagePath = cursor.getString(columnIndex);
		      //  System.gc();
		       // showToastShort("path: " + imagePath);
		        WriteImagetoMatrix();	        
		      
		        try {
					appendWrite(BitmapBytes, decodedDirPath + "/" + "slideshow" + ".rgb565");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
    }
    
    public class writePixelAsync extends AsyncTask<Void, Integer, Void>{
		
		 int progress_status;
	      
		  @Override
		  protected void onPreExecute() {
	      super.onPreExecute();
	    
	     progress = new ProgressDialog(MainActivity.this);
		        progress.setMax(size);
		        progress.setTitle("Writing Slide Show to PIXEL");
		        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		        progress.show();
		  }
	      
	  @Override
	  protected Void doInBackground(Void... params) {
			
	  int count;
	  for (count=0;count<size-1;count++) {  //size is the number of slides
				 
				
				  File file = new File(decodedDirPath + "/" + "slideshow" + ".rgb565"); //this is one big file now, no longer separate files
				  
					RandomAccessFile raf = null;
				  
					//let's setup the seeker object
					try {
						raf = new RandomAccessFile(file, "r");
						
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}  // "r" means open the file for reading
					
					if (x == size) { // Manju - Reached End of the file.
		   				x = 0;
		   				try {
							raf.seek(0); //move pointer back to the beginning of the file
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
		   			}
					
					 switch (selectedFileResolution) { //16x32 matrix = 1024k frame size, 32x32 matrix = 2048k frame size
			            case 16: frame_length = 1048;
			                     break;
			            case 32: frame_length = 2048;
			                     break;
			            case 64: frame_length = 4096;
			                     break;
			            default: frame_length = 2048;
			                     break;
			          }
					 
					//now let's see forward to a part of the file
						try {
							raf.seek(x*frame_length);
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} 
						//Log.d("PixelAnimations","from sd card write, x is: " + x);
						x++;
						
						if (frame_length > Integer.MAX_VALUE) {
			   			    try {
								throw new IOException("The file is too big");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			   			}
						
						// Create the byte array to hold the data
			   			//byte[] bytes = new byte[(int)length];
			   			BitmapBytes = new byte[(int)frame_length];
			   			 
			   			// Read in the bytes
			   			int offset = 0;
			   			int numRead = 0;
			   			try {
							while (offset < BitmapBytes.length && (numRead=raf.read(BitmapBytes, offset, BitmapBytes.length-offset)) >= 0) {
							    offset += numRead;
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			   			 
			   			// Ensure all the bytes have been read in
			   			if (offset < BitmapBytes.length) {
			   			    try {
								throw new IOException("The file was not completely read: "+file.getName());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			   			}
			   			 
			   			// Close the input stream, all file contents are in the bytes variable
			   			try {
			   				raf.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}	
							
							//now that we have the byte array loaded, load it into the frame short array
							
						int y = 0;
						for (int i = 0; i < frame_.length; i++) {
							frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
							y = y + 2;
						}
					
						//need to add something in here if the transfer got interruped, then go back to interactive mode and start over
						//downloadCounter++;
					   	try {
					   	 Log.i("PixelAnimations ","Starting-->"+ count + " " + String.valueOf(size-1));
					   		matrix_.frame(frame_);
					   		progress_status++;
						    publishProgress(progress_status);
					   	
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			  }  // end for
		  
			//progress.incrementProgressBy(1);
			//copyDecodedThread("0rain");
			
		    
	   return null;
	  }
	  
	  @Override
	  protected void onProgressUpdate(Integer... values) {
	   super.onProgressUpdate(values);
	   progress.incrementProgressBy(1);
	  }
	   
	  @Override
	  protected void onPostExecute(Void result) {
		  progress.dismiss();
	
	 
	   try {
		matrix_.playFile();
	} catch (ConnectionLostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  super.onPostExecute(result);
}
	
}
    
    
    
    public void appendWrite(byte[] data, String filename) throws IOException {
		 FileOutputStream fos = new FileOutputStream(filename, true);  //true means append, false is over-write
	     fos.write(data);
	     fos.close();
	}
    
    
    @SuppressLint("ParserError")
	private void SlideShow() throws ConnectionLostException {
	      	        
    	if (deviceFound == 1) {   
    	
	    	if (pixelHardwareID.substring(0,4).equals("PIXL")) {  //download mode cuz it's a PIXEL V2 unit
	    	   			
	    			new createSlideShowAsync().execute();  //start the async task
	    	}
	    	
	    	else { //it's a PIXEL V1 unit
	    	
	    		if (slideshowPosition == size) { //let's make sure we haven't reached the end
		        	slideshowPosition = 0;
		        }
		        
		        cursor.moveToPosition(slideshowPosition);
		        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); //this is crashing things
		        //showToast("colum index: " + getString(columnIndex)); //this does not work for some reason and causes slideshow to not go
		        slideshowPosition++; //increment it so we can play the next one
		        
		        // Get image filename
		        imagePath = cursor.getString(columnIndex);
		        System.gc();
		       // showToastShort("path: " + imagePath);
		        WriteImagetoMatrix();	        
		        imagedisplaydurationTimer.start(); //the image will stay on for as long as this timer;
	    	}    
    	}
    	else {
    		showToast("PIXEL was not found, did you Bluetooth pair to PIXEL?");
    	}
    }
    
    private void stopSlideShow() { //stop the slideshow
    	
    	if (pixelHardwareID.substring(0,4).equals("PIXL")) {  
    		try {
				matrix_.interactive(); //go out of local playback mode
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else {
	    	imagedisplaydurationTimer.cancel();
	 		pausebetweenimagesdurationTimer.cancel();
    	}
    	
    }
    
    private void showPleaseWait(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				//pDialog = ProgressDialog.show(breath.this,analyzingText, justAmomentText, true);
				pDialog = ProgressDialog.show(MainActivity.this,getString(R.string.loadingImagesPlsWaitTitle), str, true);
				//pDialog.setCancelable(true);  //we don't need this one
			}
		});
	}
    
    private void screenOn() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();  //turn the screen back on
				lp.screenBrightness = 10 / 100.0f;  
				//lp.screenBrightness = 100 / 100.0f;  
				getWindow().setAttributes(lp);
			}
		});
	}
	
	private void dimScreen() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();  //turn the screen back on
				lp.screenBrightness = 1 / 100.0f;  
				getWindow().setAttributes(lp);
			}
		});
	}
    
    private void clearMatrixImage() throws ConnectionLostException {
    	//let's claear the image
    	 BitmapInputStream = getResources().openRawResource(R.raw.blank); //load a blank image to clear it
    	 loadRGB565();    	
    	 matrix_.frame(frame_); 
    	//then let's start another timer to load the next image
    	pausebetweenimagesdurationTimer.start();  //how long the rgb matrix should be of before showing the next image
    }
    
   
	//@Override
	//protected void onStop() {
		//super.onStop();
	//}
	
	//@Override
	//protected void onStart() {
		//	super.onStart();
	//}
    
    public class ImageDisplayDurationTimer extends CountDownTimer
	{

		public ImageDisplayDurationTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
			imagedisplaydurationTimer.cancel();//added this back to see if it will stop crashing
			try {
				clearMatrixImage();
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
 
 public class PauseBetweenImagesDurationTimer extends CountDownTimer
	{

		public PauseBetweenImagesDurationTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
			try {
				SlideShow();
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //we've paused long enough, show the next image
			pausebetweenimagesdurationTimer.cancel();
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
    
    
    
    class MyGestureDetector extends SimpleOnGestureListener {       
    	@Override        
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { 
    		
    		try {                
    			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
    				return false;                // right to left swipe
    			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    				//Toast.makeText(MainActivity.this, "Stopping Slide Show", Toast.LENGTH_SHORT).show();
    				showToastShort(getResources().getString(R.string.slideShowStopVerbage)); //stop slideshow verbage
    				stopSlideShow();
    				slideShowRunning = 0;
    				//if (dimDuringSlideShow == true) {
    				//	screenOn();
    				//}
    				//sdcardImages.setOnItemClickListener(MainActivity.this); //add the onclick listener back
    				//add onclick listener back?
    				
    				
    				}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { 
    					
    					if (slideShowRunning == 0) {    						
    						slideShowRunning = 1;
	    					//Toast.makeText(MainActivity.this, "Starting Slide Show...", Toast.LENGTH_SHORT).show();
	    					showToastShort(getResources().getString(R.string.slideShowStartVerbage));
	    					showToast(getResources().getString(R.string.slideShowStopInstructions));
	    					//sdcardImages.setOnClickListener(null); //remove the on click listener
	    					//if (dimDuringSlideShow == true) {
	    					//	dimScreen();
	    					//}
	    					
	    					String[] projection = {MediaStore.Images.Media.DATA}; //maybe move this outside of slideshow since it runs everytime
	    				        
	    				        if (scanAllPics == true) {
	    				            
	    				            cursor = managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	    				                projection, // Which columns to return
	    				                null,       // Return all rows
	    				                null,
	    				                null);
	    				        }
	    				        else {
	    				        	cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	    				                projection, 
	    				                MediaStore.Images.Media.DATA + " like ? ",
	    				                new String[] {"%pngs%"},  
	    				                null);
	    				        } 
	    					
	    					
	    					
	    					SlideShow(); //start or resume the slideshow
	    					
    					}
    					
    					
    					}            } catch (Exception e) {                // nothing
    						
    					}            return false;
    				}
    		}
    
}
    
    
    
    
    




























