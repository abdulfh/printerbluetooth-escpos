package com.example.bluetoothprinter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) this.findViewById(R.id.button_bluetooth);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printBluetooth();
            }
        });
    }

    public static final int PERMISSION_BLUETOOTH = 1;
    private void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            this.printIt(BluetoothPrintersConnections.selectFirstPaired());
        }
    }
   public void printIt(DeviceConnection printerConnection) {
       try {
           SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
           final EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);
           List<Bitmap> segments = new ArrayList<Bitmap>();
           final StringBuilder textToPrint = new StringBuilder();
           Picasso.get().load("https://dummyimage.com/600x1000/000/fff.png").into(new Target() {
               @Override
               public void onBitmapLoaded(Bitmap ImgBitmap, Picasso.LoadedFrom from) {
                   int width = ImgBitmap.getWidth();
                   int height = ImgBitmap.getHeight();
                   int pixel = 254;

                   for(int y = 0; y < height; y += pixel) {
                       Bitmap newBitmap = Bitmap.createBitmap(ImgBitmap, 0, y, width, (y + pixel >= height) ? height - y : pixel);
                       textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, newBitmap) + "</img>\n");
                   }
               }

               @Override
               public void onBitmapFailed(Exception e, Drawable errorDrawable) {

               }

               @Override
               public void onPrepareLoad(Drawable placeHolderDrawable) {

               }
           });

           Log.d("PT", "getAsyncEscPosPrinter: " + textToPrint);
           printer.printFormattedText(textToPrint.toString());
       }  catch (EscPosConnectionException e) {
           e.printStackTrace();
           new AlertDialog.Builder(this)
                   .setTitle("Broken connection")
                   .setMessage(e.getMessage())
                   .show();
       } catch (EscPosParserException e) {
           e.printStackTrace();
           new AlertDialog.Builder(this)
                   .setTitle("Invalid formatted text")
                   .setMessage(e.getMessage())
                   .show();
       } catch (EscPosEncodingException e) {
           e.printStackTrace();
           new AlertDialog.Builder(this)
                   .setTitle("Bad selected encoding")
                   .setMessage(e.getMessage())
                   .show();
       } catch (EscPosBarcodeException e) {
           e.printStackTrace();
           new AlertDialog.Builder(this)
                   .setTitle("Invalid barcode")
                   .setMessage(e.getMessage())
                   .show();
       }
   }
}