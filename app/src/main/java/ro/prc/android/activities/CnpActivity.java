package ro.prc.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.prc.android.R;
import ro.prc.android.adapters.CnpAdapter;
import ro.prc.android.models.CNP;

public class CnpActivity extends AppCompatActivity {

    private EditText inputName, inputSurName, inputCnp;
    private TextInputLayout inputLayoutName, inputLayoutSurName, inputLayoutCnp;
    private Button btnCheck;
    private RecyclerView recyclerView;
    private List<CNP> cnpList;
    private CnpAdapter adapter;


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

        recyclerView = (RecyclerView) findViewById(R.id.recyclerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        cnpList = new ArrayList<>();
        adapter = new CnpAdapter(getApplicationContext(), cnpList);

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        refreshCnpList(getApplicationContext());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void refreshCnpList(Context context) {
        final List<CNP> list = new ArrayList<>();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://apollo.eed.usv.ro:8080/servletPrcCnp/UserController?action=listJson";

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url,
            new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d("Response", response.toString());
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            CNP cnp = new CNP();
                            JSONObject jsonobject = response.getJSONObject(i);
                            cnp.setCnp(jsonobject.getString("CNP"));
                            cnp.setName(jsonobject.getString("firstName"));
                            cnp.setSurname(jsonobject.getString("lastName"));
                            list.add(cnp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    cnpList.clear();
                    cnpList.addAll(list);
                    adapter.notifyDataSetChanged();
                    recyclerView.invalidate();
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
        final String cnp = inputCnp.getText().toString().trim();
        final String name = inputName.getText().toString().trim();
        final String surname = inputSurName.getText().toString().trim();

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
                                CNP user = new CNP();
                                user.setName(name);
                                user.setSurname(surname);
                                user.setCnp(cnp);
                                saveCNP(context, user);
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

    private void saveCNP(Context context, final CNP user) {
        RequestQueue queue = Volley.newRequestQueue(context);
        //String url = "http://localhost:8080/SimpleJspServletDB/UserController?action=addJson&firstName=ana&lastName=ana&cnp=465456";
        String url = "http://apollo.eed.usv.ro:8080/servletPrcCnp/UserController?action=addJson" +
                "&firstName=" + user.getName() +
                "&lastName=" + user.getSurname() +
                "&cnp=" + user.getCnp();

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response", response.toString());
                            boolean saved = response.getBoolean("saved");
                            if (saved) {
                                Toast.makeText(getApplicationContext(), "Salvat!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Userul exista in sistem!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Eroare!", Toast.LENGTH_SHORT).show();
                        }

                        refreshCnpList(getApplicationContext());
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
            /*@Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("firstName", user.getName());
                params.put("lastName", user.getSurname());
                params.put("cnp", user.getCnp());

                return params;
            }*/
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
