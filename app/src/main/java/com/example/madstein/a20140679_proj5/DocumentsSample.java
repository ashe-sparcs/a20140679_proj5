
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.madstein.a20140679_proj5;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Example that exercises client side of {@link DocumentsContract}.
 */
public class DocumentsSample extends Activity {
    private static final String TAG = "DocumentsSample";

    private static final int CODE_READ = 42;
    /*
    private static final int CODE_WRITE = 43;
    private static final int CODE_TREE = 44;
    private static final int CODE_RENAME = 45;
    */

    private TextView mResult;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Context context = this;

        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);

        mResult = new TextView(context);
        view.addView(mResult);

        Button button;
        button = new Button(context);
        button.setText("OPEN_DOC */*");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);

        final ScrollView scroll = new ScrollView(context);
        scroll.addView(view);

        setContentView(scroll);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ContentResolver cr = getContentResolver();

        clearLog();

        log("resultCode=" + resultCode);
        log("data=" + String.valueOf(data));

        final Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            log("isDocumentUri=" + DocumentsContract.isDocumentUri(this, uri));
        } else {
            log("missing URI?");
            return;
        }

        if (requestCode == CODE_READ) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                log("FAILED TO TAKE PERMISSION", e);
            }
            InputStream is = null;
            try {
                is = cr.openInputStream(uri);
                log("read length=" + readFullyNoClose(is).length);
            } catch (Exception e) {
                log("FAILED TO READ", e);
            } finally {
                closeQuietly(is);
            }
        }
    }

    private void clearLog() {
        mResult.setText(null);
    }

    private void log(String msg) {
        log(msg, null);
    }

    private void log(String msg, Throwable t) {
        Log.d(TAG, msg, t);
        mResult.setText(mResult.getText() + "\n" + msg);
    }

    public static byte[] readFullyNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, count);
        }
        return bytes.toByteArray();
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}