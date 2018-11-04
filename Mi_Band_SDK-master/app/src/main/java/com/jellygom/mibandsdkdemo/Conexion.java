package com.jellygom.mibandsdkdemo;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Tulio on 05-10-2017.
 */

public class Conexion {
    private Connection conexion;

    private Conexion(String ip) throws ClassNotFoundException, SQLException {
        StrictMode.ThreadPolicy policy= new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(policy);
        Class.forName("com.mysql.jdbc.Driver");
        setConexion(DriverManager.getConnection("jdbc:mysql://"+ip+"/rutabd","prueba","prueba"));
    }
    public static synchronized Conexion newInstance(String ip) throws SQLException, ClassNotFoundException {
        return new Conexion(ip);
    }
    public void desconectar() throws SQLException {
        getConexion().close();
    }
    public Connection getConexion() {
        return conexion;
    }

    private void setConexion(Connection conexion) {
        this.conexion = conexion;
    }
}
