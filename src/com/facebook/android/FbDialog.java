package com.facebook.android;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.android.Facebook.DialogListener;
import com.quanturium.bseries.R;

public class FbDialog extends Dialog
{

	static final int						FB_BLUE						= 0xFF6D84B4;
	static final float[]					DIMENSIONS_DIFF_LANDSCAPE	= { 20, 60 };
	static final float[]					DIMENSIONS_DIFF_PORTRAIT	= { 40, 60 };
	static final FrameLayout.LayoutParams	FILL						= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	static final int						MARGIN						= 4;
	static final int						PADDING						= 2;
	static final String						DISPLAY_STRING				= "touch";
	static final String						FB_ICON						= "icon.png";

	private String							mUrl;
	private DialogListener					mListener;
	private ProgressDialog					mSpinner;
	private ImageView						mCrossImage;
	private WebView							mWebView;
	private FrameLayout						mContent;

	public FbDialog(Context context, String url, DialogListener listener)
	{
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mUrl = url;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mSpinner = new ProgressDialog(getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Chargement");
		mSpinner.setCancelable(false);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContent = new FrameLayout(getContext());

		createCrossImage();

		int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
		setUpWebView(crossWidth / 2);

		mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	private void createCrossImage()
	{
		mCrossImage = new ImageView(getContext());
		// Dismiss the dialog when user click on the 'x'
		mCrossImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mListener.onCancel();
				FbDialog.this.dismiss();
			}
		});
		Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.close);
		mCrossImage.setImageDrawable(crossDrawable);
		/*
		 * 'x' should not be visible while webview is loading make it visible only after webview has fully loaded
		 */
		mCrossImage.setVisibility(View.INVISIBLE);
	}

	private void setUpWebView(int margin)
	{
		LinearLayout webViewContainer = new LinearLayout(getContext());
		mWebView = new WebView(getContext());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new FbWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(mUrl);
		mWebView.setLayoutParams(FILL);
		mWebView.setVisibility(View.INVISIBLE);

		webViewContainer.setPadding(margin, margin, margin, margin);
		webViewContainer.addView(mWebView);
		mContent.addView(webViewContainer);
	}

	private class FbWebViewClient extends WebViewClient
	{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			Util.logd("Facebook-WebView", "Redirect URL: " + url);
			if (url.startsWith(Facebook.REDIRECT_URI))
			{
				Bundle values = Util.parseUrl(url);

				String error = values.getString("error");
				if (error == null)
				{
					error = values.getString("error_type");
				}

				if (error == null)
				{
					mListener.onComplete(values);
				}
				else if (error.equals("access_denied") || error.equals("OAuthAccessDeniedException"))
				{
					mListener.onCancel();
				}
				else
				{
					mListener.onFacebookError(new FacebookError(error));
				}

				FbDialog.this.dismiss();
				return true;
			}
			else if (url.startsWith(Facebook.CANCEL_URI))
			{
				mListener.onCancel();
				FbDialog.this.dismiss();
				return true;
			}
			else if (url.contains(DISPLAY_STRING))
			{
				return false;
			}
			// launch non-dialog URLs in a full browser
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
		{
			super.onReceivedError(view, errorCode, description, failingUrl);
			mListener.onError(new DialogError(description, errorCode, failingUrl));
			FbDialog.this.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			Util.logd("Facebook-WebView", "Webview loading URL: " + url);
			super.onPageStarted(view, url, favicon);
			mSpinner.show();
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			super.onPageFinished(view, url);
			mSpinner.dismiss();

			mContent.setBackgroundColor(Color.TRANSPARENT);
			mWebView.setVisibility(View.VISIBLE);
			mCrossImage.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed()
	{
		mListener.onCancel();
		super.onBackPressed();
	}
}
