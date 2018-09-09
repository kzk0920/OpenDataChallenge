package com.k09.opendatachallenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.k09.opendatachallenge.data.OdptData;
import com.k09.opendatachallenge.data.Railway;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ODPT";

    private Button mSearchButton;
    private Button.OnClickListener mSearchButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mOdptAccessor.getRailway(mRailwayNameEditText.getText().toString());
        }
    };

    private EditText mRailwayNameEditText;

    private OdptAccessor mOdptAccessor;
    private OdptAccessor.Callback mOdptAccessorCallback = new OdptAccessor.Callback() {
        @Override
        public void onGet(String dataType, OdptData[] data) {
            switch (dataType) {
                case OdptAccessor.DATA_TYPE_RAILWAY:
                    Railway[] railways = (Railway[])data;
                    for (Railway railway : railways) {
                        Log.d(LOG_TAG, "Railway title:" + railway.title);
                        for (Railway.StationOrder order : railway.stationOrder) {
                            Log.d(LOG_TAG, "Station title:" + order.stationTitle.ja + "(" + order.stationTitle.en + ")");
                        }
                    }
            }
        }

        @Override
        public void onError(int errorType) {
            String message = "不明なエラー";
            switch (errorType) {
                case OdptAccessor.ERROR_TYPE_INTERNAL: {
                    message = "内部エラーが発生しました。";
                    break;
                }
                case OdptAccessor.ERROR_TYPE_ODPT_SERVICE_UNAVAILABLE: {
                    message = "サービスが使用不可能です。";
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchButton = (Button)findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(mSearchButtonOnClickListener);

        mRailwayNameEditText = (EditText)findViewById(R.id.railway_name_edit_text);

        mOdptAccessor = new OdptAccessor(mOdptAccessorCallback);
    }
}
