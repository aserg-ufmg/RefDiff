package org.apache.cordova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/**
 * This class is the main Android activity that represents the Cordova
 * 
 * @@ -81,114 +73,26 @@ Licensed to the Apache Software Foundation (ASF) under
 * one
 * deprecated in favor of the config.xml file.
 *
 */
public class CordovaActivity extends Activity implements CordovaInterface {
  public static String TAG = "CordovaActivity";

  // The webview for our app
  protected CordovaWebView appView;

  @Deprecated // unused.
  protected CordovaWebViewClient webViewClient;

  @Deprecated // Will be removed. Use findViewById() to retrieve views.
  protected LinearLayout root;

  private final ExecutorService threadPool = Executors.newCachedThreadPool();

  private static int ACTIVITY_STARTING = 0;
  private static int ACTIVITY_RUNNING = 1;
  private static int ACTIVITY_EXITING = 2;
  private int activityState = 0; // 0=starting, 1=running (after 1st resume), 2=shutting down

  // Plugin to call when activity result is received
  protected int activityResultRequestCode;
  protected CordovaPlugin activityResultCallback;
  protected boolean activityResultKeepRunning;

  /*
   * The variables below are used to cache some of the activity properties.
   */

  // Draw a splash screen using an image located in the drawable resource
  // directory.
  @Deprecated // Use "SplashScreen" preference instead.
  protected int splashscreen = 0;
  @Deprecated // Use "SplashScreenDelay" preference instead.
  protected int splashscreenTime = -1;

  // LoadUrl timeout value in msec (default of 20 sec)
  protected int loadUrlTimeoutValue = 20000;

  // Keep app running when pause is received. (default = true)
  // If true, then the JavaScript and native code continue to run in the
  // background
  // when another application (activity) is started.
  protected boolean keepRunning = true;

  private String initCallbackClass;

  // Read from config.xml:
  protected CordovaPreferences preferences;
  protected Whitelist internalWhitelist;
  protected Whitelist externalWhitelist;
  protected String launchUrl;
  protected ArrayList<PluginEntry> pluginEntries;

  /**
   * Sets the authentication token.
   *
   * @param authenticationToken
   * @param host
   * @param realm
   */
  public void setAuthenticationToken(AuthenticationToken authenticationToken, String host, String realm) {
    if (this.appView != null && this.appView.viewClient != null) {
      this.appView.viewClient.setAuthenticationToken(authenticationToken, host, realm);
    }
  }

  /**
   * Removes the authentication token.
   *
   * @param host
   * @param realm
   *
   * @return the authentication token or null if did not exist
   */
  public AuthenticationToken removeAuthenticationToken(String host, String realm) {
    if (this.appView != null && this.appView.viewClient != null) {
      return this.appView.viewClient.removeAuthenticationToken(host, realm);
    }
    return null;
  }

  /**
   * Gets the authentication token.
   *
   * In order it tries:
   * 1- host + realm
   * 2- host
   * 3- realm
   * 4- no host, no realm
   *
   * @param host
   * @param realm
   *
   * @return the authentication token
   */
  public AuthenticationToken getAuthenticationToken(String host, String realm) {
    if (this.appView != null && this.appView.viewClient != null) {
      return this.appView.viewClient.getAuthenticationToken(host, realm);
    }
    return null;
  }

  /**
   * Clear all authentication tokens.
   */
  public void clearAuthenticationTokens() {
    if (this.appView != null && this.appView.viewClient != null) {
      this.appView.viewClient.clearAuthenticationTokens();
    }
  }

  /**
   * Called when the activity is first created.
   * 
   * @@ -220,9 +124,25 @@ public void onCreate(Bundle savedInstanceState) {
   * 
   * super.onCreate(savedInstanceState);
   * 
   * 
   * if(savedInstanceState != null)
   * {
   * initCallbackClass = savedInstanceState.getString("callbackClass");
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * }
   * }
   * 
   * @@ -232,126 +152,52 @@ protected void loadConfig() {
   * parser.parse(this);
   * preferences = parser.getPreferences();
   * preferences.setPreferencesBundle(getIntent().getExtras());
   * preferences.copyIntoIntentExtras(this);
   * internalWhitelist = parser.getInternalWhitelist();
   * externalWhitelist = parser.getExternalWhitelist();
   * launchUrl = parser.getLaunchUrl();
   * pluginEntries = parser.getPluginEntries();
   * Config.parser = parser;
   * }
   * 
   * @SuppressWarnings("deprecation")
   * 
   * protected void createViews() {
   * // This builds the view. We could probably get away with NOT having a
   * LinearLayout, but I like having a bucket!
   * 
   * LOG.d(TAG, "CordovaActivity.createViews()");
   * 
   * Display display = getWindowManager().getDefaultDisplay();
   * int width = display.getWidth();
   * int height = display.getHeight();
   * 
   * root = new LinearLayoutSoftKeyboardDetect(this, width, height);
   * root.setOrientation(LinearLayout.VERTICAL);
   * root.setLayoutParams(new
   * LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
   * ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
   * 
   * appView.setId(100);
   * appView.setLayoutParams(new LinearLayout.LayoutParams(
   * ViewGroup.LayoutParams.MATCH_PARENT,
   * ViewGroup.LayoutParams.MATCH_PARENT,
   * 1.0F));
   * 
   * // need to remove appView from any existing parent before invoking
   * root.addView(appView)
   * ViewParent parent = appView.getParent();
   * if ((parent != null) && (parent != root)) {
   * LOG.d(TAG, "removing appView from existing parent");
   * ViewGroup parentGroup = (ViewGroup) parent;
   * parentGroup.removeView(appView);
   * }
   * root.addView((View) appView);
   * setContentView(root);
   * 
   * int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
   * root.setBackgroundColor(backgroundColor);
   * }
   * 
   * /**
   * Get the Android activity.
   */
  @Override
  public Activity getActivity() {
    return this;
  }

  /**
   * Construct the default web view object.
   *
   * This is intended to be overridable by subclasses of CordovaIntent which
   * require a more specialized web view.
   */
  protected CordovaWebView makeWebView() {
    return new CordovaWebView(CordovaActivity.this);
  }

  /**
   * Construct the client for the default web view object.
   *
   * This is intended to be overridable by subclasses of CordovaIntent which
   * require a more specialized web view.
   *
   * @param webView the default constructed web view object
   */
  protected CordovaWebViewClient makeWebViewClient(CordovaWebView webView) {
    return webView.makeWebViewClient(this);
  }

  /**
   * Construct the chrome client for the default web view object.
   *
   * This is intended to be overridable by subclasses of CordovaIntent which
   * require a more specialized web view.
   *
   * @param webView the default constructed web view object
   */
  protected CordovaChromeClient makeChromeClient(CordovaWebView webView) {
    return webView.makeWebChromeClient(this);
  }

  public void init() {
    this.init(appView, null, null);
  }

  @SuppressLint("NewApi")
  @Deprecated // Call init() instead and override makeWebView() to customize.
  public void init(CordovaWebView webView, CordovaWebViewClient webViewClient, CordovaChromeClient webChromeClient) {
    LOG.d(TAG, "CordovaActivity.init()");

    if (splashscreenTime >= 0) {
      preferences.set("SplashScreenDelay", splashscreenTime);
    }
    if (splashscreen != 0) {
      preferences.set("SplashDrawableId", splashscreen);
    }

    appView = webView != null ? webView : makeWebView();

    // TODO: Have the views set this themselves.
    if (preferences.getBoolean("DisallowOverscroll", false)) {
      appView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    createViews();

    // Init plugins only after creating views
    if (appView.pluginManager == null) {
      appView.init(this, webViewClient != null ? webViewClient : makeWebViewClient(appView),
          webChromeClient != null ? webChromeClient : makeChromeClient(appView),
          pluginEntries, internalWhitelist, externalWhitelist, preferences);
    }

    // Wire the hardware volume controls to control media if desired.
    String volumePref = preferences.getString("DefaultVolumeStream", "");
    if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
      setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
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
   * 
   * /**
   * Load the url into the webview after waiting for period of time.
   * This is used to display the splashscreen for certain amount of time.
   *
   * @param url
   * @param time The number of ms to wait before loading webview
   */
  @Deprecated // Use loadUrl(String url) instead.
  public void loadUrl(final String url, int time) {

    this.splashscreenTime = time;
    this.loadUrl(url);
  }

  @Deprecated
  public void cancelLoadUrl() {
  }

  /**
   * Clear the resource cache.
   */
  @Deprecated // Call method on appView directly.
  public void clearCache() {
    if (appView == null) {
      init();
    }
    this.appView.clearCache(true);
  }

  /**
   * Clear web history in this web view.
   */
  @Deprecated // Call method on appView directly.
  public void clearHistory() {
    this.appView.clearHistory();
  }

  /**
   * Go to previous page in history. (We manage our own history)
   *
   * @return true if we went back, false if we are already at top
   */
  @Deprecated // Call method on appView directly.
  public boolean backHistory() {
    if (this.appView != null) {
      return appView.backHistory();
    }
    return false;
  }

  /**
   * Get boolean property for activity.
   */
  @Deprecated // Call method on preferences directly.
  public boolean getBooleanProperty(String name, boolean defaultValue) {
    return preferences.getBoolean(name, defaultValue);
  }

  /**
   * Get int property for activity.
   */
  @Deprecated // Call method on preferences directly.
  public int getIntegerProperty(String name, int defaultValue) {
    return preferences.getInteger(name, defaultValue);
  }

  /**
   * Get string property for activity.
   */
  @Deprecated // Call method on preferences directly.
  public String getStringProperty(String name, String defaultValue) {
    return preferences.getString(name, defaultValue);
  }

  /**
   * Get double property for activity.
   */
  @Deprecated // Call method on preferences directly.
  public double getDoubleProperty(String name, double defaultValue) {
    return preferences.getDouble(name, defaultValue);
  }

  /**
   * Set boolean property on activity.
   * This method has been deprecated in 3.0 and will be removed at a future
   * time. Please use config.xml instead.
   *
   * @param name
   * @param value
   * @deprecated
   */
  @Deprecated
  public void setBooleanProperty(String name, boolean value) {
    Log.d(TAG,
        "Setting boolean properties in CordovaActivity will be deprecated in 3.0 on July 2013, please use config.xml");
    this.getIntent().putExtra(name.toLowerCase(), value);
  }

  /**
   * Set int property on activity.
   * This method has been deprecated in 3.0 and will be removed at a future
   * time. Please use config.xml instead.
   *
   * @param name
   * @param value
   * @deprecated
   */
  @Deprecated
  public void setIntegerProperty(String name, int value) {
    Log.d(TAG,
        "Setting integer properties in CordovaActivity will be deprecated in 3.0 on July 2013, please use config.xml");
    this.getIntent().putExtra(name.toLowerCase(), value);
  }

  /**
   * Set string property on activity.
   * This method has been deprecated in 3.0 and will be removed at a future
   * time. Please use config.xml instead.
   *
   * @param name
   * @param value
   * @deprecated
   */
  @Deprecated
  public void setStringProperty(String name, String value) {
    Log.d(TAG,
        "Setting string properties in CordovaActivity will be deprecated in 3.0 on July 2013, please use config.xml");
    this.getIntent().putExtra(name.toLowerCase(), value);
  }

  /**
   * Set double property on activity.
   * This method has been deprecated in 3.0 and will be removed at a future
   * time. Please use config.xml instead.
   *
   * @param name
   * @param value
   * @deprecated
   */
  @Deprecated
  public void setDoubleProperty(String name, double value) {
    Log.d(TAG,
        "Setting double properties in CordovaActivity will be deprecated in 3.0 on July 2013, please use config.xml");
    this.getIntent().putExtra(name.toLowerCase(), value);
  }

  /**
   * Called when the system is about to start resuming a previous activity.
   */
  @Override
  protected void onPause() {
    super.onPause();

    LOG.d(TAG, "Paused the application!");

    // Don't process pause if shutting down, since onDestroy() will be called
    if (this.activityState == ACTIVITY_EXITING) {
      return;
    }

    if (this.appView == null) {
      return;
    } else {
      this.appView.handlePause(this.keepRunning);
    }
  }

  protected void onNewIntent(Intent intent) {

  @Override
  protected void onResume() {
    super.onResume();
    LOG.d(TAG, "Resuming the App");

    if (this.activityState == ACTIVITY_STARTING) {
      this.activityState = ACTIVITY_RUNNING;
      return;
    }

    if (this.appView == null) {
      return;
    }
    // Force window to have focus, so application always
    // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at
    // least)
    this.getWindow().getDecorView().requestFocus();

    this.appView.handleResume(this.keepRunning, this.activityResultKeepRunning);

    // If app doesn't want to run in background
    if (!this.keepRunning || this.activityResultKeepRunning) {

      // Restore multitasking state
      if (this.activityResultKeepRunning) {
        this.keepRunning = this.activityResultKeepRunning;
        this.activityResultKeepRunning = false;
      }
    }
  }

  /**
   * The final call you receive before your activity is destroyed.
   */
  @Override
  public void onDestroy() {
    LOG.d(TAG, "CordovaActivity.onDestroy()");
    super.onDestroy();

    if (this.appView != null) {
      appView.handleDestroy();
    } else {
      this.activityState = ACTIVITY_EXITING;
    }

  }

  /**
   * Send a message to all plugins.
   */
  public void postMessage(String id, Object data) {
    if (this.appView != null) {
      this.appView.postMessage(id, data);
    }
  }

  /**
   * @deprecated
   *             Add services to res/xml/plugins.xml instead.
   *
   *             Add a class that implements a service.
   */
  @Deprecated
  public void addService(String serviceType, String className) {
    if (this.appView != null && this.appView.pluginManager != null) {
      this.appView.pluginManager.addService(serviceType, className);
    }
  }

  /**
   * Send JavaScript statement back to JavaScript.
   * (This is a convenience method)
   *
   * @param statement
   */
  @Deprecated // Call method on appView directly.
  public void sendJavascript(String statement) {
    if (this.appView != null) {
      this.appView.bridge.getMessageQueue().addJavaScript(statement);
    }

  }

  /**
   * Show the spinner. Must be called from the UI thread.
   *
   * @param title   Title of the dialog
   * @param message The message of the dialog
   */
  @Deprecated // Call this directly on SplashScreen plugin instead.
  public void spinnerStart(final String title, final String message) {
    JSONArray args = new JSONArray();
    args.put(title);
    args.put(message);
    doSplashScreenAction("spinnerStart", args);
  }

  /**
   * Stop spinner - Must be called from UI thread
   */
  @Deprecated // Call this directly on SplashScreen plugin instead.
  public void spinnerStop() {
    doSplashScreenAction("spinnerStop", null);
  }

  /**
     * End this activity by calling finish for activity
     */
    public void endActivity() {
        this.activityState = ACTIVITY_EXITING;
        super.finish();
    };
  }
}