package com.jellygom.mibandsdkdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jellygom.miband_sdk.MiBandIO.Listener.HeartrateListener;
import com.jellygom.miband_sdk.MiBandIO.Listener.NotifyListener;
import com.jellygom.miband_sdk.MiBandIO.Listener.RealtimeStepListener;
import com.jellygom.miband_sdk.MiBandIO.MibandCallback;
import com.jellygom.miband_sdk.MiBandIO.Model.UserInfo;
import com.jellygom.miband_sdk.Miband;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Miband miband;
    private BluetoothAdapter mBluetoothAdapter;
    private String ip;
    private TextView heart, step, battery;
    private TextView text;
    private Conexion con;
    private int limite_inferior;
    private int limite_superior;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;
    private int contador = 0;
    String numero_emergencia;

    private RealtimeStepListener realtimeStepListener = new RealtimeStepListener() {
        @Override
        public void onNotify(final int steps) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    step.setText(steps + " steps");
                    text.append(steps + " steps\n");
                }
            });
        }
    };

    private HeartrateListener heartrateNotifyListener = new HeartrateListener() {
        @Override
        public void onNotify(final int heartRate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if (heartRate > limite_inferior)

                    heart.setText(heartRate + " bpm");
                    text.setText(heartRate + " bpm\n" + text.getText());
                    //     new Insertar().execute(new Integer(heartRate));

                    if(heartRate<limite_inferior || heartRate>limite_superior)
                    {
                       if(contador<10)
                       {
                           contador ++;
                       }
                       else
                       {
                           contador = 0;
                           onClickLlamada();
                           miband.sendAlert(mibandCallback);

                       }
                    }

                    Log.d("Corazon2", "Paso por aqui");
                }

            });
        }
    };

    private final MibandCallback mibandCallback = new MibandCallback() {
        @Override
        public void onSuccess(Object data, int status) {
            switch (status) {
                case MibandCallback.STATUS_SEARCH_DEVICE:
                    Log.e(TAG, "성공: STATUS_SEARCH_DEVICE");
                    miband.connect((BluetoothDevice) data, this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Buscando...", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case MibandCallback.STATUS_CONNECT:
                    Log.e(TAG, "성공: STATUS_CONNECT");
                    miband.getUserInfo(this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case MibandCallback.STATUS_SEND_ALERT:
                    Log.e(TAG, "성공: STATUS_SEND_ALERT");
                    break;
                case MibandCallback.STATUS_GET_USERINFO:
                    Log.e(TAG, "성공: STATUS_GET_USERINFO");
                    UserInfo userInfo = new UserInfo().fromByteData(new byte[]{-81, 119, 19, -76, 1, 22, -76, -126, 0, 4, 0, 4, 0, 4, 0, 89, 117, 108, 105, -100});
                    miband.setUserInfo(userInfo, this);
                    break;
                case MibandCallback.STATUS_SET_USERINFO:
                    Log.e(TAG, "성공: STATUS_SET_USERINFO");
                    miband.setHeartRateScanListener(heartrateNotifyListener);
                    latidos();
                    break;
                case MibandCallback.STATUS_START_HEARTRATE_SCAN:
                    Log.e(TAG, "성공: STATUS_START_HEARTRATE_SCAN");
                    break;
                case MibandCallback.STATUS_GET_BATTERY:
                    Log.e(TAG, "성공: STATUS_GET_BATTERY");
                    final int level = (int) data;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            battery.setText(level + " % battery");
                            text.append(level + " % battery\n");
                        }
                    });
                    break;
                case MibandCallback.STATUS_GET_ACTIVITY_DATA:
                    Log.e(TAG, "성공: STATUS_GET_ACTIVITY_DATA");
                    final int steps = (int) data;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            step.setText(steps + " steps");
                            text.append(steps + " steps\n");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onFail(int errorCode, String msg, int status) {
            switch (status) {
                case MibandCallback.STATUS_SEARCH_DEVICE:
                    Log.e(TAG, "실패: STATUS_SEARCH_DEVICE");
                    break;
                case MibandCallback.STATUS_CONNECT:
                    Log.e(TAG, "실패: STATUS_CONNECT");
                    break;
                case MibandCallback.STATUS_SEND_ALERT:
                    Log.e(TAG, "실패: STATUS_SEND_ALERT");
                    break;
                case MibandCallback.STATUS_GET_USERINFO:
                    Log.e(TAG, "실패: STATUS_GET_USERINFO");
                    break;
                case MibandCallback.STATUS_SET_USERINFO:
                    Log.e(TAG, "실패: STATUS_SET_USERINFO");

                    break;
                case MibandCallback.STATUS_START_HEARTRATE_SCAN:
                    Log.e(TAG, "실패: STATUS_START_HEARTRATE_SCAN");
                    break;
                case MibandCallback.STATUS_GET_BATTERY:
                    Log.e(TAG, "실패: STATUS_GET_BATTERY");
                    break;
                case MibandCallback.STATUS_GET_ACTIVITY_DATA:
                    Log.e(TAG, "실패: STATUS_GET_ACTIVITY_DATA");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Bundle limites = getIntent().getExtras();

        limite_superior = Integer.parseInt(limites.getString("superior").toString());
        limite_inferior = Integer.parseInt(limites.getString("inferior").toString());
        numero_emergencia = limites.getString("numero");

        //findViewById(R.id.button_vive).setOnClickListener(this);
        //findViewById(R.id.button_steps).setOnClickListener(this);
        //findViewById(R.id.button_realtime_steps).setOnClickListener(this);
        //findViewById(R.id.button_battery).setOnClickListener(this);
        //findViewById(R.id.button_heart_start_one).setOnClickListener(this);
        //findViewById(R.id.button_heart_start_many).setOnClickListener(this);
        ip = getIntent().getStringExtra("ip");
        heart = (TextView) findViewById(R.id.heart);
        //step = (TextView) findViewById(R.id.steps);
        battery = (TextView) findViewById(R.id.battery);
        text = (TextView) findViewById(R.id.text);

        miband = new Miband(getApplicationContext());

        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        ((Button) findViewById(R.id.btnBateria)).setOnClickListener(this);
        miband = new Miband(getApplicationContext());

        miband.searchDevice(mBluetoothAdapter, this.mibandCallback);
        Log.d("Paso", "paso a este lugar");
        miband.setDisconnectedListener(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                miband.searchDevice(mBluetoothAdapter, mibandCallback);
            }
        });
        // ubicacion();
    }

    public void onClickLlamada() {
        String dial = "tel: "+numero_emergencia;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "No hay permiso para realizar llamadas", Toast.LENGTH_SHORT).show();

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

            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
  }

  public void latidos() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        funcionHilo();
      }
    }, 0, 10000);
  }
 /* public void ubicacion() {
    Timer timer = new Timer();
  //  timer.scheduleAtFixedRate(new TimerTask() {
      @RequiresApi(api = Build.VERSION_CODES.M)
      @Override
      public void run() {
        funcionUbi();
      }
    }, 0, 500);
  }*/

  @Override
  public void onClick(View view) {
    int i = view.getId();
    if (i == R.id.sincro) {
      UserInfo userInfo = new UserInfo().fromByteData(new byte[]{-81, 119, 19, -76, 1, 22, -76, -126, 0, 4, 0, 4, 0, 4, 0, 89, 117, 108, 105, -100});
      miband.setUserInfo(userInfo, this.mibandCallback);
    } else if (i == R.id.btnBateria) {
      miband.getBatteryLevel(this.mibandCallback);
    }


  }

  private Runnable accion = new Runnable() {
    @Override
    public void run() {
      miband.startHeartRateScan(0, mibandCallback);
    }
  };
  private Runnable bate = new Runnable() {
    @Override
    public void run() {
      miband.getBatteryLevel(mibandCallback);
    }
  };

  private void funcionHilo() {
    this.runOnUiThread(accion);
  }

/*  @RequiresApi(api = Build.VERSION_CODES.M)
  private void funcionUbi() {
    new InsertarLocacion().execute(getLocation());
  }*/

 /* private class Insertar extends AsyncTask<Integer, Void, Void> {

    @Override
    protected Void doInBackground(Integer... ints) {
      try {
        Log.d("Corazon2", "Entro");
        con = Conexion.newInstance(ip);
        String sql = "Insert into heartrate(rut,pulso) values(?,?)";
        PreparedStatement stm = con.getConexion().prepareStatement(sql);
        stm.setInt(1, 191153626);
        stm.setInt(2, ints[0]);
        Log.d("Corazon2", "Insertando dato" + ints[0]);
        stm.executeUpdate();
        stm.close();

      } catch (SQLException e) {
        Log.d("Error", e.getMessage());
      } catch (Exception e) {
        Log.d("Error", e.getMessage());
      } finally {
        if (con.getConexion() != null) {
          try {
            con.desconectar();
          } catch (java.sql.SQLException e) {
            Log.d("Error", e.getMessage());
          }
        }
      }
      return null;
    }

  }

  /*private class InsertarLocacion extends AsyncTask<LatLng, Void, Void> {

    @Override
    protected Void doInBackground(LatLng... ints) {
      try {
        Log.d("Corazon2", "Entro");
        con = Conexion.newInstance(ip);
        String sql = "INSERT INTO `location`(`latitud`, `longitud`, `persona_rut`) VALUES (?,?,?)";
        PreparedStatement stm = con.getConexion().prepareStatement(sql);
        stm.setDouble(1,ints[0].getLat());
        stm.setDouble(2,ints[0].getLon());
        stm.setInt(3, 191153626);
        Log.d("corazon 3", "Insertando dato");
        stm.executeUpdate();
        stm.close();

      } catch (SQLException e) {
        Log.d("Error", e.getMessage());
      } catch (Exception e) {
        Log.d("Error", e.getMessage());
      } finally {
        if (con.getConexion() != null) {
          try {
            con.desconectar();
          } catch (java.sql.SQLException e) {
            Log.d("Error", e.getMessage());
          }
        }
      }
      return null;
    }

  }
  @RequiresApi(api = Build.VERSION_CODES.M)
  public LatLng getLocation() {
    // Get the location manager
    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    String bestProvider = locationManager.getBestProvider(criteria, false);
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
      },123);
    }
    Location location = locationManager.getLastKnownLocation(bestProvider);
    Double lat,lon;
    try {
      lat = location.getLatitude ();
      lon = location.getLongitude ();
      return new LatLng(lat, lon);
    }
    catch (NullPointerException e){
      e.printStackTrace();
      return null;
    }
  }*/
}
