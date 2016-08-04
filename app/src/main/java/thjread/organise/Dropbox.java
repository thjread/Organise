package thjread.organise;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Created by tread on 04/08/16.
 */
public class Dropbox {
    final static String dropbox_token_pref = "DROPBOX_ACCESS_TOKEN_PREF";

    static private String APP_KEY;
    static private String APP_SECRET;
    static private DropboxAPI<AndroidAuthSession> mDBApi;
    static private Context context;

    public static void init(Context c, final SwipeRefreshLayout swipeRefresh, final SyncFilesCallback callback) {
        context = c;

        APP_KEY = context.getResources().getString(R.string.dropbox_app_key);
        APP_SECRET = context.getResources().getString(R.string.dropbox_app_secret);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<>(session);

        String token = PrefUtils.readPref(context, dropbox_token_pref, null);
        if (token != null) {
            mDBApi.getSession().setOAuth2AccessToken(token);
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(true);
                    syncFiles(callback);
                }
            });
        } else {
            mDBApi.getSession().startOAuth2Authentication(context);
        }
    }

    public static void resumeAuth(SyncFilesCallback callback) {
        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();
                String token = mDBApi.getSession().getOAuth2AccessToken();
                PrefUtils.writePref(context, dropbox_token_pref, token);
                syncFiles(callback);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public static void syncFiles(SyncFilesCallback callback) {
        OrgFiles files = GlobalState.getFiles();
        files.syncFiles(context, mDBApi, callback);
    }
}
