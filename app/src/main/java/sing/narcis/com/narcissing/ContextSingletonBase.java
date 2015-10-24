package sing.narcis.com.narcissing;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;

public class ContextSingletonBase<T>{

  @SuppressWarnings("rawtypes")
  private static HashMap<String, ContextSingletonBase> classnameToInstance = new HashMap<String, ContextSingletonBase>();
  protected Context context;

  protected ContextSingletonBase(){ }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T extends ContextSingletonBase> T getInstance(Class<T> clazz) {
    ContextSingletonBase instance = classnameToInstance.get(clazz.getName());
      if(instance == null){
        try {
          instance = clazz.newInstance();
          classnameToInstance.put(clazz.getName(), (T) instance);
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      return (T) instance;
  }

  public void init(Context context){
    this.context = context;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      super.finalize();
    } finally {
      this.context = null;
    }
  }
}
