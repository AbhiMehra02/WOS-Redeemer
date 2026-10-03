package com.wos.giftredeemer;
import java.util.*;
public final class GiftCodeParser {
 public static List<String> activeFromVisibleText(String text){
  String[] raw=text.replace("\r","").split("\n");List<String> lines=new ArrayList<>();for(String s:raw){s=s.trim();if(!s.isEmpty())lines.add(s);}
  int start=-1;for(int i=0;i<lines.size();i++)if(lines.get(i).equalsIgnoreCase("Known Gift Codes")){start=i+1;break;} if(start<0)return Collections.emptyList();
  LinkedHashSet<String> out=new LinkedHashSet<>();for(int i=start;i+1<lines.size();i++){
   String code=lines.get(i), next=lines.get(i+1); if(i>start && (code.equalsIgnoreCase("Auto-Redeem")||code.equalsIgnoreCase("Auto Redeem")||code.equalsIgnoreCase("How to Redeem")))break;
   if(!code.matches("[A-Za-z0-9]+"))continue;if(code.equalsIgnoreCase("active")||code.equalsIgnoreCase("expired")||code.equalsIgnoreCase("copy"))continue;
   if(next.equalsIgnoreCase("active"))out.add(code);
  } return new ArrayList<>(out);
 }
 private GiftCodeParser(){}
}
