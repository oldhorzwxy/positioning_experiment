package com.navior.experiment.positioning;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

public class SettingActivity extends Activity {

  private HashMap<String, SettingLine> layoutMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

    LocationList.starList = new HashMap<String, Star>();

    layoutMap = new HashMap<String, SettingLine>();
    for( int i = 21; i < 33; i++ ) {
      SettingLine line = new SettingLine( this, "8765432" + i );
      ((ViewGroup) findViewById( R.id.root ) ).addView( line );
      layoutMap.put("876543"+ i, line);
    }

    Button button = (Button) findViewById(R.id.save);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Iterator<String> keySet = layoutMap.keySet().iterator();
        while (keySet.hasNext()) {
          String name = keySet.next();
          if(layoutMap.get(name).getCheckBox().isChecked()){
            SettingLine layout = layoutMap.get(name);
            int x = layout.getInputX();
            int y = layout.getInputY();
            Star star = new Star(name, x, y);
            LocationList.starList.put(name, star);
          }
        }
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

    public int getInputY() {
      int result = -1;
      try {
        result = Integer.parseInt( y.getEditableText().toString() );
      } catch (NumberFormatException e ) {

      }
      return result;
    }
  }
}