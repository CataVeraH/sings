package com.jellygom.mibandsdkdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

public class Principal extends AppCompatActivity {

    Toolbar toolbar;
    Button button;
    EditText superior_txt;
    EditText inferior_txt;
    EditText numero_txt;

    String limite_superior;
    String limite_inferior;
    String numero_emergencia;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        button = (Button) findViewById(R.id.btnContinuar_principal);
        superior_txt = (EditText) findViewById(R.id.txtLimite_superior);
        inferior_txt = (EditText) findViewById(R.id.txtLimite_inferior);
        numero_txt = (EditText) findViewById(R.id.txtNumero_Emergencia);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }


    }

    public void continuar(View view)
    {
        limite_inferior = inferior_txt.getText().toString();
        limite_superior = superior_txt.getText().toString();
        numero_emergencia = numero_txt.getText().toString();

        if(limite_superior != null && limite_inferior!=null)
        {
            Bundle bundle = new Bundle();
            bundle.putString("superior",limite_superior);
            bundle.putString("inferior",limite_inferior);
            bundle.putString("numero",numero_emergencia);

            Intent mainIntent = new Intent(this,MainActivity.class);
            mainIntent.putExtras(bundle);
            startActivity(mainIntent);
        }
        else
        {
            Toast.makeText(this, "Debe Ingresar ambos parametros", Toast.LENGTH_SHORT).show();
        }
    }

}
