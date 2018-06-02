package com.example.mperminov.inventoryapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditActivity extends AppCompatActivity {
    private Uri mUri;
    @BindView(R.id.edit_product)
    TextInputEditText productEditText;
    @BindView(R.id.edit_price)
    TextInputEditText priceEditText;
    @BindView(R.id.edit_quantity)
    TextInputEditText quantityEditText;
    @BindView(R.id.edit_supplier_name)
    TextInputEditText supplierNameEditText;
    @BindView(R.id.edit_supplier_phone)
    TextInputEditText supplierPhoneEditText;
    @BindView(R.id.layout_edit_product)
    TextInputLayout productTextLayout;
    @BindView(R.id.layout_edit_price)
    TextInputLayout priceTextLayout;
    @BindView(R.id.layout_edit_quantity)
    TextInputLayout quantityTextLayout;
    @BindView(R.id.layout_edit_supplier_name)
    TextInputLayout supplierTextLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        if (getIntent().getData() != null) {
            mUri = getIntent().getData();
            setTitle(getString(R.string.edit_label_case_edit));
        } else {
            setTitle(getString(R.string.edit_label_case_add));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit.xml file.
        // This adds icon for Save in toolbar
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (validateData()) {
                if (mUri != null) {
                    finish();
                    //getContentResolver().update(mUri,values,null,null);
                } else {
                    finish();
                    //getContentResolver().insert(StoreContract.StoreEntry.CONTENT_URI,values);
                }
            } else
                Toast.makeText(this, R.string.user_correct_toast, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method collect data from TextInputEditText fields and check it according to this rules:
     * *Product name - not empty;
     * *Price - not empty. Also it should be > 0 but we can guarantee it with typeInput specified
     * in xml layout
     * *Quantity - same as for price. Postive and integer value guaranteed by xml layout parameter
     * *Supplier name - I think often happens that some people start to write phone intead of name
     * (like me). So if we found that input doesn't contain letters at all,
     * we should tell user correct name.
     *
     * @return result of validation
     */
    private boolean validateData() {
        boolean dataCorrect = true;
        //*Product name - not empty;
        if (TextUtils.isEmpty(productEditText.getText().toString())) {
            productTextLayout.setError(getString(R.string.product_name_empty_error));
            dataCorrect = false;
        } else {
            productTextLayout.setError("");
        }
        //*Price - not empty
        if (TextUtils.isEmpty(priceEditText.getText().toString())) {
            priceTextLayout.setError(getString(R.string.enter_price_error));
            dataCorrect = false;
        } else {
            priceTextLayout.setError("");
        }
        //*Quantity - not empty
        if (TextUtils.isEmpty(quantityEditText.getText().toString())) {
            quantityTextLayout.setError(getString(R.string.enter_quantity_error));
            dataCorrect = false;
        } else {
            quantityTextLayout.setError("");
        }
        //Supplier name - at least one letter in name
        if (!supplierNameEditText.getText().toString().matches("\\w*[a-zA-Z]\\w*") &&
                !TextUtils.isEmpty(supplierNameEditText.getText().toString())) {
            supplierTextLayout.setError(getString(R.string.name_error_edit_text));
            dataCorrect = false;
        } else {
            supplierTextLayout.setError("");
        }
        return dataCorrect;
    }
}
