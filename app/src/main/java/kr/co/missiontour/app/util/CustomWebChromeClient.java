package kr.co.missiontour.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomWebChromeClient extends WebChromeClient {

    private CallBackReturnWebBrowser mCallBackReturnWebBrowser = null;
    private ValueCallback mUploadMessage = null;
    private ValueCallback<Uri[]> mFilePathCallback = null;
    private File mTempFile = null;
    private String mCameraPhotoPath;
    public static int WEBVIEW_REQ_CODE = 1212;
    public static int WEBVIEW_REQ_CODE_PRE_LOLLIPOP = 1212;

    public interface CallBackReturnWebBrowser {
        public void startLoading();
        public void stopLoading();
        public void openPopupWindow(WebView view);
        public void closePopupWindow(WebView view);
    }
    public void setOnCallBackReturnWebBrowser(CallBackReturnWebBrowser mCallBackReturnWebBrowser) {
        this.mCallBackReturnWebBrowser = mCallBackReturnWebBrowser;
    }


    private Context mContext = null;
    public CustomWebChromeClient(Context context) {
//        mActivity = (Activity) context;
        mContext = context;
        //cwcc = this;
    }

    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);

        if(Global.IS_USE_PROCESS_LOADING_ANIMATION_BAR) {
            if (newProgress == 100) {
                //MainActivity.getInstance().offLoading();
                mCallBackReturnWebBrowser.stopLoading();
            } else {
                //MainActivity.getInstance().onLoading();
                mCallBackReturnWebBrowser.startLoading();
            }
        }

        /*
        if(MainActivity.getInstance()!=null)
        {
            if(MainActivity.getInstance().isFirstLoading())
            {
                if(newProgress==100)
                {
                    //MainActivity.getInstance().offBlind();
                    mCallBackReturnWebBrowser.stopLoading();
                }
            }
            else
            {
                if(Global.IS_USE_PROCESS_LOADING_ANIMATION_BAR) {
                    if (newProgress == 100) {
                        //MainActivity.getInstance().offLoading();
                        mCallBackReturnWebBrowser.stopLoading();
                    } else {
                        //MainActivity.getInstance().onLoading();
                        mCallBackReturnWebBrowser.startLoading();
                    }
                }
            }
        }
        */

    }


    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if(Global.IS_USE_PROCESS_SHOW_CONSOLE_TOAST_LOG) {
            Toast.makeText(mContext, consoleMessage.message() + "[" + consoleMessage.lineNumber() + "]", Toast.LENGTH_LONG).show();
        } else if(Global.IS_USE_PROCESS_SHOW_CONSOLE_LOG) {
            Log.d(Global.DEBUG_LOG, consoleMessage.message() + "[" + consoleMessage.lineNumber() + "]");
        }
        return super.onConsoleMessage(consoleMessage);
    }

    public boolean onJsAlert(WebView view, String url, String message,
                             final JsResult result) {
        //return super.onJsAlert(view, url, message, result);
        new AlertDialog.Builder(view.getContext())
                //.setTitle("알림")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();
        return true;
    }


    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        //return super.onJsConfirm(view, url, message, result);
        new AlertDialog.Builder(view.getContext())
                //.setTitle("알림")
                .setMessage(message)
                .setPositiveButton("확인",
                        new AlertDialog.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setNegativeButton("취소",
                        new AlertDialog.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();
        return true;
    }


    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        //super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType ) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
//        mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), WEBVIEW_REQ_CODE);
    }

    //For Android 4.1+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
////        mActivity.startActivityForResult
//        (
//                Intent.createChooser(i, "File Browser"),
//                WEBVIEW_REQ_CODE_PRE_LOLLIPOP);
    }



    //For Android 5.0 +
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {

        // Double check that we don't have any existing callbacks
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;

        // Set up the take picture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
//            // Create the File where the photo should go
//
//
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                Log.e("1", "Unable to create Image File", ex);
//            }
//
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
//            } else {
//                takePictureIntent = null;
//            }
//        }
        // Set up the intent to get an existing image
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        // Set up the intents for the Intent chooser
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }


        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);


//        mActivity.startActivityForResult(chooserIntent, WEBVIEW_REQ_CODE);

        return true;
    }

    public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {

        if (requestCode == WEBVIEW_REQ_CODE) {
            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(results);
            }
            mFilePathCallback = null;

        }

        if ( requestCode == WEBVIEW_REQ_CODE_PRE_LOLLIPOP ){
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
            if (null == mUploadMessage) {
                return;
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageFile;
    }

    //----------- 동영상 ------------------//
//    private Activity mActivity = null;
//    private boolean FULLSCREEN = false;
//    public boolean isFullScreen() {
//        return FULLSCREEN;
//    }
//    public View mCustomVideoView;
//    private CustomViewCallback mCustomViewCallback;
//    private int mOriginalOrientation;
//
//    private FrameLayout mContentView;
//    private FrameLayout mFullscreenContainer;
//
//    private final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//    public void onShowCustomView(View view, CustomViewCallback callback) {
//
//        if (Build.VERSION.SDK_INT >= 14) {
//            if (mCustomVideoView != null) {
//                callback.onCustomViewHidden();
//                return;
//            }
//
//            //mOriginalOrientation = getRequestedOrientation();
//            FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
//            mFullscreenContainer = new FullscreenHolder(mActivity);
//            mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
//            decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
//            mCustomVideoView = view;
//            setFullscreen(true);
//            mCustomViewCallback = callback;
//        }
//        super.onShowCustomView(view, callback);
//
//    }

//    public void onHideCustomView() {
//        if (mCustomVideoView == null) {
//            return;
//        }
//        setFullscreen(false);
//        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
//        decor.removeView(mFullscreenContainer);
//        mFullscreenContainer = null;
//        mCustomVideoView = null;
//        mCustomViewCallback.onCustomViewHidden();
//        //setRequestedOrientation(mOriginalOrientation);
//    }
//
//    private void setFullscreen(boolean enabled) {
//        FULLSCREEN = enabled;
//        Window win = mActivity.getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        if (enabled) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//            if (mCustomVideoView != null) {
//                mCustomVideoView.setVisibility(View.VISIBLE);
//            } else {
//                mContentView.setVisibility(View.VISIBLE);
//            }
//        }
//        win.setAttributes(winParams);
//    }

    class FullscreenHolder extends FrameLayout {
        //    	private boolean close = true;
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
//        	if(close) closeView();
            return true;
        }

        public void closeView() {
//        	close = false;
//        	new AlertDialog.Builder(mActivity).setTitle("플레이어 닫기")
//        	.setPositiveButton("닫기", new AlertDialog.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					onHideCustomView();
//				}
//			})
//			.setNegativeButton("취소", new AlertDialog.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					close = true;
//					return;
//				}
//			})
//			.show();
        }


    }



    //--------------------- 새창띄우기 -----------------------//
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        Log.d(Global.DEBUG_LOG, "[onCreateWindow] isDialog : " + isDialog + " / isUserGesture : " + isUserGesture + " / resultMsg : " + resultMsg.toString());

        final WebView childView = new WebView(view.getContext());
        final WebSettings settings = childView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        childView.setWebChromeClient(this);

        if(isDialog) {
            childView.setWebViewClient(new WebViewClient());
            mCallBackReturnWebBrowser.openPopupWindow(childView);
        }

        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(childView);
        resultMsg.sendToTarget();

        return true;


    }

    @Override
    public void onCloseWindow(WebView window) {
        window.setVisibility(View.GONE);
        mCallBackReturnWebBrowser.closePopupWindow(window);
    }


}
