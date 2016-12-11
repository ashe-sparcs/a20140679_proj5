package com.example.madstein.a20140679_proj5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Madstein on 2016-12-11.
 */

public class DocumentsSample extends Activity {
    private static final String TAG = "DocumentsSample";
    private static final int CODE_READ = 42;

    private TextView mResult;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Context context = this;

        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);

        mResult = new TextView(context);
        view.addView(mResult);

        final CheckBox multiple = new CheckBox(context);
        multiple.setText("ALLOW_MULTIPLE");
        view.addView(multiple);
        final CheckBox localOnly = new CheckBox(context);
        localOnly.setText("LOCAL_ONLY");
        view.addView(localOnly);

        Button button;
        button = new Button(context);
        button.setText("OPEN_DOC */*");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);
    }
}
