package ro.prc.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;
import ro.prc.android.R;
import ro.prc.android.adapters.CnpAdapter;
import ro.prc.android.models.CNP;

public class SearchActivity extends AppCompatActivity {

    private EditText inputCnp;
    private TextInputLayout inputLayoutCnp;
    private CircleButton btnSearch;
    private RecyclerView recyclerView;
    private ArrayList<CNP> cnpList;
    private CnpAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputLayoutCnp = (TextInputLayout) findViewById(R.id.input_search_cnp);
        inputCnp = (EditText) findViewById(R.id.input_cnp_search);
        btnSearch = (CircleButton) findViewById(R.id.btSearch);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerListSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        cnpList = new ArrayList<>();
        adapter = new CnpAdapter(getApplicationContext(), cnpList);

        recyclerView.setAdapter(adapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void submitForm() {
        searchCnp(getApplicationContext());
    }

    private void searchCnp(final Context context) {
        String cnp = inputCnp.getText().toString().trim();

        if (cnp.isEmpty()) {
            inputLayoutCnp.setError(getString(R.string.err_msg_empty_cnp));
            requestFocus(inputCnp);
        } else {
            final ArrayList<CNP> list = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://apollo.eed.usv.ro:8080/servletPrcCnp/UserController?action=showJ&cnp=" + cnp;

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        inputLayoutCnp.setErrorEnabled(false);

                        try {
                            if (response.getInt("userid") == 0) {
                                cnpList.clear();
                                adapter.notifyDataSetChanged();
                            } else {
                                CNP cnp = new CNP();
                                cnp.setCnp(response.getString("CNP"));
                                cnp.setName(response.getString("firstName"));
                                cnp.setSurname(response.getString("lastName"));
                                list.add(cnp);

                                satefut(list);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void satefut(ArrayList<CNP> list) {
        cnpList.clear();
        cnpList.addAll(list);
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
