package org.apache.cordova;

import java.util.ArrayList;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * This class is the main Android activity that represents the Cordova
 * 
 * @@ -81,114 +73,26 @@ Licensed to the Apache Software Foundation (ASF) under
 * one
 * deprecated in favor of the config.xml file.
 *
 */
public class CordovaActivity extends Activity {
  public static String TAG = "CordovaActivity";

  // The webview for our app
  protected CordovaWebView appView;

  private static int ACTIVITY_STARTING = 0;
  private static int ACTIVITY_RUNNING = 1;
  private static int ACTIVITY_EXITING = 2;

  // Keep app running when pause is received. (default = true)
  // If true, then the JavaScript and native code continue to run in the
  // background
  // when another application (activity) is started.
  protected boolean keepRunning = true;

  // Read from config.xml:
  protected CordovaPreferences preferences;

  protected String launchUrl;
  protected ArrayList<PluginEntry> pluginEntries;
  protected CordovaInterfaceImpl cordovaInterface;

  /**
   * Called when the activity is first created.
   * 
   * @@ -220,9 +124,25 @@ public void onCreate(Bundle savedInstanceState) {
   * 
   * super.onCreate(savedInstanceState);
   * 
   * cordovaInterface = makeCordovaInterface();
   * if(savedInstanceState != null)
   * {
   * cordovaInterface.restoreInstanceState(savedInstanceState);
   * }
   * }
   * 
   * protected void init() {
   * appView = makeWebView();
   * createViews();
   * if (!appView.isInitialized()) {
   * appView.init(cordovaInterface, pluginEntries, preferences);
   * }
   * cordovaInterface.onCordovaInit(appView.getPluginManager());
   * 
   * // Wire the hardware volume controls to control media if desired.
   * String volumePref = preferences.getString("DefaultVolumeStream", "");
   * if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
   * setVolumeControlStream(AudioManager.STREAM_MUSIC);
   * }
   * }
   * 
   * @@ -232,126 +152,52 @@ protected void loadConfig() {
   * parser.parse(this);
   * preferences = parser.getPreferences();
   * preferences.setPreferencesBundle(getIntent().getExtras());
   * 
   * 
   * 
   * launchUrl = parser.getLaunchUrl();
   * pluginEntries = parser.getPluginEntries();
   * Config.parser = parser;
   * }
   * 
   * //Suppressing warnings in AndroidStudio
   * @SuppressWarnings({"deprecation", "ResourceType"})
   * protected void createViews() {
   * //Why are we setting a constant as the ID? This should be investigated
   * appView.getView().setId(100);
   * appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
   * ViewGroup.LayoutParams.MATCH_PARENT,
   * ViewGroup.LayoutParams.MATCH_PARENT));
   * 
   * setContentView(appView.getView());
   * 
   * if (preferences.contains("BackgroundColor")) {
   * int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
   * // Background of activity:
   * appView.getView().setBackgroundColor(backgroundColor);
   * appView.getView().requestFocusFromTouch();
   * }
   * 
   * /**
   * Construct the default web view object.
   *
   * Override this to customize the webview that is used.
   * 
   */
  protected CordovaWebView makeWebView() {
    return new CordovaWebViewImpl(makeWebViewEngine());

  }

  protected CordovaWebViewEngine makeWebViewEngine() {
    return CordovaWebViewImpl.createEngine(this, preferences);
  }

  protected CordovaInterfaceImpl makeCordovaInterface() {
    return new CordovaInterfaceImpl(this) {
      @Override
      public Object onMessage(String id, Object data) {
        // Plumb this to CordovaActivity.onMessage for backwards compatibility
        return CordovaActivity.this.onMessage(id, data);
      }
    };

  }

  /**
   * @@ -361,173 +207,22 @@ public void loadUrl(String url) {
   * if (appView == null) {
   * init();
   * }
   * 
   * // If keepRunning
   * this.keepRunning = preferences.getBoolean("KeepRunning", true);
   * 
   * appView.loadUrlIntoView(url, true);
   * }
   * /**
   * Called when the system is about to start resuming a previous activity.
   */
  @Override
  protected void onPause() {
    super.onPause();
    LOG.d(TAG, "Paused the activity.");

    if (this.appView != null) {

      this.appView.handlePause(this.keepRunning);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    LOG.d(TAG, "Resumed the activity.");

    if (this.appView == null) {
      return;
    }
    // Force window to have focus, so application always
    // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at
    // least)
    this.getWindow().getDecorView().requestFocus();

    this.appView.handleResume(this.keepRunning);

  }

  /**
   * Called when the activity is no longer visible to the user.
   */
  @Override
  protected void onStop() {
    super.onStop();
    LOG.d(TAG, "Stopped the activity.");

    if (this.appView == null) {
      return;

    }
    this.appView.handleStop();
  }

  /**
   * Called when the activity is becoming visible to the user.
   */
  @Override
  protected void onStart() {
    super.onStart();
    LOG.d(TAG, "Started the activity.");

    if (this.appView == null) {
      return;

    }
    this.appView.handleStart();
  }

  /**
   * The final call you receive before your activity is destroyed.
   * 
   * 
   */
  @Override
  public void onDestroy() {
    LOG.d(TAG, "CordovaActivity.onDestroy()");
    super.onDestroy();

    if (this.appView != null) {
      appView.handleDestroy();

    }
  }

  @Override
  public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
    // Capture requestCode here so that it is captured in the
    // setActivityResultCallback() case.
    cordovaInterface.setActivityResultRequestCode(requestCode);
    super.startActivityForResult(intent, requestCode, options);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    LOG.d(TAG, "Incoming Result. Request code = " + requestCode);
    super.onActivityResult(requestCode, resultCode, intent);
    cordovaInterface.onActivityResult(requestCode, resultCode, intent);

  }

  /**
   * @@ -741,8 +333,7 @@ public void onReceivedError(final int errorCode, final
   * String description, final
   * 
   * // If errorUrl specified, then load it
   * final String errorUrl = preferences.getString("errorUrl", null);
   * if ((errorUrl != null) && (!failingUrl.equals(errorUrl)) && (appView !=
   * null)) {
   * 
   * // Load URL on UI thread
   * me.runOnUiThread(new Runnable() {
   * public void run() {
   * @@ -756,7 +347,7 @@ public void run() {
   * me.runOnUiThread(new Runnable() {
   * public void run() {
   * if (exit) {
   * me.appView.getView().setVisibility(View.GONE);
   * me.displayError("Application Error", description + " (" + failingUrl + ")",
   * "OK", exit);
   * }
   * }
   * @@ -781,7 +372,7 @@ public void run() {
   * public void onClick(DialogInterface dialog, int which) {
   * dialog.dismiss();
   * if (exit) {
   * finish();
   * }
   * }
   * });
   * @@ -794,121 +385,33 @@ public void onClick(DialogInterface dialog, int which)
   * {
   * });
   * }
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * /*
   * Hook in Cordova for menu plugins
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (appView != null) {
      appView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (appView != null) {
      appView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
    }
    return true;
  }

  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onOptionsItemSelected", item);
        }
        return true;
    }
  }
