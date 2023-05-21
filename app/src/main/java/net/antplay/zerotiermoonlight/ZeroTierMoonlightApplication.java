package net.antplay.zerotiermoonlight;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import net.antplay.zerotiermoonlight.model.DaoMaster;
import net.antplay.zerotiermoonlight.model.DaoSession;
import net.antplay.zerotiermoonlight.model.ZTOpenHelper;


/**
 * main program entry
 *
 * @author kaaass
 */
public class ZeroTierMoonlightApplication extends MultiDexApplication {
    private DaoSession mDaoSession;

    public void onCreate() {
        super.onCreate();
        Log.i("Application", "Starting Application");
        // 创建 DAO 会话
        this.mDaoSession = new DaoMaster(
                new ZTOpenHelper(this, "ztfixdb", null)
                        .getWritableDatabase()
        ).newSession();
    }

    public DaoSession getDaoSession() {
        return this.mDaoSession;
    }
}
