package com.navior.experiment.positioning;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.*;
import java.util.*;

public class SettingActivity extends Activity {

  private HashMap<String, SettingLine> layoutMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

    layoutMap = new HashMap<String, SettingLine>();
    for( int i = 21; i < 33; i++ ) {
      SettingLine line = new SettingLine( this, "876543" + i );
      ((ViewGroup) findViewById( R.id.root ) ).addView( line );
      layoutMap.put("876543"+ i, line);
    }

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader( openFileInput("stars") ) );
      while (reader.ready()){
        String line = reader.readLine();
        String[] parts = line.split(" ");
        String name = parts[0];
        String x = parts[1];
        String y = parts[2];
        layoutMap.get(name).enable();
        layoutMap.get(name).setInputX(Integer.parseInt(x));
        layoutMap.get(name).setInputY(Integer.parseInt(y));
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();  //TODO
    } catch (IOException e) {
      e.printStackTrace();  //TODO
    }

    Button button = (Button) findViewById(R.id.save);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        PrintWriter writer = null;
        try {
          writer = new PrintWriter(SettingActivity.this.openFileOutput( "stars", MODE_PRIVATE ));
        } catch (FileNotFoundException e) {
          e.printStackTrace();  //TODO
        }

        Iterator<String> keySet = layoutMap.keySet().iterator();
        while (keySet.hasNext()) {
          String name = keySet.next();
          if(layoutMap.get(name).getCheckBox().isChecked()){
            SettingLine layout = layoutMap.get(name);
            int x = layout.getInputX();
            int y = layout.getInputY();
            Star star = new Star(name, x, y);
            writer.write(name + " " + x + " " + y + "\n");
          }
        }
        writer.close();
        SettingActivity.this.setResult(RESULT_OK);
        SettingActivity.this.finish();
      }
    });
	}

  class SettingLine extends LinearLayout {
    final private CheckBox checkBox;
    private TextView textView;
    private EditText x;
    private EditText y;
    final private String starname;

    public SettingLine(Context context, final String name) {
      super(context);

      this.starname = name;

      setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      setOrientation(HORIZONTAL);

      checkBox = new CheckBox(context);
      checkBox.setOnClickListener( new OnClickListener() {
        @Override
        public void onClick(View view) {
          if(checkBox.isChecked()) {
            layoutMap.put(starname, SettingLine.this);
            x.setEnabled(true);
            y.setEnabled(true);
          }
          else {
            layoutMap.remove(starname);
            x.setEnabled(false);
            y.setEnabled(false);
          }
        }
      });
      addView(checkBox);
      textView = new TextView(context);
      textView.setText(name);
      addView(textView);
      x = new EditText(context);
      x.setHint("x\t\t\t\t\t");
      x.setInputType(InputType.TYPE_CLASS_NUMBER);
      x.setEnabled(false);
      addView(x);
      y = new EditText(context);
      y.setHint("y\t\t\t\t\t");
      y.setInputType(InputType.TYPE_CLASS_NUMBER);
      y.setEnabled(false);
      addView(y);
    }

    public CheckBox getCheckBox() {
      return checkBox;
    }

    public int getInputX() {
      int result = -1;
      try {
        result = Integer.parseInt( x.getEditableText().toString() );
      } catch (NumberFormatException e ) {

      }
      return result;
    }

    public void setInputX(int x) {
      this.x.setText( x + "" );
    }

    public int getInputY() {
      int result = -1;
      try {
        result = Integer.parseInt( y.getEditableText().toString() );
      } catch (NumberFormatException e ) {

      }
      return result;
    }

    public void setInputY(int y) {
      this.y.setText( y + "" );
    }

    public void enable() {
      this.checkBox.setChecked(true);
      this.x.setEnabled(true);
      this.y.setEnabled(true);
    }
  }
}