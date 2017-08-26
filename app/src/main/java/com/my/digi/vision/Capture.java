package com.my.digi.vision;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Landmark;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class Capture extends Activity implements View.OnClickListener, OnItemSelectedListener, TextToSpeech.OnInitListener {

	boolean myAudioToggle = true;
	boolean textOnClick, imageOnClick;
	Button bSearch, bSound, bUpload, bTextSearch, bWiki, bPlay, bVoiceRecord;
	Canvas canvas;
	EditText etTextSearch;
	float scaleX, scaleY, scaleWidth, scaleHeight;
	ImageView ivPreview;
	int x, y, startX, startY, cropX, cropY, cropWidth, cropHeight, width, height, defaulttvHeight, targettvHeight, defaulttvWidth, targettvWidth, landmarkTopXVertices, landmarkTopYVertices, landmarkBottomXVertices, landmarkBottomYVertices;
	int defaultivHeight, targetivHeight, defaultivWidth, targetivWidth, defaultivLeft, defaultivTop;
	int topLeftVertexX, topLeftVertexY, bottomRightVertexX, bottomRightVertexY;
	Locale localeDestination, localeSource, localeSourceText, localeSourceVoice;
	Paint paint;
	Spinner sLanguageDestination, sLanguageSourceText, sLanguageSourceVoice;
	String option, objectResults, textResults, landmarkResult, logoResult;
	String[] objectsArray, objectsMidArray, localesArray, textsArray;
	int[] textTopXVertices, textTopYVertices, textBottomXVertices, textBottomYVertices;
	float[] objectsScoreArray;
	SurfaceHolder holderTransparent;
	SurfaceView transparentView;
	TextView tvLoading, tvDetails, tvSearchType, tvObjectDetails, tvTextDetails, tvLandmarkDetails, tvLogoDetails, tvTranslationResult, tvLogos, tvObjects, tvTexts, tvLandmarks;
	private static final int GALLERY_IMAGE_REQUEST = 1;
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyCdm3gfCWDVmVm4Vhs43a4QN4upKjA7bfM";
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyAajx889Bm3dN6Ga1VU6ZAzqLmQEz7sPy8";
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyBC_HGuCjfOxQ5tKB31vEya3aUUjdx3wR0";
	private static final String CLOUD_VISION_API_KEY = "AIzaSyBWqxA25q0fai-twE6FbKS7lzJKGe643zA";
	private AudioManager audio;
	private Camera mCamera;
	private CameraPreview mPreview;
	protected static final String TAG = null;
	private static int SR_CODE = 123;
	public TextToSpeech tts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camerav2);
		initialize();

		//set background
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		//enable drawing of box to select area of image
		OnTouchListener onTouchListner = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				int action = event.getAction();

				x = (int) event.getX();
				y = (int) event.getY();

				switch(action){

					case MotionEvent.ACTION_DOWN:
						startX = x;
						startY = y;
						break;
					case MotionEvent.ACTION_MOVE:
						DrawFocusRect(startX, startY, x, y);
						break;
					case MotionEvent.ACTION_UP:
						DrawFocusRect(startX, startY, x, y);
						//finalizeDrawing();
						break;
				}
				return true;
			}
		};
		preview.setOnTouchListener(onTouchListner);

		//set objects for image search invisible
		tvObjects.setVisibility(View.INVISIBLE);
		tvTexts.setVisibility(View.INVISIBLE);
		tvLandmarks.setVisibility(View.INVISIBLE);
		tvLogos.setVisibility(View.INVISIBLE);
		tvObjectDetails.setVisibility(View.INVISIBLE);
		tvTextDetails.setVisibility(View.INVISIBLE);
		tvLandmarkDetails.setVisibility(View.INVISIBLE);
		tvLogoDetails.setVisibility(View.INVISIBLE);
		ivPreview.setVisibility(View.INVISIBLE);
		bWiki.setVisibility(View.INVISIBLE);

		//set objects for text and voice search invisible
		sLanguageSourceText.setVisibility(View.INVISIBLE);
		sLanguageSourceVoice.setVisibility(View.INVISIBLE);
		sLanguageDestination.setVisibility(View.INVISIBLE);
		etTextSearch.setVisibility(View.INVISIBLE);
		tvTranslationResult.setVisibility(View.INVISIBLE);
		bPlay.setVisibility(View.INVISIBLE);
		bVoiceRecord.setVisibility(View.INVISIBLE);

		//set title of page
		tvSearchType.setText(Html.fromHtml("<b>" + option + " Search" + "</b>" + "<br />" + "<small>" + "Draw box around object to be captured and click on search icon" + "</small>"));
	}

	private void initialize() {
		// TODO Auto-generated method stub
		//set values for image search
		x = y = 0;
		targettvHeight = targettvWidth = targetivHeight = targetivWidth = 0;
		option = "Image";
		textOnClick = true;
		imageOnClick = true;

		//set values for text and voice search
		String[] selectionSourceText = {"English:", "French:", "Spanish:"};
		String[] selectionSourceVoice = {"Chinese:", "English:", "French:", "German:", "Hindi:", "Italian:", "Japanese:", "Korean:", "Malay:", "Norwegian:", "Spanish:", "Thai:"};
		String[] selectionDestination = {"Chinese:", "English:", "French:", "German:", "Hindi:", "Italian:", "Japanese:", "Korean:", "Malay:", "Norwegian:", "Spanish:", "Thai:"};
		myAudioToggle = true;
		localeSourceText = new Locale("en");
		localeSourceVoice = new Locale("en");
		localeDestination = new Locale("ms");

		//Define audio objects
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		tts = new TextToSpeech(this, this);

		//Define image objects
		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		transparentView = (SurfaceView)findViewById(R.id.TransparentView);
		holderTransparent = transparentView.getHolder();
		holderTransparent.setFormat(PixelFormat.TRANSPARENT);
		holderTransparent.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		//Define textView objects
		tvObjects = (TextView) findViewById(R.id.tvObjects);
		tvTexts = (TextView) findViewById(R.id.tvTexts);
		tvLandmarks = (TextView) findViewById(R.id.tvLandmarks);
		tvLogos = (TextView) findViewById(R.id.tvLogos);
		tvObjectDetails = (TextView) findViewById(R.id.tvObjectDetails);
		tvTextDetails = (TextView) findViewById(R.id.tvTextDetails);
		tvLandmarkDetails = (TextView) findViewById(R.id.tvLandmarkDetails);
		tvLogoDetails = (TextView) findViewById(R.id.tvLogoDetails);
		tvTranslationResult = (TextView) findViewById(R.id.tvTranslationResult);
		tvLoading = (TextView) findViewById(R.id.tvLoading);
		tvSearchType = (TextView) findViewById(R.id.tvSearchType);

		tvTextDetails.setOnClickListener(this);
		ivPreview.setOnClickListener(this);

		//Define editText objects
		etTextSearch = (EditText) findViewById(R.id.etTextSearch);

		//Define buttons
		bSearch = (Button) findViewById(R.id.bSearch);
		bSound = (Button) findViewById(R.id.bMute);
		bUpload = (Button) findViewById(R.id.bUpload);
		bTextSearch = (Button) findViewById(R.id.bTextSearch);
		bWiki = (Button) findViewById(R.id.bWiki);
		bVoiceRecord = (Button) findViewById(R.id.bVoiceRecord);
		bPlay = (Button) findViewById(R.id.bPlaySound);

		bSearch.setOnClickListener(this);
		bSound.setOnClickListener(this);
		bUpload.setOnClickListener(this);
		bTextSearch.setOnClickListener(this);
		bWiki.setOnClickListener(this);
		bVoiceRecord.setOnClickListener(this);
		bPlay.setOnClickListener(this);

		//Define spinners
		ArrayAdapter<String> adapterSourceText = new ArrayAdapter<String>(Capture.this, android.R.layout.simple_spinner_item, selectionSourceText);
		ArrayAdapter<String> adapterSourceVoice = new ArrayAdapter<String>(Capture.this, android.R.layout.simple_spinner_item, selectionSourceVoice);
		ArrayAdapter<String> adapterDestination = new ArrayAdapter<String>(Capture.this, android.R.layout.simple_spinner_item, selectionDestination);

		sLanguageSourceText = (Spinner) findViewById(R.id.sLanguageSourceText);
		sLanguageSourceText.setAdapter(adapterSourceText);
		sLanguageSourceText.setSelection(0);
		sLanguageSourceText.setOnItemSelectedListener(this);

		sLanguageSourceVoice = (Spinner) findViewById(R.id.sLanguageSourceVoice);
		sLanguageSourceVoice.setAdapter(adapterSourceVoice);
		sLanguageSourceVoice.setSelection(1);
		sLanguageSourceVoice.setOnItemSelectedListener(this);

		sLanguageDestination = (Spinner) findViewById(R.id.sLanguageDestination);
		sLanguageDestination.setAdapter(adapterDestination);
		sLanguageDestination.setSelection(8);
		sLanguageDestination.setOnItemSelectedListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (tts != null){
			tts.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.shutdown();
		}
		super.onDestroy();
	}

	/////////////////////START OF NON-MAIN FUNCTIONS///////////////////////////

	//BUTTON DEFINITION FUNCTIONS//

	public void onClick(View v){
		switch (v.getId()){
			//Toggles between mute and unmute
			case R.id.bMute:
				myAudioToggle = !myAudioToggle;
				setAudio(myAudioToggle);
				break;

			//
			case R.id.bTextSearch:
				x = y = 0;
				Clear();
				//Transitioning from Voice to Image
				if (option == "Voice"){
					bUpload.setVisibility(View.VISIBLE);
					bVoiceRecord.setVisibility(View.INVISIBLE);
					etTextSearch.setVisibility(View.INVISIBLE);

					bTextSearch.setBackgroundResource(R.drawable.text_icon);
					bSearch.setBackgroundResource(R.drawable.search_icon);

					option = "Image";
					tvSearchType.setText(Html.fromHtml("<b>" + option + " Search" + "</b>" + "<br />" + "<small>" + "Draw box around object to be captured and click on search icon" + "</small>"));
				}
				//Transitioning from Image to Text
				else if (option == "Image"){
					bUpload.setVisibility(View.INVISIBLE);
					etTextSearch.setVisibility(View.VISIBLE);
					etTextSearch.setText("");
					sLanguageSourceText.setVisibility(View.VISIBLE);
					sLanguageSourceVoice.setVisibility(View.INVISIBLE);
					sLanguageDestination.setVisibility(View.VISIBLE);
					sLanguageDestination.setSelection(8);

					bTextSearch.setBackgroundResource(R.drawable.voice_icon);
					bSearch.setBackgroundResource(R.drawable.translate_icon);

					targettvHeight = targettvWidth = 0;

					option = "Text";
					tvSearchType.setText(Html.fromHtml("<b>" + option + " Search" + "</b>" + "<br />" + "<small>" + "Select input and output language, key in word/phrase, and click translate icon" + "</small>"));
				}
				//Transtioning from Text to Voice
				else{
					bUpload.setVisibility(View.INVISIBLE);
					bVoiceRecord.setVisibility(View.VISIBLE);
					etTextSearch.setVisibility(View.VISIBLE);
					etTextSearch.setText("");
					sLanguageSourceText.setVisibility(View.INVISIBLE);
					sLanguageSourceVoice.setVisibility(View.VISIBLE);
					sLanguageDestination.setVisibility(View.VISIBLE);
					sLanguageDestination.setSelection(1);

					bTextSearch.setBackgroundResource(R.drawable.camera_icon);
					bSearch.setBackgroundResource(R.drawable.translate_icon);

					option = "Voice";
					tvSearchType.setText(Html.fromHtml("<b>" + option + " Search" + "</b>" + "<br />" + "<small>" + "Select input and output language, then click on microphone icon to capture speech" + "</small>"));

					//make sure keyboard is hidden
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(bTextSearch.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
				}
				break;

			//Initiates gallery for user to select image from gallery
			case R.id.bUpload:
				x = y = 0;
				Clear();
				ivPreview.setVisibility(View.VISIBLE);
				startGalleryChooser();
				break;

			//Initiates capturing of picture or translation
			case R.id.bSearch:
				Clear();
				//If image search to initiate taking of picture
				if (option == "Image"){
					ivPreview.setVisibility(View.VISIBLE);
					mCamera.takePicture(null, null, mPicture);
					mCamera.startPreview();
				}
				//If text or voice search to initiate translation
				else{
					etTextSearch.setVisibility(View.VISIBLE);
					sLanguageDestination.setVisibility(View.VISIBLE);
					if (option == "Text"){
						sLanguageSourceText.setVisibility(View.VISIBLE);
						localeSource = localeSourceText;
					}
					else{
						sLanguageSourceVoice.setVisibility(View.VISIBLE);
						localeSource = localeSourceVoice;
					}
					try {
						Language languageSource = getLanguage(localeSource);
						Language languageDestination = getLanguage(localeDestination);
						TranslateText(etTextSearch.getText().toString(), languageSource, languageDestination);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			//Allows to expand and minimize text searh result by clicking on text view
			case R.id.tvTextDetails:

				ViewGroup.LayoutParams params = tvTextDetails.getLayoutParams();

				if(textOnClick){
					defaulttvHeight = tvTextDetails.getHeight();
					defaulttvWidth = tvTextDetails.getWidth();
					if (targettvHeight == 0 && targettvWidth == 0){
						params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
						params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
						tvTextDetails.setLayoutParams(params);
					}
					else{
						tvTextDetails.setHeight(targettvHeight);
						tvTextDetails.setWidth(targettvWidth);
					}
					tvLandmarkDetails.setVisibility(View.INVISIBLE);
					tvLogoDetails.setVisibility(View.INVISIBLE);
					textOnClick = false;
				}
				else{
					targettvHeight = tvTextDetails.getHeight();
					targettvWidth = tvTextDetails.getWidth();
					tvTextDetails.setHeight(defaulttvHeight);
					tvTextDetails.setWidth(defaulttvWidth);
					textOnClick = true;
					if (landmarkResult != null){
						tvLandmarkDetails.setVisibility(View.VISIBLE);
					}
					if (logoResult != null){
						tvLogoDetails.setVisibility(View.VISIBLE);
					}
				}
				break;

			//Expand image preview
			case R.id.ivPreview:

				RelativeLayout.LayoutParams ivParams;

				if(imageOnClick){
					defaultivHeight = ivPreview.getHeight();
					defaultivWidth = ivPreview.getWidth();
					defaultivLeft = ivPreview.getLeft();
					defaultivTop = ivPreview.getTop();
					ivParams = new RelativeLayout.LayoutParams(800,800);
					ivParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					ivPreview.setLayoutParams(ivParams);
					tvObjectDetails.setVisibility(View.INVISIBLE);
					tvTextDetails.setVisibility(View.INVISIBLE);
					tvLandmarkDetails.setVisibility(View.INVISIBLE);
					tvLogoDetails.setVisibility(View.INVISIBLE);
					tvObjects.setVisibility(View.INVISIBLE);
					tvTexts.setVisibility(View.INVISIBLE);
					tvLandmarks.setVisibility(View.INVISIBLE);
					tvLogos.setVisibility(View.INVISIBLE);
					bWiki.setVisibility(View.INVISIBLE);
					imageOnClick = false;
				}
				else{
					ivParams = new RelativeLayout.LayoutParams(defaultivHeight,defaultivWidth);
					ivParams.leftMargin = defaultivLeft;
					ivParams.topMargin = defaultivTop;
					ivPreview.setLayoutParams(ivParams);
					imageOnClick = true;
					if (objectResults != ""){
						tvObjects.setVisibility(View.VISIBLE);
						tvObjectDetails.setVisibility(View.VISIBLE);
					}
					if (textResults != ""){
						tvTexts.setVisibility(View.VISIBLE);
						tvTextDetails.setVisibility(View.VISIBLE);
					}
					if (landmarkResult != null){
						tvLandmarks.setVisibility(View.VISIBLE);
						tvLandmarkDetails.setVisibility(View.VISIBLE);
						bWiki.setVisibility(View.VISIBLE);
					}
					if (logoResult != null){
						tvLogos.setVisibility(View.VISIBLE);
						tvLogoDetails.setVisibility(View.VISIBLE);
					}
				}
				break;

			//Invokes browser to show details about landmark result
			case R.id.bWiki:
				if (landmarkResult != null){
					landmarkResult = landmarkResult.replaceAll(" ", "_");
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/" + landmarkResult));
					startActivity(browserIntent);
				}
				break;

			//Allows user to invoke reading of translation without having to re-perform search
			case R.id.bPlaySound:
				if (tvTranslationResult != null){
					tts.speak(tvTranslationResult.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
				}
				break;

			//Invokes google speech recoginition module
			case R.id.bVoiceRecord:
				listen();
				break;
		}
	}

	//SPINNER DEFINITION FUNCTIONS//

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
							   long id) {
		// TODO Auto-generated method stub
		int selectionSourceText = sLanguageSourceText.getSelectedItemPosition();
		int selectionSourceVoice = sLanguageSourceVoice.getSelectedItemPosition();
		int selectionDestination = sLanguageDestination.getSelectedItemPosition();

		Spinner spinnerTemp = (Spinner) parent;
		if (spinnerTemp.getId() == R.id.sLanguageSourceText || spinnerTemp.getId() == R.id.sLanguageSourceVoice){
			etTextSearch.setText("");
		}
		tvTranslationResult.setText("");

		if (option == "Text"){
			switch(selectionSourceText) {
				case 0:
					localeSourceText = new Locale("en");
					break;
				case 1:
					localeSourceText = new Locale("fr");
					break;
				case 2:
					localeSourceText = new Locale("es");
					break;
			}
		}
		else{
			switch(selectionSourceVoice){
				case 0:
					localeSourceVoice = new Locale("zh");
					break;
				case 1:
					localeSourceVoice = new Locale("en");
					break;
				case 2:
					localeSourceVoice = new Locale("fr");
					break;
				case 3:
					localeSourceVoice = new Locale("de");
					break;
				case 4:
					localeSourceVoice = new Locale("hi");
					break;
				case 5:
					localeSourceVoice = new Locale("it");
					break;
				case 6:
					localeSourceVoice = new Locale("ja");
					break;
				case 7:
					localeSourceVoice = new Locale("ko");
					break;
				case 8:
					localeSourceVoice = new Locale("ms");
					break;
				case 9:
					localeSourceVoice = new Locale("no");
					break;
				case 10:
					localeSourceVoice = new Locale("es");
					break;
				case 11:
					localeSourceVoice = new Locale("th");
					break;
			}
		}

		switch(selectionDestination) {
			case 0:
				localeDestination = new Locale("zh");
				break;
			case 1:
				localeDestination = new Locale("en");
				break;
			case 2:
				localeDestination = new Locale("fr");
				break;
			case 3:
				localeDestination = new Locale("de");
				break;
			case 4:
				localeDestination = new Locale("hi");
				break;
			case 5:
				localeDestination = new Locale("it");
				break;
			case 6:
				localeDestination = new Locale("ja");
				break;
			case 7:
				localeDestination = new Locale("ko");
				break;
			case 8:
				localeDestination = new Locale("ms");
				break;
			case 9:
				localeDestination = new Locale("no");
				break;
			case 10:
				localeDestination = new Locale("es");
				break;
			case 11:
				localeDestination = new Locale("th");
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	//////////////////////////IMAGE PROCESSING FUNCTIONS/////////////////////////////

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	//draw box to isolate area of interest
	private void DrawFocusRect(int RectLeft, int RectTop, int RectRight, int RectBottom)
	{
		canvas = holderTransparent.lockCanvas();
		canvas.drawColor(0,Mode.CLEAR);
		//border's properties
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));
		paint.setColor(Color.RED);
		paint.setStrokeWidth(10);
		canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);

		holderTransparent.unlockCanvasAndPost(canvas);
	}

	//on image taken
	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			try {
				//compress image so that device does not freeze upon taking picture
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inTempStorage = new byte[16 * 1024];
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size pictureSize = parameters.getPictureSize();

				int height11 = pictureSize.height;
				int width11 = pictureSize.width;
				float mb = (width11 * height11) / 1024000;

				if (mb > 4f)
					options.inSampleSize = 4;
				else if (mb > 3f)
					options.inSampleSize = 2;

				//Rotate and preview image
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				Matrix mat = new Matrix();
				mat.postRotate(90);  // angle is the desired angle you wish to rotate
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);

				//crop image based on box drawn
				if(x!=0 || y!=0){
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					width = size.x;
					height = size.y;

					if (startX < x) {
						scaleX = (float) startX / (float) width * (float) bitmap.getWidth();
						scaleWidth = (float) (x - startX) / (float) width * (float) bitmap.getWidth();
					}
					else {
						scaleX = (float) x / (float) width * (float) bitmap.getWidth();
						scaleWidth = (float) (startX - x) / (float) width * (float) bitmap.getWidth();
					}

					if (startY < y){
						scaleY = (float) startY / (float) height* (float) bitmap.getHeight();
						scaleHeight = (float) (y-startY)/ (float) height* (float) bitmap.getHeight();
					}
					else{
						scaleY = (float) y / (float) height* (float) bitmap.getHeight();
						scaleHeight = (float) (startY-y)/ (float) height* (float) bitmap.getHeight();
					}

					cropX = (int)scaleX;
					cropY = (int)scaleY+20;
					cropWidth = (int)scaleWidth;
					cropHeight = (int)scaleHeight;

					Matrix matrix = new Matrix();
					matrix.postScale(0.5f, 0.5f);
					bitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight, matrix, true);
					x = y = 0;
				}

				//display image and call Google api
				ivPreview.setImageBitmap(bitmap);
				bitmap = resizeBitmap(bitmap);
				callCloudVision(bitmap);

			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	};

	//open gallery for selection
	public void startGalleryChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select a photo"),
				GALLERY_IMAGE_REQUEST);
	}

	//load image from phone gallery to passed on for image search
	public void uploadImage(Uri uri) {
		if (uri != null) {
			try {
				// scale the image to save on bandwidth
				Bitmap bitmapUpload = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

				ivPreview.setImageBitmap(bitmapUpload);
				bitmapUpload = resizeBitmap(bitmapUpload);
				callCloudVision(bitmapUpload);

			} catch (IOException e) {
				Log.d(TAG, "Image picking failed because " + e.getMessage());
				Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
			}
		} else {
			Log.d(TAG, "Image picker gave us a null image.");
			Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
		}
	}

	//resizing image before sending it to google for search
	public Bitmap resizeBitmap(Bitmap bitmap) {

		int maxDimension = 1024;
		int originalWidth = bitmap.getWidth();
		int originalHeight = bitmap.getHeight();
		int resizedWidth = maxDimension;
		int resizedHeight = maxDimension;

		if (originalHeight > originalWidth) {
			resizedHeight = maxDimension;
			resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
		} else if (originalWidth > originalHeight) {
			resizedWidth = maxDimension;
			resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
		} else if (originalHeight == originalWidth) {
			resizedHeight = maxDimension;
			resizedWidth = maxDimension;
		}
		return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
	}

	//////////////////////IMAGE SEARCH FUNCTIONS/////////////////////////////

	//Converting image, sending image too Google and parsing results to be printed in textViews
	private void callCloudVision(final Bitmap bitmap) throws IOException {
		// Switch text to loading
		tvLoading.setBackgroundColor(Color.WHITE);
		tvLoading.setText(R.string.loading_message);

		// Do the real work in an async task, because we need to use the network anyway
		new AsyncTask<Object, Void, String>() {
			ProgressDialog progressDialog = new ProgressDialog(Capture.this);

			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog.setTitle("Uploading image. Please wait...");
				progressDialog.show();
			}

			protected String doInBackground(Object... params) {
				try {
					HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
					JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

					Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
					builder.setVisionRequestInitializer(new
							VisionRequestInitializer(CLOUD_VISION_API_KEY));
					Vision vision = builder.build();

					BatchAnnotateImagesRequest batchAnnotateImagesRequest =
							new BatchAnnotateImagesRequest();
					batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
						AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

						// Add the image
						Image base64EncodedImage = new Image();
						// Convert the bitmap to a JPEG
						// Just in case it's a format that Android understands but Cloud Vision
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
						byte[] imageBytes = byteArrayOutputStream.toByteArray();

						// Base64 encode the JPEG
						base64EncodedImage.encodeContent(imageBytes);
						annotateImageRequest.setImage(base64EncodedImage);

						List<Feature> featureList = new ArrayList<>();

						Feature labelDetection = new Feature();
						labelDetection.setType("LABEL_DETECTION");
						labelDetection.setMaxResults(10);
						featureList.add(labelDetection);

						Feature textDetection = new Feature();
						textDetection.setType("TEXT_DETECTION");
						textDetection.setMaxResults(10);
						featureList.add(textDetection);

						Feature landmarkDetection = new Feature();
						landmarkDetection.setType("LANDMARK_DETECTION");
						landmarkDetection.setMaxResults(10);
						featureList.add(landmarkDetection);

						Feature logoDetection = new Feature();
						logoDetection.setType("LOGO_DETECTION");
						logoDetection.setMaxResults(10);
						featureList.add(logoDetection);

						annotateImageRequest.setFeatures(featureList);

						// Add the list of one thing to the request
						add(annotateImageRequest);
					}});

					Vision.Images.Annotate annotateRequest =
							vision.images().annotate(batchAnnotateImagesRequest);
					// Due to a bug: requests to Vision API containing large images fail when GZipped.
					annotateRequest.setDisableGZipContent(true);
					Log.d(TAG, "created Cloud Vision request object, sending request");

					BatchAnnotateImagesResponse response = annotateRequest.execute();
					return convertResponseToString(response);

				} catch (GoogleJsonResponseException e) {
					Log.d(TAG, "failed to make API request because " + e.getContent());
				} catch (IOException e) {
					Log.d(TAG, "failed to make API request because of other IOException " +
							e.getMessage());
				}
				return "Cloud Vision API request failed. Check logs for details.";
			}

			protected void onPostExecute(String result) {
				progressDialog.dismiss();

				objectResults = "";
				if (objectsArray != null){
					objectResults = objectResults + getNow() + "\n";
					for (int i=0; i < objectsArray.length; i++){
						objectResults = objectResults + objectsMidArray[i] + ", " + objectsArray[i] + "\n";
					}
					objectResults = objectResults + "\n";
					tvObjects.setVisibility(View.VISIBLE);
					tvObjectDetails.setVisibility(View.VISIBLE);
					tvObjectDetails.setMovementMethod(new ScrollingMovementMethod());
					tvObjectDetails.setText(objectResults);
					objectsArray = null;

					try {
						writeToFile(Environment.getExternalStorageDirectory().getPath(), "logfileVision.txt", objectResults);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				textResults = "";
				if (textsArray != null) {
					for (int i = 0; i < localesArray.length; i++) {
						if (localesArray[i] != "" && localesArray[i] != null){
							Locale locale = new Locale(localesArray[i]);
							Language languageSource = getLanguage(locale);
							//Only translate if results for text image search are returned and language of text image is not English
							if (languageSource != null){
								if(languageSource.name() == "ENGLISH") {
									textResults = textResults + textsArray[i] + " (" + languageSource.name() + ")\n";
									tvTexts.setVisibility(View.VISIBLE);
									tvTextDetails.setVisibility(View.VISIBLE);
									tvTextDetails.setMovementMethod(new ScrollingMovementMethod());
									tvTextDetails.setText(textResults);
								}
								else {
									try {
										TranslateText(textsArray[i], languageSource, Language.ENGLISH);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
							else {
								tvTexts.setVisibility(View.VISIBLE);
								tvTextDetails.setVisibility(View.VISIBLE);
								tvTextDetails.setMovementMethod(new ScrollingMovementMethod());
								tvTextDetails.setText("Language of text not supported (" + locale.toString() + ")");
							}
						}
					}

				}

				if (landmarkResult != null){
					bWiki.setVisibility(View.VISIBLE);
					tvLandmarks.setVisibility(View.VISIBLE);
					tvLandmarkDetails.setVisibility(View.VISIBLE);
					tvLandmarkDetails.setMovementMethod(new ScrollingMovementMethod());
					tvLandmarkDetails.setText(landmarkResult);
				}

				if (logoResult != null){
					tvLogos.setVisibility(View.VISIBLE);
					tvLogoDetails.setVisibility(View.VISIBLE);
					tvLogoDetails.setMovementMethod(new ScrollingMovementMethod());
					tvLogoDetails.setText(logoResult);
				}

				tvLoading.setText("");
				tvLoading.setBackgroundColor(Color.TRANSPARENT);
			}
		}.execute();
		mCamera.startPreview();
	}

	//Massaging information returned from Google. Arrays built in this function are used to print information to textViews in CallCloudVision function
	private String convertResponseToString(BatchAnnotateImagesResponse response) {
		StringBuilder message = new StringBuilder("Results:\n\n");
		message.append("Objects found:\n");

		int i = 0;
		List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

		if (labels != null) {
			objectsArray = new String[labels.size()];
			objectsScoreArray = new float[labels.size()];
			objectsMidArray = new String[labels.size()];
			for (EntityAnnotation label : labels) {
				objectsArray[i]=label.getDescription();
				objectsScoreArray[i]=label.getScore();
				objectsMidArray[i]=label.getMid();
				if (objectsScoreArray[i] > 0.5) {
					message.append(String.format(Locale.getDefault(), "%s", label.getMid(), "%s", label.getDescription()));
					message.append("\n");
				}
				i += 1;
			}
		} else {
			message.append("nothing\n");
		}

		i = 0;
		message.append("\n Texts found:\n");
		List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();

		if (texts != null) {
			localesArray = new String [texts.size()];
			textTopXVertices = new int [texts.size()];
			textTopYVertices = new int [texts.size()];
			textBottomXVertices = new int [texts.size()];
			textBottomYVertices = new int [texts.size()];
			textsArray = new String[texts.size()];
			for (EntityAnnotation text : texts) {
				if (text.size()==3){
					topLeftVertexX = text.getBoundingPoly().getVertices().get(0).getX();
					topLeftVertexY = text.getBoundingPoly().getVertices().get(0).getY();
					bottomRightVertexX = text.getBoundingPoly().getVertices().get(2).getX();
					bottomRightVertexY = text.getBoundingPoly().getVertices().get(2).getY();

					localesArray[i] = text.getLocale();
					textTopXVertices[i] = topLeftVertexX;
					textTopYVertices[i] = topLeftVertexY;
					textBottomXVertices[i] = bottomRightVertexX;
					textBottomYVertices[i] = bottomRightVertexY;
					textsArray[i]= text.getDescription().replaceAll("\\n", " ");
					message.append(String.format(Locale.getDefault(), "%s: %s",
							text.getLocale(), text.getDescription()));
					message.append("\n");
					i += 1;
				}
			}
		} else {
			message.append("nothing\n");
		}

		i = 0;
		landmarkResult = null;
		message.append("\nLandmark Found: ");
		List<EntityAnnotation> landmarks = response.getResponses().get(0).getLandmarkAnnotations();
		if (landmarks != null) {
			for (EntityAnnotation landmark : landmarks) {
				if (i==0) {
					landmarkResult = landmark.getDescription();
					landmarkTopXVertices = landmark.getBoundingPoly().getVertices().get(0).getX();
					landmarkTopYVertices = landmark.getBoundingPoly().getVertices().get(0).getY();
					landmarkBottomXVertices = landmark.getBoundingPoly().getVertices().get(2).getX();
					landmarkBottomYVertices = landmark.getBoundingPoly().getVertices().get(2).getY();
					message.append(String.format(Locale.getDefault(), "%s",
							landmark.getDescription()));
					message.append("\n");
					i += 1;
				}
			}
		} else {
			message.append("nothing\n");
		}

		i = 0;
		logoResult = null;
		message.append("\nLogo Found: ");
		List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
		if (logos != null) {
			for (EntityAnnotation logo : logos) {
				if (i==0) {
					logoResult = logo.getDescription();
					message.append(String.format(Locale.getDefault(), "%s",
							logo.getDescription()));
					message.append("\n");
					i += 1;
				}
			}
		} else {
			message.append("nothing\n");
		}
		return message.toString();
	}

	//////////////////////AUDIO PROCESSING FUNCTIONS/////////////////////////////

	//initializing text to speech function
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
				tts.setLanguage(Locale.US);
		} else if (status == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed...",
					Toast.LENGTH_LONG).show();
		}
	}

	//setting audio to mute and unmute
	private void setAudio(boolean b) {
		if (b){
			AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			bSound.setBackgroundResource(R.drawable.sound_icon_on);
			Toast.makeText(this, "Unmute", Toast.LENGTH_LONG).show();
		}
		else{
			AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
			bSound.setBackgroundResource(R.drawable.sound_icon_off);
			Toast.makeText(this, "Mute", Toast.LENGTH_LONG).show();
		}
	}

	//Invokes Google voice recognition to convert speech to text
	private void listen() {
		try {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			//Specify language
			String myLanguage = localeSourceVoice.toString();
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguage);
			intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguage);

			// Specify language model
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

			// Specify how many results to receive
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

			// Start listening
			startActivityForResult(intent, SR_CODE);
		} catch(ActivityNotFoundException e) {
			//prompts user to install google search recognition software if not available
			String appPackageName = "com.google.android.googlequicksearchbox";
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
		}
	}

	//////////////////////TEXT TRANSLATION FUNCTIONS/////////////////////////////

	//translate text from source language to destination language
	private void TranslateText(final String inputText, final Language localeSource, final Language localeDestination) throws IOException {

		new AsyncTask<Object, Void, String>() {
			ProgressDialog progressDialog = new ProgressDialog(Capture.this);

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog.setTitle("Translating text. Please wait...");
				progressDialog.show();
			}

			protected String doInBackground(Object... params) {
				Translate.setClientId("Vision1");
				Translate.setClientSecret("91wLXzrNNkmp5x2IQuPsd6QFrcRgheTdG3WkJpWXBdY=");
				String translatedText = null;
				try {
						translatedText = Translate.execute(inputText, localeSource, localeDestination);
				} catch (Exception e) {
					e.printStackTrace();
				}
				translatedText = translatedText + " ";
				return translatedText;
			}

			//print results to textViews
			@Override
			protected void onPostExecute(String result) {
				progressDialog.dismiss();

				//String textResults = "";
				if (option == "Text" || option == "Voice"){
					tvTranslationResult.setVisibility(View.VISIBLE);
					bPlay.setVisibility(View.VISIBLE);
					tvTranslationResult.setText(result);
					tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);
				}
				else{
					textResults = textResults + inputText + " (" + localeSource.name().replaceAll("_.*","") + ") = " + result + "(ENGLISH)\n";
					tvTexts.setVisibility(View.VISIBLE);
					tvTextDetails.setVisibility(View.VISIBLE);
					tvTextDetails.setMovementMethod(new ScrollingMovementMethod());
					tvTextDetails.setText(textResults);
					textsArray = null;
				}
			}
		}.execute();
	}

	//////////////////////IMAGE AND AUDIO PROCESSING FUNCTIONS/////////////////////////////

	//Calls upload image function for image search and prints result of speech to text to editText before calling translation function
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
			uploadImage(data.getData());
		}

		else if (requestCode == SR_CODE && resultCode == RESULT_OK) {
			if (data != null) {
				//Retrieves the best list SR result
				ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String bestResult = nBestList.get(0);
				etTextSearch.setText(bestResult);
				try {
					Language languageSource = getLanguage(localeSourceVoice);
					Language languageDestination = getLanguage(localeDestination);
					TranslateText(etTextSearch.getText().toString(), languageSource, languageDestination);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				//Reports error in recognition error in log
				Log.e(TAG, "Recognition was not successful");
			}

		}

	}

	public String getNow() {

		Time now = new Time();
		now.setToNow();
		String sTime = now.format("%Y%m%d %H:%M:%S");
		return sTime;
	}

	//////////////////////LANGUAGE REFERENCE TABLE/////////////////////////////

	//converts locale object into Language object for text to speech and translation function
	public Language getLanguage (Locale locale){
		Language languageReturn = null;

		switch (locale.toString()) {

			case "ar":
				languageReturn = Language.ARABIC;
				break;
			case "zh-CHS":
				languageReturn = Language.CHINESE_SIMPLIFIED;
				break;
			case "zh-CHT":
				languageReturn = Language.CHINESE_TRADITIONAL;
				break;
			case "zh-hant":
				languageReturn = Language.CHINESE_TRADITIONAL;
				break;
			case "zh":
				languageReturn = Language.CHINESE_TRADITIONAL;
				tts.setLanguage(Locale.CHINA);
				break;
			case "da":
				languageReturn = Language.DANISH;
				break;
			case "nl":
				languageReturn = Language.DUTCH;
				break;
			case "en":
				languageReturn = Language.ENGLISH;
				tts.setLanguage(Locale.US);
				break;
			case "oc":
				languageReturn = Language.FRENCH;
				break;
			case "fr":
				languageReturn = Language.FRENCH;
				tts.setLanguage(Locale.FRANCE);
				break;
			case "de":
				languageReturn = Language.GERMAN;
				tts.setLanguage(Locale.GERMAN);
				break;
			case "hi":
				languageReturn = Language.HINDI;
				tts.setLanguage(locale);
				break;
			case "in":
				languageReturn = Language.HINDI;
				break;
			case "he":
				languageReturn = Language.HEBREW;
				break;
			case "id":
				languageReturn = Language.INDONESIAN;
				break;
			case "it":
				languageReturn = Language.ITALIAN;
				tts.setLanguage(Locale.ITALY);
				break;
			case "ja":
				languageReturn = Language.JAPANESE;
				tts.setLanguage(Locale.JAPAN);
				break;
			case "ko":
				languageReturn = Language.KOREAN;
				tts.setLanguage(Locale.KOREA);
				break;
			case "ms":
				languageReturn = Language.MALAY;
				tts.setLanguage(Locale.US);
				break;
			case "no":
				languageReturn = Language.NORWEGIAN;
				tts.setLanguage(Locale.US);
				break;
			case "th":
				languageReturn = Language.THAI;
				tts.setLanguage(locale);
				break;
			case "sl":
				languageReturn = Language.SLOVENIAN;
				break;
			case "es":
				languageReturn = Language.SPANISH;
				tts.setLanguage(locale);
				break;
		}
		return languageReturn;
	}

	//////////////////////////CLEARING FUNCTIONS//////////////////////////////////

	//Clears objects when a button is pressed
	public void Clear(){
		tvObjects.setVisibility(View.INVISIBLE);
		tvTexts.setVisibility(View.INVISIBLE);
		tvLandmarks.setVisibility(View.INVISIBLE);
		tvLogos.setVisibility(View.INVISIBLE);
		tvObjectDetails.setVisibility(View.INVISIBLE);
		tvTextDetails.setVisibility(View.INVISIBLE);
		tvLandmarkDetails.setVisibility(View.INVISIBLE);
		tvLogoDetails.setVisibility(View.INVISIBLE);
		tvTranslationResult.setVisibility(View.INVISIBLE);
		tvLoading.setText("");

		etTextSearch.setVisibility(View.INVISIBLE);

		bWiki.setVisibility(View.INVISIBLE);
		bPlay.setVisibility(View.INVISIBLE);

		ivPreview.setVisibility(View.INVISIBLE);

		sLanguageSourceText.setVisibility(View.INVISIBLE);
		sLanguageSourceVoice.setVisibility(View.INVISIBLE);
		sLanguageDestination.setVisibility(View.INVISIBLE);

		landmarkResult = null;
		logoResult = null;
		objectsArray = null;
		textsArray = null;
		ClearCanvas();
	}

	//Clears box drawn
	private void ClearCanvas()
	{
		canvas = holderTransparent.lockCanvas();
		canvas.drawColor(0,Mode.CLEAR);
		holderTransparent.unlockCanvasAndPost(canvas);
	}

	//////////////////////////VOLUME BUTTON FUNCTIONS//////////////////////////////////

	//forces volume button to control volume of music instead of ringer
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				return true;
			default:
				return false;
		}
	}

	//////////////////////////WRITE TO FILE FUNCTIONS//////////////////////////////////

	public void writeToFile(String directory, String filename, String data ) throws IOException {
		File out;
		OutputStreamWriter outStreamWriter = null;
		FileOutputStream outStream = null;

		out = new File(new File(directory), filename);

		if ( out.exists() == false ){
			out.createNewFile();
		}

		outStream = new FileOutputStream(out, true) ;
		outStreamWriter = new OutputStreamWriter(outStream);

		outStreamWriter.append(data);
		outStreamWriter.flush();
	}
}
