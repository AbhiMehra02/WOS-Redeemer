package com.wos.giftredeemer;
import org.json.JSONObject; import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.*;
public final class WosRedeemer {
 private static final String ENDPOINT="https://wos-giftcode-api.centurygame.com/api/gift_code", SALT="tB87#kPtkxqOS2"; private static final int TIMEOUT=15000;
 private static String enc(String v)throws Exception{return URLEncoder.encode(v,"UTF-8").replace("+","%20").replace("%21","!").replace("%27","'").replace("%28","(").replace("%29",")").replace("%7E","~").replace("%2A","*");}
 private static String md5(String s)throws Exception{byte[] b=MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte q:b)x.append(String.format(Locale.US,"%02x",q));return x.toString();}
 public static JSONObject redeem(String player,String state,String code)throws Exception{
  String time=String.valueOf(System.currentTimeMillis()/1000L); TreeMap<String,String>d=new TreeMap<>();d.put("fid",player);d.put("kid",state);d.put("cdk",code);d.put("time",time);
  StringBuilder raw=new StringBuilder();for(Map.Entry<String,String>e:d.entrySet()){if(raw.length()>0)raw.append('&');raw.append(e.getKey()).append('=').append(enc(e.getValue()));}
  String sign=md5(raw+SALT); String body="sign="+enc(sign)+"&fid="+enc(player)+"&kid="+enc(state)+"&cdk="+enc(code)+"&time="+time;
  HttpURLConnection c=(HttpURLConnection)new URL(ENDPOINT).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(TIMEOUT);c.setReadTimeout(TIMEOUT);c.setDoOutput(true);c.setUseCaches(false);
  c.setRequestProperty("Accept","application/json, text/plain, */*");c.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=utf-8");c.setRequestProperty("Origin","https://wos-giftcode.centurygame.com");c.setRequestProperty("Referer","https://wos-giftcode.centurygame.com/");c.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36");
  try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));} int http=c.getResponseCode();InputStream in=http>=400?c.getErrorStream():c.getInputStream();String response=read(in);c.disconnect();
  if(http>=400)throw new IOException("HTTP "+http+": "+response); if(response.trim().isEmpty())throw new IOException("HTTP "+http+" with empty response"); return new JSONObject(response);
 }
 private static String read(InputStream in)throws IOException{if(in==null)return"";try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder s=new StringBuilder();String l;while((l=r.readLine())!=null)s.append(l).append('\n');return s.toString();}}
 private WosRedeemer(){}
}
