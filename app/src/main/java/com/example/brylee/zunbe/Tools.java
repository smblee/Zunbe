package com.example.brylee.zunbe;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by brylee on 5/8/17.
 */

public class Tools {
    public static ProgressDialog createLoadingDialog(Context context, String title) {
        ProgressDialog pd;
        pd = new ProgressDialog(context);
        //Create a new progress dialog
        pd.setTitle(title);
        pd.setMessage("Please wait a bit...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setMax(100);
        pd.setProgressPercentFormat(null);
        pd.setProgressNumberFormat(null);
        return pd;
    }
}
