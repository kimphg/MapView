package com.SeaMap.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.object.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.abs;

public class SeaMap  extends PolygonsView {

    List<Text> ListPlace = new ArrayList<>();

//    Paint textPaint = new Paint();
//    Path mPath= new Path();
//    Paint cusPaint = new Paint();

    public SeaMap(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

    }


}
