/**
 * ==============================BEGIN_COPYRIGHT===============================
 * ===================NAVIOR CO.,LTD. PROPRIETARY INFORMATION==================
 * This software is supplied under the terms of a license agreement or
 * nondisclosure agreement with NAVIOR CO.,LTD. and may not be copied or
 * disclosed except in accordance with the terms of that agreement.
 * ==========Copyright (c) 2003 NAVIOR CO.,LTD. All Rights Reserved.===========
 * ===============================END_COPYRIGHT================================
 *
 * @author wangxiayang
 * @date 21/08/13
 */
package com.navior.experiment.positioning;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.samsung.android.sdk.bt.gatt.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LocatingActivity extends Activity {

  private final static int FOR_RESULT = 0;
  private Handler handler;
  private Locator locator;
  private List<DrawPoint> orderedList;
  private HashMap<String, Star> starMap;

  private boolean hasResult = false;
  private int resultX;
  private int resultY;

  MapGraph m;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView( R.layout.activity_locating );

    handler = new Handler();
    orderedList = new ArrayList<DrawPoint>();

    starMap = new HashMap<String, Star>();


    Button set_stars = (Button) findViewById(R.id.set_stars);
    set_stars.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(LocatingActivity.this, SettingActivity.class);
        startActivityForResult(intent, FOR_RESULT);
      }
    });
    Button start_locating = (Button) findViewById(R.id.start_locating);
    start_locating.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        locator.startLocating();
      }
    });
    Button clear = (Button) findViewById(R.id.clear);
    clear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        orderedList.clear();
        m.postInvalidate();
      }
    });
    Button quit = (Button)findViewById(R.id.quit);
    quit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        LocatingActivity.this.finish();
      }
    });

    m = new MapGraph(this);
    ( (ViewGroup)findViewById(R.id.result_area) ).addView(m);
  }

  @Override
  protected void onResume() {
    super.onResume();

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader( openFileInput("stars") ) );
      while (reader.ready()){
        String line = reader.readLine();
        String[] parts = line.split(" ");
        String name = parts[0];
        String x = parts[1];
        String y = parts[2];
        starMap.put(name, new Star(name, Integer.parseInt(x), Integer.parseInt(y)));
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();  //TODO
    } catch (IOException e) {
      e.printStackTrace();  //TODO
    }

    locator = new Locator();
    m.postInvalidate();
  }

  class ResultTable extends TableLayout {

    private HashMap< String, ResultTableRow > rows;
    private List< String > nameList;

    ResultTable(Context context, List< String > nameList) {
      super(context);
      this.nameList = nameList;
      rows = new HashMap<String, ResultTableRow>();
      ResultTableRow head = new ResultTableRow(context);
      head.addBlock("name");
      rows.put("head", head);
      addView(head);
      for(int i = 0; i < nameList.size(); i++) {
        ResultTableRow row = new ResultTableRow(context);
        row.addBlock(nameList.get(i));
        rows.put(nameList.get( i ), row);
        addView(row);
      }
    }

    void clear() {
      rows.get("head").removeAllViews();
      rows.get("head").addBlock("name");
      for(int i = 0; i < nameList.size(); i++) {
        rows.get(nameList.get( i )).removeAllViews();
        rows.get(nameList.get( i )).addBlock(nameList.get( i ));
      }
    }

    class ResultTableRow extends TableRow {

      ResultTableRow(Context context) {
        super(context);
      }

      void addBlock(String content) {
        TextView t = new TextView(getContext());
        t.setText(content);
        addView(t);
      }
    }
  }

  private class Locator {

    private HashMap< String, ArrayList< RssiRecord > > rssiMap;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mCallback = new BluetoothGattCallback() {

      @Override
      public void onScanResult( BluetoothDevice bluetoothDevice, int rssi, byte[] bytes ) {
        if(rssiMap.containsKey( bluetoothDevice.getName() )) {
          RssiRecord record = new RssiRecord(bluetoothDevice.getName(), rssi);
          rssiMap.get( bluetoothDevice.getName() ).add(record);
        }
      }
    };
    private BluetoothProfile.ServiceListener mServiceListener = new BluetoothProfile.ServiceListener() {
      @Override
      public void onServiceConnected(int profile, BluetoothProfile bluetoothProfile) {
        if( profile == BluetoothGattAdapter.GATT ) {
          mBluetoothGatt = ( BluetoothGatt )bluetoothProfile;
          mBluetoothGatt.registerApp( mCallback );
        }
      }

      @Override
      public void onServiceDisconnected(int i) {
        mBluetoothGatt.unregisterApp();
        mBluetoothGatt = null;
      }
    };

    Locator() {
      this.rssiMap = new HashMap<String, ArrayList< RssiRecord >>();
      Iterator<String> iterator = starMap.keySet().iterator();
      while(iterator.hasNext()) {
        rssiMap.put(iterator.next(), new ArrayList<RssiRecord>());
      }
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if( !mBluetoothAdapter.isEnabled() ) {
        // TODO tell caller to open bluetooth
      }
      BluetoothGattAdapter.getProfileProxy(LocatingActivity.this, mServiceListener, BluetoothGattAdapter.GATT);
    }

    void startLocating() {

      if( mBluetoothGatt.startScan() ) {
        Thread t = new Thread(){
          @Override
          public void run() {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              mBluetoothGatt.stopScan();
              Locator.this.calculateLocation();
            }
          }
        };
        t.start();
      }
    }

    void calculateLocation() {
      Iterator<String> iterator = rssiMap.keySet().iterator();
      while (iterator.hasNext()) {
        String name = iterator.next();
        if(!starMap.containsKey(name)){
          continue;
        }
        List<RssiRecord> list = rssiMap.get(name);
        float average = 0;
        for(int i = 0; i < list.size(); i++) {
          average += list.get(i).getRssi();
        }
        average /= list.size();
        if( (int)average == 0 ) {
          average = -100;
        }
        Star s = starMap.get(name);
        DrawPoint point = new DrawPoint();
        point.x = s.getX();
        point.y = s.getY();
        point.r = (int)getDistance(average);
        point.rssi = (int)average;
        point.label = name;
        orderedList.add(point);
      }
      m.postInvalidate();
    }

    float getDistance(float rssi) {
      return 100 * (float)Math.pow(2, (-55 - rssi) / 5);
    }

    @Override
    protected void finalize() throws Throwable {
      mBluetoothGatt.stopScan();
      BluetoothGattAdapter.closeProfileProxy( BluetoothGattAdapter.GATT, mBluetoothGatt );
      super.finalize();
    }
  }

  class RssiRecord {
    private int rssi;
    private String starName;

    RssiRecord(String starName, int rssi) {
      setRssi(rssi);
      setStarName(starName);
    }

    private int getRssi() {
      return rssi;
    }

    private void setRssi(int rssi) {
      this.rssi = rssi;
    }

    private String getStarName() {
      return starName;
    }

    private void setStarName(String starName) {
      this.starName = starName;
    }
  }

  class DrawPoint{
    public int x;
    public int y;
    public int r;
    public String label;
    public int rssi;
  }

  class MapGraph extends View {

    private final static int MAX_Y = 1920;
    private final static int MAX_X = 1080;
    private final static int PADDING_X = 40;
    private final static int PADDING_Y = 50;

    private final static int MAX_COLUMN = 10;
    private final static int MAX_ROW = 20;

    private final static int BLOCK_SIZE = 100;

    private final static int MARK_LENGTH = 20;

    MapGraph(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      Paint painter = new Paint();
      canvas.drawLine( MARGIN_X, MARGIN_Y, MARGIN_X + MAX_COLUMN * BLOCK_SIZE, MARGIN_Y, painter );
      canvas.drawLine( MARGIN_X, MARGIN_Y, MARGIN_X, MARGIN_Y + MAX_ROW * BLOCK_SIZE, painter );
      for(int i = 0; i < MAX_COLUMN; i++) {
        canvas.drawLine(MARGIN_X + i * BLOCK_SIZE, MARGIN_Y, MARGIN_X + i * BLOCK_SIZE, MARGIN_Y - MARK_LENGTH, painter);
        canvas.drawText("" + i, MARGIN_X + 5 + i * BLOCK_SIZE, MARGIN_Y + 5, painter);
      }
      for(int i = 0; i < MAX_ROW; i++) {
        canvas.drawLine(MARGIN_X, MARGIN_Y + i * BLOCK_SIZE, MARGIN_X - MARK_LENGTH, MARGIN_Y + i * BLOCK_SIZE, painter);
        canvas.drawText("" + i, 10, MARGIN_Y + i * BLOCK_SIZE - 5, painter);
      }

      int max = -100;
      for(int i = 0; i < orderedList.size(); i++) {
         if(orderedList.get(i).rssi > max) {
           max = orderedList.get(i).rssi;
         }
      }

      for(int i = 0; i < orderedList.size(); i++) {
        DrawPoint p = orderedList.get(i);
        Paint paint = new Paint();
        canvas.drawCircle(MARGIN_X + p.x * BLOCK_SIZE / 100, MARGIN_Y + p.y * BLOCK_SIZE / 100, 3, paint);
        canvas.drawText(p.label + "/" + p.rssi, MARGIN_X + p.x * BLOCK_SIZE / 100 + 20, MARGIN_Y + p.y * BLOCK_SIZE / 100 + 20, paint );
        paint.setStyle(Paint.Style.STROKE);
        if( p.rssi == max ) {
          paint.setColor(Color.RED);
        }
        canvas.drawCircle(MARGIN_X + p.x * BLOCK_SIZE / 100, MARGIN_Y + p.y * BLOCK_SIZE / 100, p.r * BLOCK_SIZE / 100, paint);
      }

      if( hasResult ) {

      }
    }
  }
}
