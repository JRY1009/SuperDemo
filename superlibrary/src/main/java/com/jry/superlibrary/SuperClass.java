package com.jry.superlibrary;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.IBinder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujing on 2015/6/25.
 */
public class SuperClass {

    public static final String CMD_DEFAULT_LAUNCHER = "defaultLauncher";
    Object mPackageManager;

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.print("args:" + arg + "\n");
        }
        new SuperClass().handleCommand(args);
    }

    private void handleCommand(String[] args) {
        try {
            String cmd = args[0];
            if (cmd == null || cmd.equals(""))
                return;

            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getMethod("getService", java.lang.String.class);
            IBinder service = (IBinder) getService.invoke(null, "package");

            Class<?> iPackageManagerStub = Class.forName("android.content.pm.IPackageManager$Stub");
            Method asInterface = iPackageManagerStub.getMethod("asInterface", IBinder.class);
            mPackageManager = asInterface.invoke(null, service);

            if (mPackageManager == null) {
                System.out.println(getClass() + ":packagemanager is null\n");
                System.exit(0);
                return;
            }

            if (cmd.equalsIgnoreCase(CMD_DEFAULT_LAUNCHER)) {
                if (args.length < 2)
                    return;

                setDefaultLauncher(args[1], args[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    void setDefaultLauncher(String pkg, String cls){

        System.out.print("defaultLauncher : " + pkg + "," + cls + "\n");

        Class pmClass = mPackageManager.getClass();

        Method clearPackagePreferredActivities = null;
        Method addPreferredActivity = null;
        Method addPreferredActivity_Uid = null;
        Method getPreferredActivities = null;
        Method queryIntentActivities = null;

        try {
            clearPackagePreferredActivities = pmClass.getMethod("clearPackagePreferredActivities",
                    String.class);
            getPreferredActivities = pmClass.getMethod("getPreferredActivities",
                    List.class,
                    List.class,
                    String.class);
            queryIntentActivities = pmClass.getMethod("queryIntentActivities",
                    Intent.class,
                    String.class,
                    int.class,
                    int.class);

        }catch(Exception e){
            System.out.print("defaultLauncher : " + e.toString());
        }

        if (clearPackagePreferredActivities == null
                || getPreferredActivities == null
                || queryIntentActivities == null) {
            System.out.print("defaultLauncher : NonPointer Return");
            return;
        }

        try {
            addPreferredActivity = pmClass.getMethod("addPreferredActivity",
                    IntentFilter.class,
                    int.class,
                    ComponentName[].class,
                    ComponentName.class);
        }catch(Exception e){
            System.out.print("defaultLauncher : " + e.toString());
        }

        try {
            addPreferredActivity_Uid = pmClass.getMethod("addPreferredActivity",
                    IntentFilter.class,
                    int.class,
                    ComponentName[].class,
                    ComponentName.class,
                    int.class);
        }catch(Exception e){
            System.out.print("defaultLauncher : " + e.toString());
        }

        if (addPreferredActivity == null && addPreferredActivity_Uid == null) {
            System.out.print("defaultLauncher : addPreferredActivity NonPointer Return");
            return;
        }

        try{
            // clean default launcher
            ArrayList<IntentFilter> intentList = new ArrayList<IntentFilter>();
            ArrayList<ComponentName> cnList = new ArrayList<ComponentName>();
            getPreferredActivities.invoke(mPackageManager, intentList, cnList, null);
            IntentFilter dhIF;
            for (int i = 0; i < cnList.size(); i++) {
                dhIF = intentList.get(i);
                if (dhIF.hasAction(Intent.ACTION_MAIN) && dhIF.hasCategory(Intent.CATEGORY_HOME)) {
                    clearPackagePreferredActivities.invoke(mPackageManager, cnList.get(i).getPackageName());
                }
            }

            // get all components and the best match
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MAIN);
            filter.addCategory(Intent.CATEGORY_HOME);
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);

            List<ResolveInfo> outActivities = (List<ResolveInfo>) queryIntentActivities.invoke(mPackageManager, intent, null, 0, 0);
            if(outActivities == null || outActivities.size() == 0) {
                System.out.print("defaultLauncher : None outActivities");
                return;
            }

            ComponentName[] components = new ComponentName[outActivities.size()];
            int bestMatch = 0;
            for(int i = 0; i < outActivities.size(); i ++){
                ResolveInfo r = outActivities.get(i);
                components[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                if(r.match > bestMatch) bestMatch = r.match;
            }

            // set default launcher
            ComponentName launcher = new ComponentName(pkg, cls);
            if (addPreferredActivity != null) {
                addPreferredActivity.invoke(mPackageManager, filter, bestMatch, components, launcher);
            } else if (addPreferredActivity_Uid != null) {
                addPreferredActivity_Uid.invoke(mPackageManager, filter, bestMatch, components, launcher, 0);
            }
        }catch(Exception e){
            System.out.print("defaultLauncher : " + e.toString());
        }
    }
}
