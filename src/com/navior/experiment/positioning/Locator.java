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
 * @date 26/08/13
 */
package com.navior.experiment.positioning;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Locator {
  private Handler handler;
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

  Locator(Context context, HashMap<String, Star> starMap) {
    handler = new Handler();
    rssiMap = new HashMap<String, ArrayList< RssiRecord >>();

    Iterator<String> iterator = starMap.keySet().iterator();
    while(iterator.hasNext()) {
      rssiMap.put(iterator.next(), new ArrayList<RssiRecord>());
    }

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if( !mBluetoothAdapter.isEnabled() ) {
      // TODO tell caller to open bluetooth
    }
    BluetoothGattAdapter.getProfileProxy(context, mServiceListener, BluetoothGattAdapter.GATT);
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
          }
        }
      };
      t.start();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if(mBluetoothGatt != null) {
      mBluetoothGatt.stopScan();
    }
    BluetoothGattAdapter.closeProfileProxy( BluetoothGattAdapter.GATT, mBluetoothGatt );
    super.finalize();
  }

  void clearRecord() {
    Iterator<String> iterator = rssiMap.keySet().iterator();
    while (iterator.hasNext()) {
      String name = iterator.next();
      rssiMap.get(name).clear();
    }
  }
}
