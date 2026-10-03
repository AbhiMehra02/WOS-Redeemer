package com.wos.giftredeemer;
import android.annotation.SuppressLint;import android.app.Activity;import android.graphics.Bitmap;import android.os.Handler;import android.os.Looper;import android.webkit.*;import java.util.*;
public final class GiftCodeScraper {
 public interface Callback{void success(List<String> codes);void error(String message);}
 private final Activity activity;private WebView webView;private boolean done;
 public GiftCodeScraper(Activity a){activity=a;}
 @SuppressLint("SetJavaScriptEnabled") public void fetch(Callback cb){destroy();done=false;webView=new WebView(activity);WebSettings s=webView.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setUserAgentString("Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Mobile Safari/537.36");
  webView.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String url){new Handler(Looper.getMainLooper()).postDelayed(()->extract(cb),1800);}@Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame())fail(cb,"Page load failed: "+e.getDescription());}});webView.loadUrl("https://wostools.net/gift-codes");
  new Handler(Looper.getMainLooper()).postDelayed(()->{if(!done)fail(cb,"Gift-code page timed out");},15000);
 }
 private void extract(Callback cb){if(done||webView==null)return;webView.evaluateJavascript("(function(){return document.body?document.body.innerText:'';})()",value->{try{String text=decodeJsString(value);List<String> codes=GiftCodeParser.activeFromVisibleText(text);if(codes.isEmpty()){fail(cb,"Rendered page loaded, but no active codes were found in the Known Gift Codes section.");return;}done=true;destroy();cb.success(codes);}catch(Exception e){fail(cb,"Could not parse rendered page: "+e.getMessage());}});}
 private static String decodeJsString(String v)throws Exception{if(v==null||v.equals("null"))return"";return new org.json.JSONTokener(v).nextValue().toString();}
 private void fail(Callback cb,String m){if(done)return;done=true;destroy();cb.error(m);} public void destroy(){if(webView!=null){webView.stopLoading();webView.destroy();webView=null;}}
}
