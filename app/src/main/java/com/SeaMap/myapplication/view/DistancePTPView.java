package com.SeaMap.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.object.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnItemClick;

public class DistancePTPView extends View {

    private List<Text> listTextRoute = new ArrayList<Text>();
    private Paint linePaint = new Paint();

    private int mDistance;

    public DistancePTPView(Context context) {
        super(context);
        mDistance = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = listTextRoute.size();
        for(int i = 0; i< size; i++){
            float [] coor = listTextRoute.get(i).getCoordinate();
            Point p = SeaMap.ConvWGSToScrPoint(coor[0], coor[1]);
            Bitmap bitmap = createBitmapFromView(i + 1);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(bitmap,p.x, p.y,locationPaint);

        }

        linePaint.setColor(Color.RED);
    }

    public List<Text> getListCoor() {
        return listTextRoute;
    }

    public void setListCoor(List<Text> listTextRoute) {
        this.listTextRoute = listTextRoute;
    }

    public int getmDistance() {
        return mDistance;
    }


    public Bitmap createBitmapFromView(int number_of_place) {
        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker, null);

        TextView text = v.findViewById(R.id.number_of_plc);
        text.setText(number_of_place +"");

        v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
                v.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);


        return bitmap;
    }

}
