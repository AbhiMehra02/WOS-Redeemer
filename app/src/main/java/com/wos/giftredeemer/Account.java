package com.wos.giftredeemer;
public final class Account {
 public final long id; public final String name,playerId,state; public final boolean selected;
 public Account(long id,String name,String playerId,String state,boolean selected){this.id=id;this.name=name;this.playerId=playerId;this.state=state;this.selected=selected;}
 public String label(){return name==null||name.trim().isEmpty()?"Player "+playerId:name;}
}
