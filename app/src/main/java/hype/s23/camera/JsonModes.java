package hype.s23.camera;
import org.json.*;
import java.util.*;
final class JsonModes {
 static String merge(String original,String donor,Set<String> supported)throws JSONException{
  JSONArray base=new JSONArray(original), extra=new JSONArray(donor);boolean changed=false;
  for(int i=0;i<extra.length();i++){
   JSONObject size=extra.getJSONObject(i);String res=size.getString("mValue");JSONArray fps=size.getJSONArray("mFpsModeList");JSONObject dest=null;
   for(int j=0;j<base.length();j++){JSONObject candidate=base.getJSONObject(j);if(res.equals(candidate.getString("mValue"))){dest=candidate;break;}}
   for(int j=0;j<fps.length();j++){
    JSONObject entry=fps.getJSONObject(j);String rate=entry.getString("mFpsValue");if(!supported.contains(res+"@"+rate))continue;
    if(dest==null){dest=new JSONObject(size.toString());dest.put("mFpsModeList",new JSONArray());base.put(dest);}
    JSONArray list=dest.getJSONArray("mFpsModeList");boolean present=false;
    for(int k=0;k<list.length();k++)if(rate.equals(list.getJSONObject(k).getString("mFpsValue")))present=true;
    if(!present){list.put(new JSONObject(entry.toString()));changed=true;}
   }
  }return changed?base.toString():original;
 }
}
