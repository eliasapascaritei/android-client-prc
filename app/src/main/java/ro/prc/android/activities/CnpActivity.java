package ro.prc.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ro.prc.android.R;

public class CnpActivity extends AppCompatActivity {

    private EditText inputName, inputSurName, inputCnp;
    private TextInputLayout inputLayoutName, inputLayoutSurName, inputLayoutCnp;
    private Button btnCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cnp);

        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutSurName = (TextInputLayout) findViewById(R.id.input_layout_surname);
        inputLayoutCnp = (TextInputLayout) findViewById(R.id.input_layout_cnp);
        inputName = (EditText) findViewById(R.id.input_name);
        inputSurName = (EditText) findViewById(R.id.input_surname);
        inputCnp = (EditText) findViewById(R.id.input_cnp);
        btnCheck = (Button) findViewById(R.id.btn_check);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputSurName.addTextChangedListener(new MyTextWatcher(inputSurName));
        inputCnp.addTextChangedListener(new MyTextWatcher(inputCnp));

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validateSurName()) {
            return;
        }

        validateCnp(getApplicationContext());
    }

    private void validateCnp(final Context context) {
        String cnp = inputCnp.getText().toString().trim();

        if (cnp.isEmpty()) {
            inputLayoutCnp.setError(getString(R.string.err_msg_empty_cnp));
            requestFocus(inputCnp);
        } else {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://openapi.ro/api/validate/cnp/" + cnp + ".json";

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response", response.toString());
                            boolean valid = response.getBoolean("valid");
                            if (valid) {
                                inputLayoutCnp.setErrorEnabled(false);
                                saveCNP(context);
                            } else {
                                inputLayoutCnp.setError(getString(R.string.err_msg_cnp));
                                requestFocus(inputCnp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            inputLayoutCnp.setError(getString(R.string.err_msg_cnp));
                            requestFocus(inputCnp);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
            );
            queue.add(getRequest);
        }
    }

    private void saveCNP(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://httpbin.org/post";

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());

                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name", "Alif");
                params.put("domain", "http://itsalif.info");

                return params;
            }
        };
        queue.add(postRequest);
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_empty_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateSurName() {
        if (inputSurName.getText().toString().trim().isEmpty()) {
            inputLayoutSurName.setError(getString(R.string.err_msg_empty_surname));
            requestFocus(inputSurName);
            return false;
        } else {
            inputLayoutSurName.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_surname:
                    validateSurName();
                    break;
                case R.id.input_cnp:
                    //validateCnp(getApplicationContext());
                    break;
            }
        }
    }

}
