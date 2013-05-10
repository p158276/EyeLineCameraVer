package cliu.TutorialOnFaceDetect;

/*
 * TutorialOnFaceDetect
 * 
 * [AUTHOR]: Chunyen Liu
 * [SDK   ]: Android SDK 2.1 and up
 * [NOTE  ]: developer.com tutorial, "Face Detection with Android APIs"
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class TutorialOnFaceDetect extends Activity implements SurfaceHolder.Callback{
	
	private Camera mCamera01;
	private Button mButton01,mButton02,mButton03;
	private ImageView mImageView01;
	private TextView mTextView01;
	private String TAG1 = "HIPPO";
	private SurfaceView mSurfaceView01;
	private SurfaceHolder mSurfaceHolder01;	
	private boolean bIfPreview = false;
	//private String strCapatureFilePath = "/sdcard/camera_snap.png";
	private MyImageView mIV;
	private Bitmap mFaceBitmap;
	public Bitmap draw;
	private int mFaceWidth = 200;
	private int mFaceHeight = 200;
	private static final int MAX_FACES = 10;
	private static String TAG = "TutorialOnFaceDetect";
	private static boolean DEBUG = false;
	public int usetime=0;
	public int[] pixels;
	public int[] lResult;
	public int[] rResult;
	public int placelength=0;
	public int[] im3,iml3;
	public int width,height;
	public double startTime,endTime,totTime;
	public Canvas canvasTemp;
	public Paint p;
    static
    {
    	System.loadLibrary("main");//Load the C code
    }
	protected static final int GUIUPDATE_SETFACE = 999;
	protected static final Bitmap NULL = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);       
		setContentView(R.layout.main);
		if(!checkSDCard())
		{
			mMakeTextToast(getResources().getText(R.string.str_err_nosd).toString(),true);
		}
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		mTextView01 = (TextView)findViewById(R.id.textView1);
		mImageView01 = (ImageView)findViewById(R.id.imageView1);
		
		mSurfaceView01 = (SurfaceView)findViewById(R.id.surfaceView1);
		mSurfaceHolder01 = mSurfaceView01.getHolder();
		mSurfaceHolder01.addCallback(TutorialOnFaceDetect.this);
		
		mSurfaceHolder01.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mButton01 = (Button)findViewById(R.id.button1);
		//mButton02 = (Button)findViewById(R.id.button2);
		mButton03 = (Button)findViewById(R.id.button3);
		mButton01.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(usetime>0)
				{
					mImageView01.setImageBitmap(NULL);
					draw.recycle();
					draw = null;
					usetime=0;
				}
				initCamera();
				
			}
		});
		/*mButton02.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				resetCamera();
				
			}
		});*/
		mButton03.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(checkSDCard())
				{
					takePicture();
				}else
				{
					mTextView01.setText(getResources().getText(R.string.str_err_nosd).toString());
				}
			}
		});		
    }

    public void setFace() {
		canvasTemp =  new  Canvas(draw);
		p = new Paint();
		p.setColor(Color.YELLOW);
		p.setStrokeWidth(3);
		double startfindTime = System.currentTimeMillis();		
    	FaceDetector fd;
    	FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
    	PointF eyescenter = new PointF();
    	float eyesdist = 0.0f;
    	int [] fpx = null;
    	int [] fpy = null;
    	int count = 0;
    	
    	try {
			fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);        
    		count = fd.findFaces(mFaceBitmap, faces);	  		   		
  
    	} catch (Exception e) {
    		Log.e(TAG, "setFace(): " + e.toString());
    		return;
    	}
		double endfindTime = System.currentTimeMillis();
		Log.e(TAG,"Find face time="+(endfindTime-startfindTime)/1000+"sec");  
    	// check if we detect any faces
    	if (count > 0) {
    		fpx = new int[count * 2];
    		fpy = new int[count * 2];
    		int i = 0;
    			try {                 
    				faces[i].getMidPoint(eyescenter);                  
    				eyesdist = faces[i].eyesDistance();  
    				p.setStyle(Paint.Style.STROKE);//set the rectangle in stroke  
    		    	canvasTemp.drawRect(
                            //(int)(eyescenter.x - eyesdist*0.9),
    		    			(int)(eyescenter.x - eyesdist*1.1),
                            //(int)(eyescenter.y - eyesdist*0.6),
    		    			(int)(eyescenter.y - eyesdist*0.6),
    		    			(int)(eyescenter.x + eyesdist*1.1),
                            (int)(eyescenter.y + eyesdist*1.5),
                            p);
    				// set up left eye location
    				fpx[2 * i] = (int)(eyescenter.x - eyesdist / 2);
    				fpy[2 * i] = (int)eyescenter.y;
    				int rightxside = (int)(eyescenter.x - eyesdist / 2);
    				int rightyside = (int)eyescenter.y;
    				width = (int)(eyesdist*0.9);
    				height = (int)eyesdist*3/7;
    				int xinit = (int)(rightxside-eyesdist/2);
    				int yinit = (int)(rightyside-eyesdist/5);  				
    				im3 = new int[width*height];
    				//rResult = new int[width*height];
    				int in =0;
    				Log.e(TAG, "x=" +xinit);
    				Log.e(TAG, "y=" +yinit);
    				Log.e(TAG, "width=" +width);
    				Log.e(TAG, "height=" +height);  		 	    				
    				canvasTemp.drawRect(xinit,yinit,xinit+width,yinit+height, p);
    				p.setColor(Color.GREEN);
    				for(int y = yinit; y < (yinit+height); y++)
    				{	
    					for (int x = xinit; x < (xinit+width); x++)    				
    					{
    						
    						int index = y * mFaceWidth + x;
    						int R = (pixels[index] >> 16) & 0xff;
    		        		int G = (pixels[index] >> 8) & 0xff;
    		        		int B = pixels[index] & 0xff;  
    		        		im3[in++]=R+G+B;
    		        		//if(in < 50)
    		        		//	Log.e(TAG,"im3 = "+"(x,y)="+x+" "+y+"im"+im3[in-1]+" in= "+(in-1));
    		        		//Log.e(TAG, "im3 " +"x,y= "+x+" "+y+"content"+R+" "+G+" "+B);
    		                  
    					}
    				}
    				rResult = new int[width*height];
    				startfindTime = System.currentTimeMillis();
    				rResult = sendToC(height,width,im3);
    				endfindTime = System.currentTimeMillis();
    				Log.e(TAG,"Find reye time="+(endfindTime-startfindTime)/1000+"sec"); 
    				int co =0;
       				for(int y = yinit; y < (yinit+height); y++)
    				{	
    					for (int x = xinit; x < (xinit+width); x++)    				
    					{
    						if(rResult[co]==1)
    						{
        	    				canvasTemp.drawPoint(x, y, p);
    							//canvasTemp.drawPoint(y,x, p);
    							//int index = y * mFaceWidth + x;
    							//int R = 255;
    							//int G = 0;
    							//int B = 0;
    							//pixels[index] = 0xff000000 | (R << 16) | (G << 8) | B;
    						}
    						co++;
    					}
    				}
    				rResult=null;
    				// set up right eye location
    				fpx[2 * i + 1] = (int)(eyescenter.x + eyesdist / 2);
    				fpy[2 * i + 1] = (int)eyescenter.y;
    				int leftxside = (int)(eyescenter.x + eyesdist / 2);
    				int leftyside = (int)eyescenter.y;
    				width = (int)(eyesdist*0.9);//ori setting 4/5
    				height = (int)eyesdist*3/7;
    				iml3 = new int[width*height];
    				in =0;
    				xinit = (int)(leftxside-eyesdist/3);
    				yinit = (int)(leftyside-eyesdist/5);    				
    				//rResult = new int[width*height];
    				Log.e(TAG, "x=" +xinit);
    				Log.e(TAG, "y=" +yinit);
    				Log.e(TAG, "width=" +width);
    				Log.e(TAG, "height=" +height); 
    				p.setColor(Color.YELLOW);
    				p.setStyle(Paint.Style.STROKE);//set the rectangle in stroke       				
    				canvasTemp.drawRect(xinit,yinit,xinit+width,yinit+height, p);
    				p.setColor(Color.GREEN);
    				for (int y = yinit; y < (yinit+height); y++)
    				{	
    					for(int x = xinit; x < (xinit+width); x++)    				
    					{ 					
    						int index = y * mFaceWidth + x;
    						int R = (pixels[index] >> 16) & 0xff;
    		        		int G = (pixels[index] >> 8) & 0xff;
    		        		int B = pixels[index] & 0xff;
    		        		iml3[in++]=R+G+B;
    		        		//Log.e(TAG, "im3 " +"x,y= "+x+" "+y+"content"+R+" "+G+" "+B);
    		                //Log.e(TAG,"iml3 = "+"(x,y)="+x+" "+y+"im"+iml3[in-1]);    
    					}
    				}
    				lResult = new int[width*height];
    				//startTime = System.currentTimeMillis();
    				startfindTime = System.currentTimeMillis();
    				lResult = sendToC(height,width,iml3);
    				endfindTime = System.currentTimeMillis();
    				Log.e(TAG,"Find reye time="+(endfindTime-startfindTime)/1000+"sec"); 
    				co =0;
    				for (int y = yinit; y < (yinit+height); y++)
    				{	
    					for(int x = xinit; x < (xinit+width); x++)    				
    					{ 	
    						if(lResult[co]==1)
    						{	
        	    				canvasTemp.drawPoint(x, y, p);  
    							//int index = y * mFaceWidth + x;
    							//int R = 255;
    							//int G = 0;
    							//int B = 0;
    							//pixels[index] = 0xff000000 | (R << 16) | (G << 8) | B;
    						}
    						co++;
    					}
    				}    		
    				lResult = null;
    				DEBUG=false;
    				if (DEBUG)
    					Log.e(TAG, "setFace(): face " + i + ": confidence = " + faces[i].confidence() 
    							+ ", eyes distance = " + faces[i].eyesDistance()                             
    							+ ", pose = ("+ faces[i].pose(FaceDetector.Face.EULER_X) + ","                            
    							+ faces[i].pose(FaceDetector.Face.EULER_Y) + ","                            
    							+ faces[i].pose(FaceDetector.Face.EULER_Z) + ")"
    							+ "left eye"+fpx[0]+" "+fpy[0]+"right eye"+fpx[1]+" "+fpy[1]
    							+ ", eyes midpoint = (" + eyescenter.x + "," + eyescenter.y +")"); 
    			} catch (Exception e) { 
    				Log.e(TAG, "setFace(): face " + i + ": " + e.toString());
    			}            
    		      
    	}
    	//mIV.setDisplayPoints(fpx, fpy, count * 2, 1);
    } 
    public native int[] sendToC(int a, int b,int[] picture);//sender value to c function 

    //Camera part
	@SuppressLint("NewApi")
	private void initCamera()
	{
		if(!bIfPreview)
		{
			mCamera01 = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
		}
		//Log.i(TAG,"in init");
		Log.i(TAG,"before inside the camera");
		if(mCamera01 != null && !bIfPreview)
		{
			Log.i(TAG,"inside the camera");
			Camera.Parameters parameters = mCamera01.getParameters();
			try{
				parameters.setPictureFormat(PixelFormat.JPEG);
				//parameters.set("orientation", "portrait");
		        //parameters.setRotation(180);
		        //mCamera01.setParameters(parameters);
			}catch(Exception e)
			{
				e.printStackTrace();
				Log.i(TAG,"inside setPictureFormat");			
			}
			Log.i(TAG,"inside the ");
			List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
			int mFrameWidth=1600;
			int mFrameHeight=1200;
			width = mFrameWidth;
			height = mFrameHeight;

			// selecting optimal camera preview size closest to desired size
			double minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
			     if (Math.abs(size.width - width) < minDiff) {
			         mFrameWidth = size.width;
			         mFrameHeight = size.height;
			         minDiff = Math.abs(size.width - width);
			     }
			 }
			//parameters.setPreviewSize(mFrameWidth, mFrameHeight);
			parameters.setPreviewSize(640, 480);
			parameters.setPictureSize(mFrameWidth, mFrameHeight);
			Log.i(TAG,"width = "+mFrameWidth+" Height = "+mFrameHeight);
			mCamera01.setParameters(parameters);
			try {
				mCamera01.setPreviewDisplay(mSurfaceHolder01);
				Camera.CameraInfo camInfo = new Camera.CameraInfo();
				
				Camera.getCameraInfo(0, camInfo);
				//int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
				int result;		
				result = 270;
				mCamera01.setDisplayOrientation(result);
				Log.i(TAG,"rotation "+result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i(TAG,"inside gg camera");
			}
			mCamera01.startPreview();
			bIfPreview = true;
		}
	}
	private void takePicture()
	{
		if(mCamera01 !=null && bIfPreview)
		{
			mCamera01.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}
	private void resetCamera()
	{
		if(mCamera01 !=null && bIfPreview)
		{
			Log.i(TAG,"in reset");
			mCamera01.stopPreview();
			mCamera01.release();
			mCamera01=null;
			bIfPreview = false;
		}
	}
	private ShutterCallback shutterCallback = new ShutterCallback()
	{

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			
		}
		
	};
	private PictureCallback rawCallback = new PictureCallback()
	{

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}	
	};	
	private PictureCallback jpegCallback = new PictureCallback()
	{
		
		@Override
		public void onPictureTaken(byte[] _data, Camera camera) {
			// TODO Auto-generated method stub
			double totalstartTime = System.currentTimeMillis();
			//File myCaptureFile = new File(strCapatureFilePath);
			try
			{
				// load the photo			
		        BitmapFactory.Options opt = new BitmapFactory.Options();
		        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		        Bitmap bm = BitmapFactory.decodeByteArray(_data, 0, _data.length,opt);
		       // Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.camera_snap,opt);
		        //set final size
		        int w = bm.getWidth();
		        int h = bm.getHeight();
		        int destWidth = 1200;
		        int destHeigth = 900;
		        float scaleWidth = ((float) destWidth) / w;
		        float scaleHeight = ((float) destHeigth) / h;
		      //set resize
				startTime = System.currentTimeMillis();
				Matrix mtx = new Matrix();
				mtx.setRotate(90);
		        mtx.postScale(scaleWidth, scaleHeight);
		        mtx.postScale(-1, 1);
		        Bitmap resizedBMP = Bitmap.createBitmap(bm, 0, 0, w, h, mtx, true);
				draw=resizedBMP.copy(Bitmap.Config.ARGB_8888, true);
				mFaceBitmap = resizedBMP.copy(Bitmap.Config.RGB_565, true);
				endTime = System.currentTimeMillis();
				totTime = endTime - startTime;
				Log.e(TAG,"bmp copy time="+totTime/1000+"sec");
				mFaceWidth = mFaceBitmap.getWidth();
				mFaceHeight = mFaceBitmap.getHeight();  
				pixels = new int[mFaceWidth * mFaceHeight];
				draw.getPixels(pixels, 0, mFaceWidth, 0, 0, mFaceWidth, mFaceHeight);
				// perform face detection in setFace() in a background thread	
				canvasTemp =  new  Canvas(draw);
				//doLengthyCalc();
				startTime = System.currentTimeMillis();
				setFace();
				endTime = System.currentTimeMillis();
				totTime = endTime - startTime;
				Log.e(TAG,"total time="+totTime/1000+"sec"); 				
				Log.e(TAG,"DRAW?");
				//draw.setPixels(pixels, 0, mFaceWidth, 0, 0, mFaceWidth, mFaceHeight);
				mImageView01 = (ImageView)findViewById(R.id.imageView1); 
				mImageView01.setImageBitmap(draw);
		        Log.e(TAG,"doneiview?");
		        mFaceBitmap.recycle();
		        mFaceBitmap=null;
				resetCamera();
				usetime++;
		        ////For SD saving
				//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
				//bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
				bm.recycle();
				resizedBMP = null;
				//bm.recycle();
				bm = null;
				//bos.flush();
				//bos.close();
				//mImageView01.setImageBitmap(bm);
			}catch(Exception e)
			{
				Log.e(TAG,e.getMessage());
				Log.i(TAG,"in jpeg call");
			}
			double totalendTime = System.currentTimeMillis();
			Log.e(TAG,"total execute time="+(totalendTime-totalstartTime)/1000+"sec");
		}
	};	
	private void delFile(String strFileName)
	{
		try{
			File myFile = new File(strFileName);
			if(myFile.exists())
			{
				myFile.delete();
			}
		}catch(Exception e)
		{
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
	}
	public void mMakeTextToast(String str,boolean isLong)
	{
		if(isLong==true)
		{
			Toast.makeText(TutorialOnFaceDetect.this, str, Toast.LENGTH_LONG).show();
		}else
		{
			Toast.makeText(TutorialOnFaceDetect.this, str, Toast.LENGTH_SHORT).show();
		}
	}
	private boolean checkSDCard()
	{
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			return true;
		}else
		{
			return false;
		}
	}	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
		Log.i(TAG, "Surface changed");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Surface Created");	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try
		{
			//delFile(strCapatureFilePath);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		Log.i(TAG, "Surface Destroyed");	
	}
}