package hype.s23.camera;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import de.robv.android.xposed.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
final class XmlAdapter {
 static final String PREFIX="BACK_CAMCORDER_RESOLUTION_FEATURE_MAP_";
 static void install(Context context)throws Exception{
  Set<String> rear=Capabilities.common(Capabilities.scan(context,CameraCharacteristics.LENS_FACING_BACK));
  String[] roots={"/system/cameradata","/system/etc/cameradata","/system_ext/cameradata","/product/cameradata","/vendor/cameradata","/odm/cameradata","/system/etc","/system_ext/etc","/product/etc","/vendor/etc","/odm/etc"};
  String[] names={"camera-feature.xml","camera-features.xml","camera_feature.xml","camera_features.xml"};
  Map<String,File> candidates=new LinkedHashMap<>();for(String root:roots)for(String name:names){File f=new File(root,name);if(f.isFile()&&f.canRead()&&f.length()>0&&f.length()<2_000_000)candidates.put(f.getCanonicalPath(),f);}
  if(candidates.size()!=1){XposedBridge.log("[s23_hype Camera] XML absent or ambiguous; stock retained");return;}
  File original=candidates.values().iterator().next();
  DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();factory.setFeature("http://xml.org/sax/features/external-general-entities",false);factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);factory.setExpandEntityReferences(false);
  Document doc=factory.newDocumentBuilder().parse(original);if(!"resources".equals(doc.getDocumentElement().getTagName()))return;
  NodeList nodes=doc.getDocumentElement().getElementsByTagName("local");int count=0;Set<String> seen=new HashSet<>();
  for(int i=0;i<nodes.getLength();i++){Element e=(Element)nodes.item(i);String name=e.getAttribute("name");if(!name.isEmpty()&&!seen.add(name))return;}
  for(int[] mode:Capabilities.MODES)for(int i=2;i<mode.length;i++){
   int fps=mode[i];if(!rear.contains(Capabilities.key(mode[0],mode[1],fps)))continue;
   String name=PREFIX+mode[0]+"X"+mode[1]+"_"+fps+"FPS";
   for(int j=0;j<nodes.getLength();j++){Element e=(Element)nodes.item(j);if(name.equals(e.getAttribute("name"))&&"false".equals(e.getAttribute("value"))){e.setAttribute("value","true");count++;}}
  }
  Map<String,Boolean> flags=new LinkedHashMap<>();
  flags.put("SUPPORT_PRO_VIDEO_8K",rear.contains("7680x4320@30")||rear.contains("7680x4320@24"));
  Set<String> wide=Capabilities.wideModes(context,false);
  flags.put("SUPPORT_PRO_VIDEO_WIDE_LENS_60FPS",wide.contains("1920x1080@60")||wide.contains("3840x2160@60"));
  flags.put("SUPPORT_PRO_VIDEO_WIDE_LENS_120FPS",wide.contains("1920x1080@120")||wide.contains("3840x2160@120"));
  flags.put("SUPPORT_VIDEO_HDR",Capabilities.allHdr(context,true));
  flags.put("SUPPORT_AUTO_HDR_MENU",Capabilities.allHdr(context,false));
  flags.put("SUPPORT_FLASH_IN_WIDE_LENS",!Capabilities.wideModes(context,true).isEmpty());
  boolean knownPro=false;
  for(int j=0;j<nodes.getLength();j++){Element e=(Element)nodes.item(j);if(Arrays.asList(e.getAttribute("supported-mode").split(",")).contains("pro_video"))knownPro=true;}
  for(int j=0;j<nodes.getLength();j++){Element e=(Element)nodes.item(j);String name=e.getAttribute("name");if(Boolean.TRUE.equals(flags.get(name))&&"false".equals(e.getAttribute("value"))){e.setAttribute("value","true");count++;}
   if(!knownPro||!name.startsWith(PREFIX)||!"true".equals(e.getAttribute("value"))||!e.hasAttribute("supported-mode"))continue;
   String raw=name.substring(PREFIX.length());if(!raw.matches("[0-9]+X[0-9]+_[0-9]+FPS"))continue;
   String mode=raw.replace('X','x').replace("_","@").replace("FPS","");String modes=e.getAttribute("supported-mode");
   if(rear.contains(mode)&&!modes.isEmpty()&&!Arrays.asList(modes.split(",")).contains("pro_video")){e.setAttribute("supported-mode",modes+",pro_video");count++;}
  }
  if(count==0){XposedBridge.log("[s23_hype Camera] no verified existing disabled XML resolution flags");return;}
  File dir=new File(context.getCacheDir(),"s23_hype_camera");if(!dir.isDirectory()&&!dir.mkdirs())return;
  File target=File.createTempFile("camera-feature-",".xml",dir);try{Transformer t=TransformerFactory.newInstance().newTransformer();t.transform(new DOMSource(doc),new StreamResult(target));factory.newDocumentBuilder().parse(target);}catch(Exception e){target.delete();throw e;}
  String canonical=original.getCanonicalPath();
  XC_MethodHook redirect=new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){try{Object a=p.args[0];File f=a instanceof File?(File)a:new File((String)a);if(canonical.equals(f.getCanonicalPath())&&target.isFile())p.args[0]=a instanceof File?target:target.getAbsolutePath();}catch(Throwable ignored){}}};
  XposedHelpers.findAndHookConstructor(FileInputStream.class,File.class,redirect);
  XposedHelpers.findAndHookConstructor(FileInputStream.class,String.class,redirect);
  XposedBridge.log("[s23_hype Camera] XML adapter staged "+count+" feature flags / Pro Video entries; applies only if camera reads original through FileInputStream");
 }
}
