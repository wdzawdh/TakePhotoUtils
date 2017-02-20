/**
 *   @function:$
 *   @description: $
 *   @param:$
 *   @return:$
 *   @history:
 * 1.date:$ $
 *           author:$
 *           modification:
 */

package com.cw.takephotoutils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * @author Cw
 * @date 17/2/9
 */
public class BaseApplication extends Application {

    public static Context sAppContext;
    //全局的handler
    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
        mHandler=new Handler();
    }

    public static Handler getHandler() {
        return mHandler;
    }

}