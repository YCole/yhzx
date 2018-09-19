package com.gome.usercenter.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gome.usercenter.R;

import java.io.File;

public class DisclaimerActivity extends Activity {

    private static final String TAG = "DisclaimerActivity";

    private static final String LICENSE_PATH = "/system/etc/NOTICE.html.gz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final File file = new File(LICENSE_PATH);
        if (!file.exists() || file.length() == 0) {
            Log.e(TAG, "License file " + LICENSE_PATH + " does not exist");
            showErrorAndFinish();
            return;
        }

        // Kick off external viewer due to WebView security restrictions; we
        // carefully point it at HTMLViewer, since it offers to decompress
        // before viewing.
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "text/html");
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.disclaimer));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.android.htmlviewer");

        try {
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to find viewer", e);
            showErrorAndFinish();
        }
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.disclaimer_unavailabled, Toast.LENGTH_LONG).show();
        finish();
    }
}
