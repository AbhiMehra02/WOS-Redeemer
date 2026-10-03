package com.wos.giftredeemer;
import org.json.JSONObject;
public final class ResponseMessages {
 public static String explain(JSONObject r){int c=r.optInt("err_code",Integer.MIN_VALUE); switch(c){
  case 20000:return "you beggar, take it... 🎁"; case 40008:return "god bless your poor soul... 🙏";
  case 40011:return "you came again? you greedy bastard... 💀"; case 40007:return "it's expired... just like you. 💀";
  default:return "🤡 check your pockets: "+r.optString("msg","Unknown response")+" ("+(c==Integer.MIN_VALUE?"null":c)+")";}}
 private ResponseMessages(){}
}
