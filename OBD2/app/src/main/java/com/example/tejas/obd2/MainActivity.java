package com.example.tejas.obd2;

        import android.app.AlertDialog;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.DialogInterface;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.ArrayAdapter;
        import android.widget.Toast;

        import com.github.pires.obd.commands.ObdCommand;
        import com.github.pires.obd.commands.SpeedCommand;
        import com.github.pires.obd.commands.engine.RPMCommand;
        import com.github.pires.obd.commands.protocol.EchoOffCommand;
        import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
        import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
        import com.github.pires.obd.commands.protocol.TimeoutCommand;
        import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
        import com.github.pires.obd.enums.ObdProtocols;
        import com.github.pires.obd.commands.control.VinCommand;


        import com.github.nkzawa.socketio.client.IO;
        import com.github.nkzawa.socketio.client.Socket;

        import java.net.URISyntaxException;


        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.Set;
        import java.util.UUID;





public class MainActivity extends AppCompatActivity {
    //Declaring RPM and Speed
    public String RPM="";
    public String SPEED="";
    public String VIN="";


    //Connection TO server
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.9:3000");
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Server Connection Failed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSocket.connect();
        test();
    }




    void test(){
        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set <BluetoothDevice>pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));
        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public static final String TAG ="Details" ;
            public static final String TAG1 ="Arr" ;

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = (String) devices.get(position);
                // TODO save deviceAddress
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                BluetoothSocket socket = null;
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    Toast.makeText(MainActivity.this, "Worked", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    socket.connect();
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                RPMCommand engineRpmCommand = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();
                VinCommand vinCommand = new VinCommand();
                while (!Thread.currentThread().isInterrupted())
                {
                    try {
                        engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        vinCommand.run(socket.getInputStream(), socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // TODO handle commands result
                    RPM = engineRpmCommand.getFormattedResult();
                    SPEED = speedCommand.getFormattedResult();
                    VIN= vinCommand.getFormattedResult();
                    mSocket.emit("send message", SPEED, RPM , VIN);
                    Log.d(TAG, "RPM: " + RPM);
                    Log.d(TAG, "Speed: " + SPEED);
                    Log.d(TAG, "VIN" + VIN);
                }
            }


        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }
}
