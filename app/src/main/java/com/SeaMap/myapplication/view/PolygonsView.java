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
import android.location.Location;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.Activity.MainActivity;
import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.object.Buoy;
import com.SeaMap.myapplication.object.Polyline;
import com.SeaMap.myapplication.object.Region;

import java.util.List;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class PolygonsView extends View {

    protected static float mlat = 18.32f;//lattitude of the center of the screen
    protected static float mlon = 105.43f;//longtitude of the center of the screen
    protected static float mScale = 3f;// 1km = mScale*pixcels
    protected static float viewLat = 18.32f;//lattitude of the center of the screen
    protected static float viewLon = 105.43f;//longtitude of the center of the screen
    protected static boolean MYLOCATION = false; // vi tri hien tai
    private static boolean SEARCHPLACE = false;
    private static boolean DIRECTIONS = false;
    float searchPlace_lon, searchPlace_lat;
    public int scrCtY,scrCtX;
    protected float shipLocationLon = 105.43f, shipLocationLat = 18.32f;
    private boolean lockDragging = false;// khóa không cho drag khi đang zoom
    private Paint landPaint = new Paint(), riverPaint = new Paint(), depthLinePaint = new Paint(), borderlinePaint = new Paint();
    private ScaleGestureDetector scaleGestureDetector;
    protected PointF dragStart,dragStop;
    protected Context mCtx;
//    protected Bitmap bitmapBouy;
    protected Paint buoyPaint = new Paint();
//    private int heightBuoy, widthBuoy;
    public PolygonsView(Context context) {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleLister());
        setOnLongClickListener(infoAcoordinate);
        mCtx = context;
        //bitmapBouy = getBitmap(R.drawable.buoy_object);
        borderlinePaint.setStrokeWidth(2);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        scrCtY = h / 2;
        scrCtX = w / 2;
        initPaints();
        invalidate();
    }

    private void initPaints() {
        landPaint.setAntiAlias(true);
        landPaint.setStyle(Paint.Style.FILL);
        landPaint.setColor(Color.rgb(201, 185, 123));

        riverPaint.setAntiAlias(true);
        riverPaint.setStyle(Paint.Style.FILL);
        riverPaint.setColor(Color.rgb(115, 178, 235));

        buoyPaint.setColor(Color.MAGENTA);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //dùng biến viewLat, viewLon để làm trơn sự dịch chuyển tâm màn hình
        viewLat += (mlat- viewLat)/2.0;
        viewLon += (mlon- viewLon)/2.0;
        if(abs(mlat- viewLat)+abs(mlon-viewLon) >0.001) invalidate();

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);
        //DRAW POLYGON
        for(int lon = (int) pointT3.x - 2; lon<= (int) pointT1.x + 2; lon++) {
            for (int lat = (int) pointT3.y - 2; lat <= (int) pointT1.y + 2; lat++) {
                Vector<Region> regions;
                if(mScale < 8) {
                    regions = ReadFile.BaseRegions.get(lon + "-" + lat);
                }
                else
                    regions = ReadFile.Poligons.get(lon + "-" + lat);
                if(regions == null) continue;
                for(int k =0;  k< regions.size() ; k++){
                    Region polygon = regions.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    float[] coordinate = polygon.getCoordinate();
                    Point point1 = ConvWGSToScrPoint(coordinate[0], coordinate[1]);
                    pathRegion.moveTo(point1.x, point1.y);
                    for (int i = 0; i < coordinate.length-1; i = i + 2) {
                        point1 = ConvWGSToScrPoint(coordinate[i], coordinate[i + 1]);
                        pathRegion.lineTo(point1.x, point1.y);
                    }
                    canvas.drawPath(pathRegion, landPaint);
                }
            }
        }

        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
            for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                String area = lon + "-" + lat;

                ////DRAW RIVER
                Vector<Region> river;
                if(mScale < 8) {
                    river = ReadFile.BasePlgRiver.get(area);
                }
                else river = ReadFile.PolygonRivers.get(area);
                if(river == null) continue;
                for(int k =0;  k< river.size() ; k++){
                    Region polygon = river.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    float[] coordinate = polygon.getCoordinate();
                    Point point1 = ConvWGSToScrPoint(coordinate[0], coordinate[1]);
                    pathRegion.moveTo(point1.x, point1.y);
                    for (int i = 0; i < coordinate.length-1; i = i + 2) {
                        point1 = ConvWGSToScrPoint(coordinate[i], coordinate[i + 1]);
                        pathRegion.lineTo(point1.x, point1.y);
                    }
                    canvas.drawPath(pathRegion, riverPaint);
                }

                //DrawBorder
                Vector<Polyline> border = ReadFile.Border_Map.get(area);
                if (border== null) continue;
                for (int k = 0; k < border.size(); k++) {
                    Polyline pl = border.get(k);
                    int size = pl.getCoordinate().length;
                    float pointis[] = new float[size * 2];
                    for (int i = 0; i < size - 2; i += 2) {
                        Point p1 = ConvWGSToScrPoint(pl.getCoordinate()[i], pl.getCoordinate()[i + 1]);
                        Point p2 = ConvWGSToScrPoint(pl.getCoordinate()[i + 2], pl.getCoordinate()[i + 3]);
                        pointis[2 * i] = (float) p1.x;
                        pointis[2 * i + 1] = (float) p1.y;
                        pointis[i * 2 + 2] = (float) p2.x;
                        pointis[i * 2 + 3] = (float) p2.y;
                    }
                    canvas.drawLines(pointis, borderlinePaint);
                }
            }
        }

        //Draw

        if( mScale >5){
            for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
                for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                    String area = lon + "-" + lat;
                    //draw polyline
                    Vector<Polyline> PL = ReadFile.PLines.get(area);
                    if (PL == null) continue;
                    for (int k = 0; k < PL.size(); k++) {
                        Polyline pl = PL.get(k);
                        int size = pl.getCoordinate().length;
                        float pointis[] = new float[size * 2];
                        for (int i = 0; i < size - 2; i += 2) {
                            Point p1 = ConvWGSToScrPoint(pl.getCoordinate()[i], pl.getCoordinate()[i + 1]);
                            Point p2 = ConvWGSToScrPoint(pl.getCoordinate()[i + 2], pl.getCoordinate()[i + 3]);
                            pointis[2 * i] = (float) p1.x;
                            pointis[2 * i + 1] = (float) p1.y;
                            pointis[i * 2 + 2] = (float) p2.x;
                            pointis[i * 2 + 3] = (float) p2.y;
                        }
                        int color = pl.getPen()[2];
                        int red = (int) color / 65536;
                        int green = (int) (color - red<<16) / 256;
                        int blue = (int) (color - red * 65536 - green * 256);
                        depthLinePaint.setColor(Color.rgb(red, green, blue));
                        canvas.drawLines(pointis, depthLinePaint);
                    }
                    if( mScale > 10) {
                        //draw buoy
                        Vector<Buoy> bouyVectors = ReadFile.listBuoys.get(area);
                        if (bouyVectors == null) continue;
                        for (int k = 0; k < bouyVectors.size(); k++) {
                            Buoy buoy = bouyVectors.get(k);
                            float[] coor = buoy.getCoordinates();
                            Point p = ConvWGSToScrPoint(coor[0], coor[1]);

                            //canvas.drawBitmap(bitmapBouy, p.x + widthBuoy, p.y + heightBuoy , buoyPaint);
                            canvas.drawCircle(p.x, p.y, 5, buoyPaint);
                        }
                    }
                }
            }
        }

        if (MYLOCATION) {
            //Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(shipLocationLon, shipLocationLat);
            Paint locationPaint = new Paint();
            locationPaint.setStyle(Paint.Style.FILL);
            locationPaint.setColor(Color.argb(120, 0, 120, 100));
            canvas.drawCircle(p1.x, p1.y, 10, locationPaint);
            //int height = mbitmap.getHeight();
            //int width = mbitmap.getWidth();

            //canvas.drawBitmap(mbitmap, p1.x - height/2, p1.y - width, locationPaint);
            //draw nearby ships
            if(nearbyShips!=null)
                if(nearbyShips.size()>0) {
                    locationPaint.setColor(Color.argb(150, 0, 0, 100));
                    for (Location ship : nearbyShips) {
                        Point pship = ConvWGSToScrPoint((float) ship.getLongitude(), (float) ship.getLatitude());
                        canvas.drawCircle(pship.x, pship.y, 7, locationPaint);
                    }
                }
        }

        if(DIRECTIONS) {
            Point p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            Point p2 = ConvWGSToScrPoint(shipLocationLon, shipLocationLat);
            Paint searchPl = new Paint();
            searchPl.setColor(Color.RED);
            searchPl.setStrokeWidth(3);
            float pointf[] = {p1.x, p1.y, p2.x, p2.y};
            canvas.drawLines(pointf, searchPl);
            SEARCHPLACE = true;
        }

        if(SEARCHPLACE){
            Point p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            int height = mbitmap.getHeight();
            int wight = mbitmap.getWidth();
            Paint searchPl = new Paint();
            canvas.drawBitmap(mbitmap, p1.x - height/2, p1.y - wight/2, searchPl);
        }
    }


    float PointDistancePixels(PointF p1,PointF p2)
    {
        float dx = abs(p1.x-p2.x);
        float dy = abs(p1.y-p2.y);
        return (float) sqrt(dx*dx+dy*dy);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dragStart = new PointF(event.getX(),event.getY());
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            lockDragging = false;
        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            dragStop = new PointF(event.getX(),event.getY());
            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
            dragStart = dragStop;
            if(lockDragging)return true;//bỏ qua nếu đang khóa drag
            if(PointDistancePixels(dragStop,dragStart)>scrCtX*0.5)return true;//không cho drag màn hình quá nhanh
            mlat=newLatLon.y;
            mlon=newLatLon.x;

            //MainActivity.polygonsView.refreshDrawableState();
            invalidate();
            switch (MainActivity.CHOOSE_DISTANE_OR_ROUTE){
                case 0:{
                    break;
                }
                default:{
                    MainActivity.distancePTPView.invalidate();
                    break;
                }
            }
        }
        return true;
    }

    private class ScaleLister extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector sgd){
            mScale *= sgd.getScaleFactor();
            mScale = Math.max(0.5f, Math.min(mScale, 150));
            lockDragging = true;
            invalidate();
            return true;
        }
    }

    private View.OnLongClickListener infoAcoordinate = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    };

//    //Ham dung cannvas de ve buoy tu drawable/buoy_object
//    private Bitmap getBitmap(int drawableRes) {
//        Drawable drawable = getResources().getDrawable(drawableRes);
//        Canvas canvas = new Canvas();
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        canvas.setBitmap(bitmap);
//        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//        drawable.draw(canvas);
//        heightBuoy = getHeight();
//        widthBuoy = getWidth();
//        return bitmap;
//    }

    public Point ConvWGSToScrPoint(float m_Long,float m_Lat)// converting lat lon  WGS coordinates to screen XY coordinates
    {
        Point s = new Point();
        float refLat = (viewLat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((int )(mScale*((m_Long - viewLon) * 111.31949079327357)*cos(refLat))+scrCtX,
                (int)(mScale*((viewLat- (m_Lat)) * 111.132954))+scrCtY
        );
        return s;
    }
    public PointF ConvScrPointToWGS(int x,int y)
    {
        float olat  = mlat -  (float)(((y-scrCtY)/mScale)/(111.132954f));
        float refLat = (mlat +(olat))*0.00872664625997f;//3.14159265358979324/180.0/2;
        float olon = (x-scrCtX)/mScale/(111.31949079327357f*(float)cos(refLat))+ mlon;
        return new PointF(olon,olat);
    }
    List<Location> nearbyShips;
    public void setNearbyShips(List<Location> nearbyShips_input)
    {
        nearbyShips = nearbyShips_input;
    }
    public void setLonLatMyLocation(float latLoc, float lonLoc,boolean gotoLoc){
        shipLocationLat = latLoc;
        shipLocationLon = lonLoc;
        if(gotoLoc)
        {
            mlat = shipLocationLat;
            mlon = shipLocationLon;
            if(mScale<10)mScale = 10;
        }

        MYLOCATION =true;

        invalidate();
    }

    public void myLocationToDirection(int type, float latDirectionLoc, float lonDirectionLoc){
        DIRECTIONS = true;
        if(type == 1){
            searchPlace_lat = latDirectionLoc;
            searchPlace_lon = lonDirectionLoc;
        }
        mlat = (searchPlace_lat + shipLocationLat) / 2;
        mlon = (searchPlace_lon + shipLocationLon) / 2;
        mScale = scrCtY / (abs(shipLocationLat - searchPlace_lat) * 111.132954f) ;
        invalidate();
    }

    public void setLonLatSearchPlace(float latSearchLoc, float lonSearchLoc){
        mlat = searchPlace_lat = latSearchLoc;
        mlon = searchPlace_lon = lonSearchLoc;
        mScale = 15;
        SEARCHPLACE = true;
        DIRECTIONS = false;
        invalidate();
    }

}
