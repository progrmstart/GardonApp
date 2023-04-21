package com.example.gardonapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Déclaration des variables de la vue et du Bluetooth
    private TextView textViewStatus;
    private TextView textViewError;
    private Button buttonConnect;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice hc06Device;
    private BluetoothSocket bluetoothSocket;
    private UUID hc06UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues et de l'adaptateur Bluetooth
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewError = findViewById(R.id.textViewError);
        buttonConnect = findViewById(R.id.buttonConnect);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Vérification des permissions Bluetooth et demande si nécessaire
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }
        // Gestion du clic sur le bouton de connexion
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findHC06Device();
                if (hc06Device != null) {
                    connectToDevice();
                } else {
                    textViewError.setText("HC-06 non trouvé");
                }
            }
        });
    }
    // Recherche de l'appareil HC-06 parmi les appareils appairés
    private void findHC06Device() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-06")) {
                    hc06Device = device;
                    break;
                }
            }
        }
    }
    // Connexion à l'appareil HC-06
    private void connectToDevice() {
        try {
            // Fermez la connexion précédente si elle existe
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothSocket.close();
            }

            // Création d'un socket Bluetooth pour se connecter à l'appareil HC-06
            ParcelUuid[] uuids = hc06Device.getUuids();
            bluetoothSocket = hc06Device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            Thread.sleep(500);
            bluetoothSocket.connect();
            // Mise à jour du statut de connexion
            if (bluetoothSocket.isConnected()) {
                textViewStatus.setText("Connecté");
                textViewError.setText("");
            } else {
                textViewStatus.setText("Non connecté");
                textViewError.setText("Erreur de connexion");
            }
        } catch (IOException e) {
            e.printStackTrace();
            textViewStatus.setText("Non connecté");
            textViewError.setText("Erreur de connexion: " + e.getMessage());
        } catch (InterruptedException e) {
            textViewStatus.setText("Non connecté");
            textViewError.setText("Erreur de connexion: " + e.getMessage());
        }
    }


    // Fermeture du socket Bluetooth lors de la destruction de l'activité
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
