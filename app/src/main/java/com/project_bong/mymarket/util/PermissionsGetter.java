package com.project_bong.mymarket.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionsGetter {
    private Context mContext;
    private ArrayList<String> listPermissions;

    private boolean haveDenied = false;

    public PermissionsGetter(Context context){
        mContext = context;
    }

    public boolean haveDeniedPermissions(String[] permissions){
        listPermissions = new ArrayList<>();

        for(String pm:permissions){
            if(ContextCompat.checkSelfPermission(mContext,pm) == PackageManager.PERMISSION_DENIED){
                Log.d("permission","denied : "+pm);
                if(pm.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                    continue;
                }

                if(pm.equals(Manifest.permission.READ_EXTERNAL_STORAGE) && Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    continue;
                }

                if(pm.equals(Manifest.permission.READ_MEDIA_IMAGES) && Build.VERSION.SDK_INT<Build.VERSION_CODES.TIRAMISU){
                    continue;
                }
                listPermissions.add(pm);
            }
        }
        haveDenied = listPermissions.size() > 0;
        Log.d("permission","haveDenied : "+haveDenied);
        return haveDenied;

    }

    public String[] getPermissions(){
        if(haveDenied){
            String[] permissions = listPermissions.toArray(new String[listPermissions.size()]);
            return permissions;
        }else{
            return null;
        }
    }
}
