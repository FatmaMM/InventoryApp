package com.example.fatma.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fatma.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.text.TextUtils.isEmpty;

public class EditeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 999;

    @BindView(R.id.name)
    EditText editTextName;
    @BindView(R.id.quantity)
    EditText editTextQuantity;
    @BindView(R.id.price)
    EditText editTextPrice;
    @BindView(R.id.selected_image)
    ImageView imageView;
    @BindView(R.id.spinner_size)
    Spinner mSizeSpinner;

    private int mSize = 0;
    private static final int EXISTING_LOADER = 1;

    private Uri mCurrentUri;

    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edite);
        ButterKnife.bind(EditeActivity.this);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            setTitle(getString(R.string.add_new_iten));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edite));
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }
        editTextName.setOnTouchListener(mTouchListener);
        editTextPrice.setOnTouchListener(mTouchListener);
        editTextQuantity.setOnTouchListener(mTouchListener);
        mSizeSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @OnClick(R.id.btn_image)
    public void selectImage(View view) {
        ActivityCompat.requestPermissions(EditeActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_PERMISSIONS_READ_EXTERNAL_STORAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI).setType("image/*");

                startActivityForResult(Intent.createChooser(intent, "upload image"), MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/edit_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    private boolean validation() {
        String name = editTextName.getText().toString().trim();
        String price = editTextPrice.getText().toString();
        String quantity = editTextQuantity.getText().toString();
        if (mCurrentUri == null && isEmpty(name) && isEmpty(quantity) && isEmpty(price)) {
            Toast.makeText(this, getString(R.string.product_info_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (isEmpty(name)) {
            Toast.makeText(this, "Enter Product Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (isEmpty(quantity)) {
            Toast.makeText(this, "Enter Product Quantity", Toast.LENGTH_SHORT).show();
            return false;
        } else if (isEmpty(price)) {
            Toast.makeText(this, "Enter Product Price", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validation()) {
                    insert();
                    //Exit Activity
                    finish();
                } else {
                    // Show a dialog that notifies the user they have unsaved item
                    showUnsavedDialog();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditeActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditeActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.donot_insert);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void insert() {
        String name = editTextName.getText().toString().trim();
        String quantityString = editTextQuantity.getText().toString().trim();
        String priceString = editTextPrice.getText().toString().trim();
        byte[] img = imageViewToBitmap(imageView);

        insertProduct(name, priceString, quantityString, mSize, img);
    }

    private byte[] imageViewToBitmap(ImageView img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photo = baos.toByteArray();
        return photo;
    }

    private void insertProduct(String name, String price, String quantity, int size, byte[] img) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_IMAGE, img);
        values.put(ProductEntry.COLUMN_SIZE, size);
        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.editor_insert_product_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter sizeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSizeSpinner.setAdapter(sizeSpinnerAdapter);

        // Set the integer mSize to the constant values
        mSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!isEmpty(selection)) {
                    if (selection.equals(getString(R.string.small_size))) {
                        mSize = ProductEntry.SMALL;
                    } else if (selection.equals(getString(R.string.medium_size))) {
                        mSize = ProductEntry.MEDIUM;
                    } else {
                        mSize = ProductEntry.LARGE;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSize = 0;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SIZE,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_IMAGE};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int sizeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SIZE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);


            final String productName = cursor.getString(nameColumnIndex);
            final int productquantity = cursor.getInt(quantityColumnIndex);
            final String productPrice = cursor.getString(priceColumnIndex) + "$";
            final int productsize = cursor.getInt(sizeColumnIndex);
            final byte[] imagePath = cursor.getBlob(imageColumnIndex);

            // Update the views on the screen with the values from the database
            editTextName.setText(productName);
            editTextQuantity.setText(Integer.toString(productquantity));
            editTextPrice.setText(productPrice);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imagePath, 0, imagePath.length));

            switch (productsize) {
                case ProductEntry.LARGE:
                    mSizeSpinner.setSelection(2);
                    break;
                case ProductEntry.MEDIUM:
                    mSizeSpinner.setSelection(1);
                    break;
                default:
                    mSizeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editTextName.setText("");
        editTextPrice.setText("");
        editTextQuantity.setText("");
        mSizeSpinner.setSelection(0);
        imageView.setImageDrawable(null);
    }

    public void orderMore(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.send_email);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String message = getString(R.string.message) + editTextName.getText().toString().trim() + "\n" + getString(R.string.address) +
                        "\n" + getString(R.string.thanks);
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:supplier.m@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject) + " " + editTextName.getText().toString().trim());
                intent.putExtra(Intent.EXTRA_TEXT, message);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton(R.string.no, null);
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void increaseQ(View view) {
        String s = editTextQuantity.getText().toString().trim();
        int Q;
        if (s.isEmpty()) {
            Q = 0;
        } else {
            Q = Integer.parseInt(s);
        }
        editTextQuantity.setText(String.valueOf(Q + 1));
    }

    public void decreaseQ(View v) {
        String s = editTextQuantity.getText().toString().trim();
        int Q;
        if (s.isEmpty()) {
            return;
        } else if (s.equals("0")) {
            return;
        } else {
            Q = Integer.parseInt(s);
            editTextQuantity.setText(String.valueOf(Q - 1));
        }
    }
}
