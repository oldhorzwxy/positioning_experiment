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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LocatingActivity extends Activity {

  private Locator locator;
  private HashMap<String, Star> starMap;
  private MapGraph mapGraph;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView( R.layout.activity_locating );

    starMap = new HashMap<String, Star>();

    Button set_stars = (Button) findViewById(R.id.set_stars);
    set_stars.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(LocatingActivity.this, SettingActivity.class);
        startActivity(intent);
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
        //TODO
      }
    });
    Button quit = (Button)findViewById(R.id.quit);
    quit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        LocatingActivity.this.finish();
      }
    });

    mapGraph = new MapGraph(this);
    ( (ViewGroup)findViewById(R.id.result_area) ).addView(mapGraph);
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
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    locator = new Locator(this, starMap);
  }

  @Override
  protected void onDestroy() {
    try {
      locator.finalize();
    } catch (Throwable throwable) {
      throwable.printStackTrace();  //TODO
    }
    super.onDestroy();
  }

  void loadLocationResult(HashMap<String, ArrayList<RssiRecord>> recordMap) {
    HashMap<String, DrawPoint> pointMap = new HashMap<String, DrawPoint>();
    Iterator<String> iterator = recordMap.keySet().iterator();
    int max = -100;
    String maxName = null;
    float totalR = 1f;
    while (iterator.hasNext()) {
      String name = iterator.next();
      ArrayList<RssiRecord> recordList = recordMap.get(name);
      int average = 0;
      for (RssiRecord record : recordList) {
        average += record.getRssi();
      }
      average /= recordList.size();

      DrawPoint point = new DrawPoint();
      point.label = name;
      point.rssi = average;
      point.r = (int)getDistance(average);
      point.x = starMap.get(name).getX();
      point.y = starMap.get(name).getY();
      point.color = Color.BLACK;

      totalR *= point.r;

      pointMap.put(name, point);

      if(max < average) {
        max = average;
        maxName = name;
      }
    }
    pointMap.get(maxName).color = Color.RED;

    // draw gravity point
    float gravityX = 0;
    float gravityY = 0;
    iterator = recordMap.keySet().iterator();
    while (iterator.hasNext()) {
      String name = iterator.next();
      DrawPoint p = pointMap.get(name);
      gravityX += totalR / p.r * p.x;
      gravityY += totalR / p.r * p.y;
    }
    gravityX /= totalR;
    gravityY /= totalR;
    DrawPoint point = new DrawPoint();
    point.x = (int)gravityX;
    point.y = (int)gravityY;
    point.color = Color.GREEN;
    point.label = "gravity";
    pointMap.put("gravity", point);

    mapGraph.setPointMap(pointMap);
    mapGraph.postInvalidate();
  }

  private float getDistance(int rssi) {
    return 100 * (float)Math.pow(2, (-55 - rssi) / 5);
  }
}
