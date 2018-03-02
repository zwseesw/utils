package com.zmaitech.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lvsidiqiu.erp.scoremanager.R;
import com.lvsidiqiu.erp.scoremanager.ui.view.TlDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

public class AndroidUtils {

    /**
     * Returns version code.
     *
     * @return
     */
    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * install apk file
     * params Context
     * params String  The apk file path
     */
    public static void installApp(Context context, String dir, String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = getUriFromFile(context,new File(FileUtils.getFullPath(context, dir + File.separator + fileName)));
        intent.setDataAndType(uri
                , "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    /**
     * Returns version name.
     *
     * @return
     */
    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
        return (int) scale;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static View getViewByLayoutId(Context context, int layoutId) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(layoutId, null);
        return layout;
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return new int[]{width, height};
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static void keepViewRatioWithScreenWidth(Context context, View v, double ratio) {
        int w = AndroidUtils.getScreenSize((Activity) context)[0];
        keepViewRatioWithWidth(context, v, w, ratio);
//    	int h = (int)(w*ratio);
//    	final float scale = context.getResources().getDisplayMetrics().density; 
//		ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
//		layoutParams.height =(int)(h/scale);
    }

    public static void keepViewRatioWithScreenWidthAndMargin(Context context, View v, int margin, double ratio) {
        int w = AndroidUtils.getScreenSize((Activity) context)[0] - dip2px(context, 2 * margin);
        keepViewRatioWithWidth(context, v, w, ratio);
    }

    public static void keepViewRatioWithWidth(Context context, View v, int w, double ratio) {
        int h = (int) (w * ratio);
//    	final float scale = context.getResources().getDisplayMetrics().density; 
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = w;
        layoutParams.height = h;//(int)(h/scale);
    }

    public static void startCameraActivity(Activity activity, String path, int requestCode) {
        path = FileUtils.getFullPath(activity, path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(path);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,getUriFromFile(activity,file));
        activity.startActivityForResult(intent, requestCode);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideKeyboard(Activity activity,EditText et) {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(et.getWindowToken(),0);
    }

    public static void showKeyboard(Activity activity, EditText et) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive(et)) {
			imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		}
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isKeyboardOpen(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputManager.isActive();
    }

    public static void makeCall(final Context context, final String phone) {
		Log.e("infoo电话",phone);
        if (TextUtils.isEmpty(phone)) {
            return;
        }

        new TlDialog.Builder(context)
                .setTitle(R.string.prompt)
                .setMessage(phone)
                .setPositiveButton(R.string.call, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String phone_no = phone;
                        phone_no = phone_no.replaceAll("[^0-9|\\+]", "");
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + phone_no));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(callIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static void Vibrate(final Activity activity, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    public static String getDeviceId(final Activity activity) {
        String identifier = null;
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null)
            identifier = tm.getDeviceId();
        if (identifier == null || identifier.length() == 0)
            identifier = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
        return identifier;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void updateBackgroundResourceWithRetainedPadding(View view, int resourceID) {
        int bottom = view.getPaddingBottom();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int left = view.getPaddingLeft();
        view.setBackgroundResource(resourceID);
        view.setPadding(left, top, right, bottom);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    //设置圆形imageview
    public static void setCircleImageView(String url, final ImageView imageView) {
        new AsyncTask<String, String, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(params[0]);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(AndroidUtils.getRoundedCornerBitmap(bitmap, bitmap.getWidth() / 2));
                }
            }
        }.execute(url);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

	private static PowerManager.WakeLock wakeLock;
	/**
	 * 防止系统熄屏 保持屏幕常亮	注意在ondestroy里面调用 keepScreenOn(context, false)
	 */
	public static void keepScreenOn(Context context, boolean on) {
		if (on) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "==KeepScreenOn==");
			wakeLock.acquire();
		} else {
			if (wakeLock != null) {
				wakeLock.release();
				wakeLock = null;
			}
		}
	}

    public static Uri getUriFromFile(Context context, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName()+".FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
