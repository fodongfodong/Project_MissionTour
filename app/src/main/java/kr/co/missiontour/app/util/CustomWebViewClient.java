package kr.co.missiontour.app.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Browser;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import kr.co.missiontour.app.WebBrowserActivity;

public class CustomWebViewClient extends WebViewClient {

	private CallBackReturnWebBrowser mCallBackReturnWebBrowser = null;
	public interface CallBackReturnWebBrowser {
		public void selectPicture(String retfunc, int gallerymaxnumber, String... data);
		public void hideWebviewIntroLoadingImage();
		public void historyBack();
		public void moveUrl(String linkurl);
		public void updatePushToken();
		public void closePopup();
	}

	public void setOnCallBackReturnWebBrowser(CallBackReturnWebBrowser mCallBackReturnWebBrowser) {
		this.mCallBackReturnWebBrowser = mCallBackReturnWebBrowser;
	}
	private Context mContext = null;
	private Activity mActivity = null;
	public CustomWebViewClient(Context context) {
		mContext = context;
		mActivity = (Activity) context;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		Log.e(Global.DEBUG_LOG, "onPageFinished >> "+url);
		mCallBackReturnWebBrowser.hideWebviewIntroLoadingImage();
	}

	public boolean shouldOverrideUrlLoading(final WebView view, String url) {
		Log.e(Global.DEBUG_LOG, "shouldOverrideUrlLoading >> " + url);

		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		Uri catchUrl = Uri.parse(url);
		if(catchUrl.getScheme().equals("app")) {
			String control = catchUrl.getQueryParameter("control");
			String action = catchUrl.getQueryParameter("action");


			if (control.equals("share"))
			{

				try{
				String shareTitle = URLDecoder.decode(catchUrl.getQueryParameter("title"), "utf-8");
				String shareContent = URLDecoder.decode(catchUrl.getQueryParameter("content"), "utf-8");
				String shareLink = URLDecoder.decode(catchUrl.getQueryParameter("link"), "utf-8");
				String shareThumnail = Global.URL_ROOT+URLDecoder.decode(catchUrl.getQueryParameter("thumnail"), "utf-8");

				FeedTemplate params = FeedTemplate
						.newBuilder(ContentObject.newBuilder(shareTitle,
										shareThumnail,
										LinkObject.newBuilder().setWebUrl(shareLink)
												.setMobileWebUrl(shareLink).build())
								.setDescrption(shareContent)
								.build())
						.addButton(new ButtonObject("웹으로 보기", LinkObject.newBuilder()
								.setMobileWebUrl(shareLink)
								.build()))
						.build();
				Map<String, String> serverCallbackArgs = new HashMap<String, String>();
				KakaoLinkService.getInstance().sendDefault(mContext, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
					@Override
					public void onFailure(ErrorResult errorResult) {
						Logger.e(errorResult.toString());
						Toast.makeText(mContext.getApplicationContext(), errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
					}
					@Override
					public void onSuccess(KakaoLinkResponse result) {
					}
				});
				}catch (Exception e){
					e.printStackTrace();
				}
				return true;
			}
			//오픈링크
			else if(control.equals("openlink")){
				String linkUrl = catchUrl.getQueryParameter("link");
				String target = catchUrl.getQueryParameter("target");
				if(target.equals("_blank")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
					mContext.startActivity(browserIntent);
				}
			}
			//로그인일 경우
			else if(control.equals("autologin")){
                String mb_id = catchUrl.getQueryParameter("mb_id");
				//검색해보기
                SharedPreferences pref = mContext.getSharedPreferences("missiontour", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("app.first.user_id", mb_id);
                editor.putString("app.local.autologin", "Y");
                editor.commit();

				Global.mUserData.is_login = true;
				Global.mUserData.user_id = mb_id;

				view.loadUrl(Global.URL_ROOT);
                //view.loadUrl(Global.URL_ROOT+"?agency_lat="+Global.mUserData.myLat+"&agency_lng="+Global.mUserData.myLng);
            }

			//소셜로그인일 경우
			else if(control.equals("sociallogin")){

				view.loadUrl(Global.URL_ROOT);
				//view.loadUrl(Global.URL_ROOT+"?agency_lat="+Global.mUserData.myLat+"&agency_lng="+Global.mUserData.myLng);

				String mb_id = catchUrl.getQueryParameter("mb_id");
				Global.mUserData.is_login = true;
				Global.mUserData.user_id = mb_id;

			}

            else if(control.equals("logout")){
                SharedPreferences pref = mContext.getSharedPreferences("missiontour", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("app.first.user_id", "");
                editor.putString("app.local.autologin", "N");
                editor.commit();

                //view.loadUrl(Global.URL_ROOT+"?agency_lat="+Global.mUserData.myLat+"&agency_lng="+Global.mUserData.myLng);
				view.loadUrl(Global.URL_ROOT);
            }

			else if(control.equals("getgps")){
				if(WebBrowserActivity.getInstance() != null) {
					WebBrowserActivity.getInstance().sendGps();
				}
			}

			else if(control.equals("browser"))
			{
				if (action.equals("open"))
				{
					String link_url = catchUrl.getQueryParameter("link_url");
					String link_parameter = catchUrl.getQueryParameter("link_parameter");
					String real_parameter = "";
					try {
						JSONArray objArr = new JSONArray(link_parameter);
						for(int i=0; i<objArr.length(); i++) {
							JSONObject data = objArr.getJSONObject(i);
							String key = data.getString("key");
							String val = data.getString("val");
							real_parameter += "&"+key+"="+val;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Log.d(Global.DEBUG_LOG, "link_parameter => " + link_parameter);
					Log.d(Global.DEBUG_LOG, "real_parameter => " + real_parameter);

					Intent intent = new Intent(mContext, WebBrowserActivity.class);
					intent.putExtra("link_url", link_url);
					intent.putExtra("link_parameter", real_parameter);
					((Activity)mContext).startActivity(intent);
				}
				else if (action.equals("close"))
				{
					if(WebBrowserActivity.getInstance()!=null)
						WebBrowserActivity.getInstance().closeApp();
				}
			}

			return true;
		}
		else if(url.equals(Global.URL_ROOT+"index.php?device=mobile")
				|| url.equals(Global.URL_ROOT+"pages/lesson_list.php")
				|| url.equals(Global.URL_ROOT+"pages/facilities_list.php")
				|| url.equals(Global.URL_ROOT)){

			String paramStr = "?";
			if(url.equals(Global.URL_ROOT+"index.php?device=mobile")){
				paramStr = "&";
			}
			view.loadUrl(url+paramStr);
			//view.loadUrl(url+paramStr+"agency_lat="+Global.mUserData.myLat+"&agency_lng="+Global.mUserData.myLng);
			return true;
		}
		else if ((url.startsWith("http://") || url.startsWith("https://")) && url.endsWith(".apk"))
		{
			//downloadFile(url);
			//return super.shouldOverrideUrlLoading(view, url);
			String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
			downloadDefaultFile(url, filename);
			return true;
		}
		//일반 파일 다운로드
		else if(mimeTypeMap.getMimeTypeFromExtension(url.substring(url.lastIndexOf(".") + 1, url.length())) != null)
		//else if (url.toLowerCase().endsWith(".xls"))
		{
			String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
			downloadDefaultFile(url, filename);
			return true;

		} else if ((url.startsWith("http://") || url.startsWith("https://")) && (url.contains("market.android.com") || url.contains("m.ahnlab.com/kr/site/download"))) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			try {
				mContext.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
				return false;
			}
		} else if (url.startsWith("http://") || url.startsWith("https://")) {
			view.loadUrl(url);
			return true;
		} else if (url != null
				&& (url.contains("vguard") || url.contains("droidxantivirus") || url.contains("smhyundaiansimclick://")
				|| url.contains("smshinhanansimclick://") || url.contains("smshinhancardusim://") || url.contains("smartwall://") || url.contains("appfree://")
				|| url.contains("v3mobile") || url.endsWith(".apk") || url.contains("market://") || url.contains("ansimclick")
				|| url.contains("market://details?id=com.shcard.smartpay") || url.contains("shinhan-sr-ansimclick://"))) {
			return callApp(url);
		} else if (url.startsWith("smartxpay-transfer://")) {
			boolean isatallFlag = isPackageInstalled(mContext.getApplicationContext(), "kr.co.uplus.ecredit");
			if (isatallFlag) {
				boolean override = false;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());

				try {
					mContext.startActivity(intent);
					override = true;
				} catch (ActivityNotFoundException ex) {
				}
				return override;
			} else {
				showAlert("확인버튼을 누르시면 구글플레이로 이동합니다.", "확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("market://details?id=kr.co.uplus.ecredit")));
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
						mContext.startActivity(intent);
						//overridePendingTransition(0, 0);
					}
				}, "취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				return true;
			}
		} else if (url.startsWith("ispmobile://")) {
			boolean isatallFlag = isPackageInstalled(mContext.getApplicationContext(), "kvp.jjy.MispAndroid320");
			if (isatallFlag) {
				boolean override = false;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());

				try {
					mContext.startActivity(intent);
					override = true;
				} catch (ActivityNotFoundException ex) {
				}
				return override;
			} else {
				showAlert("확인버튼을 누르시면 구글플레이로 이동합니다.", "확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						view.loadUrl("http://mobile.vpay.co.kr/jsp/MISP/andown.jsp");
					}
				}, "취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				return true;
			}
		} else if (url.startsWith("sms:")) {
			try {
				String splitBodyStr[] = url.split("&body=");
				if (splitBodyStr[1].length() != 0) {
					String message = splitBodyStr[1];
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra("sms_body", URLDecoder.decode(splitBodyStr[1], "utf-8"));
					intent.setType("vnd.android-dir/mms-sms");
					mContext.startActivity(intent);
				} else {
					Toast.makeText(mContext.getApplicationContext(), "데이터가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
				}
			}catch (Exception e){
				Toast.makeText(mContext.getApplicationContext(), "데이터가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

			return true;
		} else if (url.startsWith("paypin://")) {
			boolean isatallFlag = isPackageInstalled(mContext.getApplicationContext(), "com.skp.android.paypin");
			if (isatallFlag) {
				boolean override = false;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());

				try {
					mContext.startActivity(intent);
					override = true;
				} catch (ActivityNotFoundException ex) {
				}
				return override;
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(("market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ..")));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
				mContext.startActivity(intent);
				//overridePendingTransition(0, 0);
				return true;
			}
		} else if (url.startsWith("lguthepay://")) {
			boolean isatallFlag = isPackageInstalled(mContext.getApplicationContext(), "com.lguplus.paynow");
			if (isatallFlag) {
				boolean override = false;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());

				try {
					mContext.startActivity(intent);
					override = true;
				} catch (ActivityNotFoundException ex) {
				}
				return override;
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("market://details?id=com.lguplus.paynow")));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
				mContext.startActivity(intent);
				//overridePendingTransition(0, 0);
				return true;
			}
		} else {
			return callApp(url);
		}

	}

	// 외부 앱 호출
	public boolean callApp(String url) {
		Intent intent = null;
		// 인텐트 정합성 체크 : 2014 .01추가
		try {
			intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
			Log.d(Global.DEBUG_LOG, "intent getScheme > " + intent.getScheme());
			Log.d(Global.DEBUG_LOG, "intent getDataString > " + intent.getDataString());
		} catch (URISyntaxException ex) {
			Log.d(Global.DEBUG_LOG, "Browser Bad URI " + url + ":" + ex.getMessage());
			return false;
		}
		//카카오톡 필터링
		if(intent.getScheme().equals("kakaolink")) {
			url = url.replace("intent:", "");
		}
		//
		try {
			Log.d(Global.DEBUG_LOG, "-----------------> 1");
			boolean retval = true;
			//chrome 버젼 방식 : 2014.01 추가						
			if (url.startsWith("intent")) { // chrome 버젼 방식
				// 앱설치 체크를 합니다.
				Log.d(Global.DEBUG_LOG, "-----------------> 2");
				if (mContext.getPackageManager().resolveActivity(intent, 0) == null) {
					String packagename = intent.getPackage();
					if (packagename != null) {
						Log.d(Global.DEBUG_LOG, "-----------------> 3");
						Uri uri = Uri.parse("market://search?q=pname:" + packagename);
						intent = new Intent(Intent.ACTION_VIEW, uri);
						mContext.startActivity(intent);
						retval = true;
					}
				} else {
					Log.d(Global.DEBUG_LOG, "-----------------> 4");
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					intent.setComponent(null);
					try {

						Log.d(Global.DEBUG_LOG, "-----------------> 5 : ");
						if (mActivity.startActivityIfNeeded(intent, -1)) {
							retval = true;
							Log.d(Global.DEBUG_LOG, "-----------------> 6");
						}
					} catch (ActivityNotFoundException ex) {
						Log.d(Global.DEBUG_LOG, "-----------------> 7");
						retval = false;
					}
				}
			} else { // 구 방식
				Log.d(Global.DEBUG_LOG, "-----------------> 8");
				Uri uri = Uri.parse(url);
				intent = new Intent(Intent.ACTION_VIEW, uri);
				mContext.startActivity(intent);
				retval = true;
			}
			return retval;
		} catch (ActivityNotFoundException e) {
			Log.d(Global.DEBUG_LOG, e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	//비동기 File Download 구현
	private void downloadFile(String mUrl) {
		new DownloadFileTask().execute(mUrl);
	}

	//AsyncTask<Params,Progress,Result>
	private class DownloadFileTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			URL myFileUrl = null;
			try {
				myFileUrl = new URL(urls[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			try {
				HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();

				// 다운 받는 파일의 경로는 sdcard/ 에 저장되며 sdcard에 접근하려면 uses-permission에 android.permission.WRITE_EXTERNAL_STORAGE을 추가해야만 가능.
				String mPath = "sdcard/v3mobile.apk";
				FileOutputStream fos;
				File f = new File(mPath);
				if (f.createNewFile()) {
					fos = new FileOutputStream(mPath);
					int read;
					while ((read = is.read()) != -1) {
						fos.write(read);
					}
					fos.close();
				}

				return "v3mobile.apk";
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}

		@Override
		protected void onPostExecute(String filename) {
			if (!"".equals(filename)) {
				Toast.makeText(mContext.getApplicationContext(), "download complete", Toast.LENGTH_SHORT).show();
				// 안드로이드 패키지 매니저를 사용한 어플리케이션 설치.
				File apkFile = new File(Environment.getExternalStorageDirectory() + "/" + filename);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
				mContext.startActivity(intent);
			}
		}
	}

	// 카드사 APP (ISP) / 계좌이체 APP 설치 여부 확인 - App 체크 메소드
	public static boolean isPackageInstalled(Context ctx, String pkgName) {
		try {
			ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		//존재:true, 존재하지않음:false
	}

	public void showAlert(String message, String positiveButton, DialogInterface.OnClickListener positiveListener, String negativeButton, DialogInterface.OnClickListener negativeListener) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setMessage(message);
		alert.setPositiveButton(positiveButton, positiveListener);
		alert.setNegativeButton(negativeButton, negativeListener);
		alert.show();
	}

//	private boolean startActivityIfNeeded(Intent intent, int i) {
//		try {
//			//String packageName = intent.getPackage();
//			//Intent goIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
//			//mContext.startActivity(goIntent);
//			mContext.startActivity(intent);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
	//비동기 File Download 구현
	private void downloadDefaultFile(String mUrl, String mFilename) {
		try {
			Log.d(Global.DEBUG_LOG,"filename : " + mFilename);
			Log.d(Global.DEBUG_LOG,"filename : " + URLDecoder.decode(mFilename, "utf-8"));
			Log.d(Global.DEBUG_LOG,"filename : " + URLDecoder.decode(mFilename, "euckr"));

			String filenameformat = URLDecoder.decode(mFilename, "utf-8");

			new DownloadDefaultFileTask().execute(mUrl, filenameformat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownloadDefaultFileTask extends AsyncTask<String, String, String> {
		private String mDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
		private ProgressDialog progressDialog = null;
		private boolean IS_DOINBACKGROUND_PAUSE = false;
		private boolean IS_NOT_DOWNLOAD = false;
		private boolean IS_RENAME_DOWNLOAD = false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(mContext);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("다운로드 준비중...");
			progressDialog.show();
		}
		@Override
		protected String doInBackground(String... urls) {
			URL myFileUrl = null;
			String mFilename = "";
			try {
				myFileUrl = new URL(urls[0]);
				mFilename = urls[1];

				publishProgress("message", mFilename + "\n" +"다운로드 중...");

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			try {
				HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();

				File chkDir = new File(mDir);
				if(!chkDir.exists())
					chkDir.mkdir();

				String mPath = mDir+"/"+mFilename;
				FileOutputStream fos;
				File f = new File(mPath);

				if(f.exists()) {
					publishProgress("alert",mFilename);
					pauseDoInBackground();
				}

				if(IS_NOT_DOWNLOAD) {

					//다운로드 하지 않음

				} else {

					if(IS_RENAME_DOWNLOAD) {
						String format = mFilename.substring(mFilename.lastIndexOf("."), mFilename.length());
						String filenameNotFormat = mFilename.replace(format, "");
						int filecount = 0;
						File chkFile = null;
						String refilename = "";
						while (!chkCreateNewFile(chkFile) && filecount < 9999) {
							filecount++;
							refilename = filenameNotFormat +"("+ filecount +")"+ format;
							chkFile = new File(mDir, refilename);
						}
						mPath = mDir+"/"+refilename;
						f = new File(mPath);
					}

					if (f.createNewFile()) {
						fos = new FileOutputStream(mPath);
						int read;
						while ((read = is.read()) != -1) {
							fos.write(read);
						}
						fos.close();
					}

				}

				return f.getName();
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}


		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			String header = values[0];
			if(header.equalsIgnoreCase("message")) {

				String msg = values[1];
				progressDialog.setMessage(msg);

			} else if(header.equalsIgnoreCase("alert")) {

				String filename = values[1];
				String mPath = mDir+"/"+filename;
				final File f = new File(mPath);

				new AlertDialog.Builder(mContext)
						.setMessage("동일한 이름의 파일이 존재합니다.")
						.setPositiveButton("삭제 후 저장", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								f.delete();
								resumeDoInBackground();
							}
						})
						.setNeutralButton("새이름 저장", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								IS_RENAME_DOWNLOAD = true;
								resumeDoInBackground();
							}
						})
						.setNegativeButton("저장안함", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								IS_NOT_DOWNLOAD = true;
								resumeDoInBackground();
							}
						})
						.show();
			}
		}

		@Override
		protected void onPostExecute(String filename) {
			progressDialog.dismiss();

			if(IS_NOT_DOWNLOAD) {
				new AlertDialog.Builder(mContext).setMessage("다운로드가 취소되었습니다.\n파일이름 : "+filename).setPositiveButton("확인",null).show();
			} else {
				if (!"".equals(filename)) {
					if(Global.IS_USE_PROCESS_SHOW_TOAST_LOG) {
						Toast.makeText(mContext.getApplicationContext(), "[ " + filename + " ] 다운로드 완료", Toast.LENGTH_SHORT).show();
					}
					new AlertDialog.Builder(mContext).setMessage("다운로드 완료\n파일이름 : " + filename).setPositiveButton("확인", null).show();
				} else {
					if(Global.IS_USE_PROCESS_SHOW_TOAST_LOG) {
						Toast.makeText(mContext.getApplicationContext(), "[ " + filename + " ] 다운로드 실패", Toast.LENGTH_SHORT).show();
					}
					new AlertDialog.Builder(mContext).setMessage("일시적으로 다운로드가 실패하였습니다.\n다시 시도해 주세요.\n파일이름 : " + filename).setPositiveButton("확인", null).show();
				}
			}


			IS_DOINBACKGROUND_PAUSE = false;
			IS_NOT_DOWNLOAD = false;
			IS_RENAME_DOWNLOAD = false;

		}


		public void pauseDoInBackground() {
			IS_DOINBACKGROUND_PAUSE = true;
			do {
				SystemClock.sleep(100);
			} while(IS_DOINBACKGROUND_PAUSE);
		}


		public void resumeDoInBackground() {
			IS_DOINBACKGROUND_PAUSE = false;
		}


		private boolean chkCreateNewFile(File f) {
			try {
				return f.createNewFile(); //존재하는 파일이 아니면
			}catch (Exception e) {
				return false;
			}
		}
	}
}
