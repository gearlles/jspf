# Introduction #


Following article describes how to get JSPF running on an android smartphone using Android 2.2 level 8.


# Getting Started #


First of all include the jspf.core in your build path. In your code create a PluginManager like
```
PluginManager pm = PluginManagerFactory.createPluginManager();
```

Due to Android's file structure and access mechanisms, there seems to be no direct way to access the directory where your plugins lie. Thus each plugin implementation _and_ its dependencies have to be added with the help of ClassURI.

```
pm.addPluginsFrom(new ClassURI(CoolPluginImpl.class).toURI());
```

Loading and using your plugins is done just the same way it is done on a normal system.
For completeness take a look at the full example below.

```
package de.jspfdemo;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.uri.ClassURI;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import de.jspfdemo.plugins.CoolPlugin;
import de.jspfdemo.plugins.impl.CoolPluginImpl;

public class JSPFDemo extends Activity {
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        // Loading and adding plugins via class uri
        PluginManager pm = PluginManagerFactory.createPluginManager();
        pm.addPluginsFrom(new ClassURI(CoolPluginImpl.class).toURI());
        
        // Getting the CoolPlugin
        CoolPlugin plugin = pm.getPlugin(CoolPluginImpl.class);
        
        // Setting the text of a TextView with the help of the CoolPlugin
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(plugin.sayHello());
    }
}
```