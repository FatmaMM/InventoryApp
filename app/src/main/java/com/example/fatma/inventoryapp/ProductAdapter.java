package com.example.fatma.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fatma.inventoryapp.data.ProductContract.ProductEntry;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProductAdapter extends CursorAdapter {
    ViewHolder holder;
    Context mContext;

    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        mContext = context;

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int sizeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SIZE);
        int imageColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE);

        final String productName = cursor.getString(nameColumnIndex);
        final int productquantity = cursor.getInt(quantityColumnIndex);
        final Float productPrice = cursor.getFloat(priceColumnIndex);
        final int productsize = cursor.getInt(sizeColumnIndex);
        String size;

        if (productsize == ProductEntry.SMALL) {
            size = context.getString(R.string.small_size);
        } else if (productsize == ProductEntry.MEDIUM) {
            size = context.getString(R.string.medium_size);
        } else
            size = context.getString(R.string.larg_size);

        holder = new ViewHolder(view);
        holder.nameTextView.setText(productName);
        holder.priceTextView.setText(NumberFormat.getCurrencyInstance().format(productPrice));
        holder.quantityTextView.setText(String.valueOf(productquantity));
        holder.sizeTextView.setText(size);

        final byte[] blob = cursor.getBlob(imageColumnIndex);

        if (blob != null) {
            holder.imageView.setImageBitmap(getImage(blob));
            holder.imageView.setVisibility(View.VISIBLE);
        } else
            holder.imageView.setVisibility(View.GONE);

        holder.sellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) {
                    Object obj = v.getTag();
                    String st = obj.toString();
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_NAME, productName);
                    values.put(ProductEntry.COLUMN_IMAGE, blob);
                    values.put(ProductEntry.COLUMN_QUANTITY, productquantity >= 1 ? productquantity - 1 : 0);
                    values.put(ProductEntry.COLUMN_PRICE, productPrice);
                    values.put(ProductEntry.COLUMN_SIZE, productsize);

                    Uri currentPetUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, Integer.parseInt(st));

                    int rowsAffected = mContext.getContentResolver().update(currentPetUri, values, null, null);
                    if (rowsAffected == 0 || productquantity == 0) {
                        Toast.makeText(mContext, mContext.getString(R.string.sell_product_failed) + productName + mContext.getString(R.string.order_more), Toast.LENGTH_SHORT).show();
                        showDialog();
                    }
                }
            }

            private void showDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.send_email);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = mContext.getString(R.string.message) + productName + "\n" + mContext.getString(R.string.address) +
                                "\n" + mContext.getString(R.string.thanks);
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:supplier.m@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.subject) + " " + productName);
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                            mContext.startActivity(intent);
                        }
                    }
                });
                builder.setNegativeButton(R.string.no,null);
                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        Object obj = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        holder.sellProduct.setTag(obj);
    }

    static class ViewHolder {
        @BindView(R.id.name)
        TextView nameTextView;
        @BindView(R.id.price)
        TextView priceTextView;
        @BindView(R.id.quantity)
        TextView quantityTextView;
        @BindView(R.id.size)
        TextView sizeTextView;
        @BindView(R.id.image)
        ImageView imageView;
        @BindView(R.id.sell_product)
        ImageView sellProduct;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
