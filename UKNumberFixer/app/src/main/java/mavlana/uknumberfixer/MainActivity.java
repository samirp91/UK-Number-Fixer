package mavlana.uknumberfixer;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private static final int LOCATION_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        LOCATION_PERMISSIONS);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        LOCATION_PERMISSIONS);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting", Toast.LENGTH_SHORT).show();
                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (cur.getCount() > 0){
                    while (cur.moveToNext()){
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        if (cur.getInt(cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0){
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if (phoneNo.startsWith("07")){
                                    phoneNo = "+44" + phoneNo.substring(1);
                                    String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                                    String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
                                    ops.add(android.content.ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                                            .withSelection(where, numberParams)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNo)
                                            .build());

                                    try {
                                        cr.applyBatch(ContactsContract.AUTHORITY, ops);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    } catch (OperationApplicationException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }

                        }
                    }
                }
                Toast.makeText(MainActivity.this, "Done!", Toast.LENGTH_SHORT).show();
            }

        });

    }
}
