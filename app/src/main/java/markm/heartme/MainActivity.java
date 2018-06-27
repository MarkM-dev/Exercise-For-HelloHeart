package markm.heartme;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private String CONFIG_URL = "https://s3.amazonaws.com/s3.helloheart.home.assignment/bloodTestConfig.json";

    private AutoCompleteTextView testName_actv;
    private EditText resNumber_editText;
    private String[] bloodTestNames;
    private Button main_submit_btn;
    private TextView result_textView;
    private JSONArray bloodTestConfig_JSON_arr;
    private LottieAnimationView animationView;
    private LottieAnimationView logo_animationView;
    private Boolean isAlreadySubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        animationView = findViewById(R.id.animation_view);
        logo_animationView = findViewById(R.id.logo_animation_view);
        testName_actv = findViewById(R.id.main_testName_actv);
        resNumber_editText = findViewById(R.id.main_resNumber_editText);
        main_submit_btn = findViewById(R.id.main_submit_btn);
        result_textView = findViewById(R.id.main_result_textView);

        resNumber_editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    startComputingSequence();

                    return true;
                }
                return false;
            }
        });

        main_submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAlreadySubmitted) {
                    reset();
                    return;
                }

                if (testName_actv.getText().toString().equals("") || resNumber_editText.getText().toString().equals("")) {
                    return;
                }

                startComputingSequence();
            }
        });


        new getBloodTestConfigData().execute();
    }

    private void startComputingSequence() {
        hideKeyboard();

        // wait for the keyboard to drop.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                computeResults();
                setSubmittedToTrue();
            }
        }, 350);
    }

    private void computeResults () {
        String testName = testName_actv.getText().toString();
        int resultNumber = Integer.parseInt(resNumber_editText.getText().toString().trim());

        JSONObject relatedJSONObject = getJSONObjectForTestName(testName);

        if (relatedJSONObject != null) {

            try {
                if (resultNumber < relatedJSONObject.getInt("threshold")) {
                    // GOOD.
                    result_textView.setText(relatedJSONObject.getString("name") + " - " + getResources().getString(R.string.good_result));
                    animationView.setAnimation("checked_done_.json");
                    animationView.playAnimation();

                } else {
                    // BAD.
                    result_textView.setText(relatedJSONObject.getString("name") + " - " + getResources().getString(R.string.bad_result));
                    animationView.setAnimation("uh_oh.json");
                    animationView.playAnimation();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            // test not found.
            result_textView.setText(testName_actv.getText().toString() + " - " + getResources().getString(R.string.unknown_result));
            animationView.setAnimation("empty_list.json");
            animationView.playAnimation();
        }
    }

    private JSONObject getJSONObjectForTestName (String testName) {
        JSONObject relatedJSONObject = null;
        String[] testNames = new String[bloodTestConfig_JSON_arr.length()];

        try {

            for (int i = 0; i < bloodTestConfig_JSON_arr.length(); i++) {
                testNames[i] = bloodTestConfig_JSON_arr.getJSONObject(i).getString("name");
            }
           
            ExtractedResult obj = FuzzySearch.extractOne(testName, Arrays.asList(testNames));

            if (obj.getScore() > 40) {
                relatedJSONObject = bloodTestConfig_JSON_arr.getJSONObject(obj.getIndex());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return relatedJSONObject;
    }

    private void fillTestNamesForACTV () {
        JSONObject btConfig_JSON_obj;
        bloodTestNames = new String[bloodTestConfig_JSON_arr.length()];

        for (int i = 0; i < bloodTestConfig_JSON_arr.length(); i++) {
            try {
                btConfig_JSON_obj = bloodTestConfig_JSON_arr.getJSONObject(i);
                bloodTestNames[i] = btConfig_JSON_obj.getString("name");
                Log.e(TAG, bloodTestNames[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        attachTestNamesToACTV();
    }

    private void attachTestNamesToACTV () {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, bloodTestNames);
        testName_actv.setThreshold(1); //will start working from first character
        testName_actv.setAdapter(adapter);
    }

    private class getBloodTestConfigData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(CONFIG_URL);

            //Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    bloodTestConfig_JSON_arr = jsonObj.getJSONArray("bloodTestConfig");

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            fillTestNamesForACTV();
        }
    }

    private void setSubmittedToTrue () {
        animationView.setVisibility(View.VISIBLE);
        main_submit_btn.setText(R.string.main_submit_btn_reset_str);
        isAlreadySubmitted = true;
    }

    private void reset () {
        result_textView.setText("");
        animationView.setVisibility(View.INVISIBLE);
        testName_actv.setText("");
        resNumber_editText.setText("");
        main_submit_btn.setText(R.string.main_submit_btn_submit_str);
        isAlreadySubmitted = false;

        testName_actv.requestFocus();
        showKeyboard();
    }

    private void hideKeyboard () {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(resNumber_editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showKeyboard () {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(testName_actv, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        logo_animationView.setAnimation("like.json");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logo_animationView.playAnimation();
            }
        }, 500);

        // wait for animation to finish (without waiting for dead time at the end of the animation).
        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                ValueAnimator anim = ValueAnimator.ofInt(logo_animationView.getMeasuredHeight(), 150);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = logo_animationView.getLayoutParams();
                        layoutParams.height = val;
                        logo_animationView.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(1500);
                anim.start();
            }
        }, 2000);
    }

}
