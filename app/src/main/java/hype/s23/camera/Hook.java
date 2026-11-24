package hype.s23.camera;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.camera2.CameraCharacteristics;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class Hook implements IXposedHookLoadPackage {
 private static final String PKG="com.sec.android.app.camera",TAG="[s23_hype Camera] ";
 private final ThreadLocal<Boolean> busy=new ThreadLocal<>();
 private final Map<String,String> patched=new HashMap<>();
 private volatile Context context;
 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
  if(!PKG.equals(p.packageName)||!PKG.equals(p.processName)||!"samsung".equalsIgnoreCase(android.os.Build.MANUFACTURER))return;
  try{XposedHelpers.findAndHookMethod(Application.class,"attach",Context.class,new XC_MethodHook(){@Override protected void afterHookedMethod(MethodHookParam param){context=(Context)param.args[0];try{XmlAdapter.install(context);}catch(Throwable e){XposedBridge.log(TAG+"XML fallback: "+e.getClass().getSimpleName());}}});}catch(Throwable e){XposedBridge.log(TAG+"attach hook unavailable");}
  try{XposedHelpers.findAndHookMethod(AssetManager.class,"open",String.class,int.class,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam param){
   if(context==null||Boolean.TRUE.equals(busy.get())||!(param.args[0] instanceof String))return;
   String path=(String)param.args[0];if(!path.matches("config/video_size_fps_modes(?:_[a-zA-Z0-9]+)*\\.json"))return;
   if(param.thisObject!=context.getAssets())return;
   busy.set(true);try{String out=patch((AssetManager)param.thisObject,path);if(out!=null)param.setResult(new ByteArrayInputStream(out.getBytes(StandardCharsets.UTF_8)));}catch(Throwable e){XposedBridge.log(TAG+"asset fallback: "+e.getClass().getSimpleName());}finally{busy.remove();}
  }});XposedBridge.log(TAG+"asset adapter registered (no Samsung class-name dependency)");}catch(Throwable e){XposedBridge.log(TAG+"asset adapter unavailable: "+e.getClass().getSimpleName());}
 }
 private synchronized String patch(AssetManager am,String path)throws Exception{
  if(patched.containsKey(path))return patched.get(path);
  String original;try(InputStream in=am.open(path)){original=read(in);}String out=original;
  Set<String> allowed=new HashSet<>(Capabilities.common(Capabilities.scan(context,CameraCharacteristics.LENS_FACING_BACK)));allowed.retainAll(Capabilities.common(Capabilities.scan(context,CameraCharacteristics.LENS_FACING_FRONT)));
  String[] assets=am.list("config");if(assets!=null)for(String name:assets){String donor="config/"+name;if(donor.equals(path)||!donor.matches("config/video_size_fps_modes(?:_[a-zA-Z0-9]+)*\\.json"))continue;try(InputStream in=am.open(donor)){out=JsonModes.merge(out,read(in),allowed);}catch(Exception ignored){}}
  if(!out.equals(original)){patched.put(path,out);XposedBridge.log(TAG+"added advertised modes to "+path);return out;}
  patched.put(path,null);XposedBridge.log(TAG+"no additional compatible shipped modes for "+path);return null;
 }
 private static String read(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(b.size()+n>262144)throw new IOException("Asset too large");b.write(buf,0,n);}return b.toString(StandardCharsets.UTF_8.name());}
}
