package com.iyue.jxtrace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

public class MainHook implements IXposedHookLoadPackage {
    private static String TAG = "iyue_HookMain-> ";
    private Context MainActivityContext;
    // 保存所有查找到的ClassLoader
    private Set<ClassLoader> classLoaders = new HashSet<>();
    private Set<String> classNames = new HashSet<>();
    private String className = "";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if(loadPackageParam.packageName.equals("com.iyue.jxtrace")){
            return ;
        }
        XposedBridge.log(TAG + " hookPackageName==" + loadPackageParam.packageName);
//        if (loadPackageParam.packageName.equals(hookPackageName)) {
            //getMainActivityContext(loadPackageParam);
            className = getHookClassName();
            findClassLoader(loadPackageParam.classLoader);
            hookAll(loadPackageParam);
//        }
    }

    private void findClassLoader(ClassLoader loader) {
        XposedBridge.log(TAG+"start hook ActivityThread: performLaunchActivity");
        Class ActivityClientRecord = XposedHelpers.findClass("android.app.ActivityThread$ActivityClientRecord",loader);
        XposedHelpers.findAndHookMethod("android.app.ActivityThread", loader, "performLaunchActivity", ActivityClientRecord, Intent.class, new XC_MethodHook() {
            @Override
            public int compareTo(XCallback o) {
                return 0;
            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                XposedBridge.log(TAG+"ActivityThread: performLaunchActivity");
            }
        });
    }

    /**
     * 获取上下文 正常APP的上下文
     */
    private void getMainActivityContext(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        XposedHelpers.findAndHookMethod("android.app.Instrumentation", loadPackageParam.classLoader, "prePerformCreate", Activity.class, new XC_MethodHook() {
            @Override
            public int compareTo(XCallback o) {
                return 0;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log(TAG + " end hook MainActivity");
                MainActivityContext = (Context) param.args[0];
                XposedBridge.log(TAG +"MainActivityContext:"+MainActivityContext.getClassLoader().toString());
                // 加壳app 可分析获取壳初始化后的ClassLoader
            }
        });
    }

    /**
     * @return 获取 jtrace 需要过滤的类名信息
     */
    private static String getHookClassName(){
        try {
            FileInputStream fileInputStream = new FileInputStream("/data/local/tmp/hookPackage");
            BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(fileInputStream));
            String s = bufferedReader.readLine();
            XposedBridge.log(TAG+"we need hook:"+s+"*");
            bufferedReader.close();
            fileInputStream.close();
            return s;
        } catch (FileNotFoundException e) {
            XposedBridge.log(TAG+"getHookCLassName fail:"+e.getMessage());
            XposedBridge.log(TAG+"echo \"class name\" > /data/local/tmp/hookPackage");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * //  获取所有ClassLoader
     *
     * @param loadPackageParam
     */
    private void hookAll(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        /**
         * dalvik.system.PathClassLoader
         * java.lang.BootClassLoader
         */

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            classLoaders.add(classLoader);
            XposedBridge.log(TAG + classLoader.toString());
            ClassLoader parent = classLoader.getParent();
            while (parent != null) {

                if (parent.getClass().getName().contains("java.lang.BootClassLoader")) {
                    XposedBridge.log(TAG + "hookAll: find ClassLoader break");
                    break;
                }
                XposedBridge.log(TAG + parent.toString());
                classLoaders.add(parent);
                parent = parent.getParent();
            }

            for (ClassLoader loader : classLoaders) {
                hookALLClass(loader);
            }
            XposedBridge.log(TAG + "hookALLClass end!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param classLoader 根据ClassLoader 查找所有类
     */
    private void hookALLClass(ClassLoader classLoader) {
        //XposedBridge.log(TAG + "hookALLClass():ClassLoader:"+classLoader.toString());
        XposedHelpers.findAndHookMethod("java.lang.ClassLoader", classLoader, "loadClass", String.class, new XC_MethodHook() {
            @Override
            public int compareTo(XCallback o) {
                return 0;
            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                XposedBridge.log(TAG + "-loadClass className:" + param.args[0]);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Class aClass = (Class) param.getResult();
                String strClassName;
                Method[] declaredMethods= null;
                try{
                    strClassName= aClass.toString();
                    if (classNames.contains(strClassName)){
                        XposedBridge.log(TAG +"-class size:"+classNames.size());
                        return ;
                    }
                    classNames.add(strClassName);
                }catch(Exception e){
                    XposedBridge.log(TAG +"-Class.toString fail:"+aClass);
                    return;
                }
//                if(strClassName.contains("android.os.Debug")){
//                    // 过滤不需要hook的类名 或前缀
//                    return ;
//                }
//                // 二选一即可

                if (!className.equals("")){
                    // 过滤需要hook的类名 或前缀
                    if(!strClassName.contains(className)){
                        return ;
                    }
                }

                XposedBridge.log(TAG+"hookALLClass : "+strClassName);
                try{
                    declaredMethods= aClass.getDeclaredMethods();
                }catch (Exception e){
                    XposedBridge.log(TAG + "Class:"+aClass+"getDeclaredMethods fail: "+e.getMessage());
                    return ;
                }
                for (Method method : declaredMethods) {
                    //XposedBridge.log(TAG + "hook method: "+ strClassName +" : "+ method.getName());
                    int modifiers = method.getModifiers();
                    if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                        String finalStrClassName = strClassName;
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            public int compareTo(XCallback o) {
                                return 0;
                            }

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                String str = new String();
                                int length = param.args.length;
                                Class<?>[] parameterTypes = method.getParameterTypes();
                                for (int i = 0; i < length; ++i) {
                                    Object s;
                                    try {
                                        s = parameterTypes[i].cast(param.args[i]).toString();
                                    } catch (Exception e) {
                                        s = param.args[i];
                                    }
                                    str += " " + parameterTypes[i] + " " + s;
                                }
                                XposedBridge.log(TAG +" class: "+ finalStrClassName + " call " + method.getName() + "(" + str + ")");
                            }
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                XposedBridge.log(TAG + " call " + method.getName() + " --> result: "+ param.getResult());
                            }
                        });
                    }
                }
            }
        });
    }

}


