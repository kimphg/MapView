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
import android.graphics.Rect;
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
import com.SeaMap.myapplication.object.Density;
import com.SeaMap.myapplication.object.Polyline;
import com.SeaMap.myapplication.object.Region;
import com.SeaMap.myapplication.object.Text;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class PolygonsView extends View {
    private Timer drawTimer;
    private boolean isBufferBusy = false;
    double buf_x=0,buf_y=0;
    public boolean isShowDensityMap = false;
    protected static float mlat = 20.0f;//lattitude of the center of the screen
    protected static float mlon = 106.5f;//longtitude of the center of the screen
    protected static float mScale = 10f;// 1km = mScale*pixcels
    protected static float mOldScale = 10f;// 1km = mScale*pixcels
    //protected static float viewLat = 18.32f;//lattitude of the center of the screen
    //protected static float viewLon = 105.43f;//longtitude of the center of the screen
    protected static boolean MYLOCATION = false; // vi tri hien tai
    private static boolean SEARCHPLACE = false;
    private static boolean DIRECTIONS = false;
    private Bitmap bufferBimap;
    private Canvas canvasBuf;
    float searchPlace_lon, searchPlace_lat;
    public int scrCtY,scrCtX;
    PointF pointTopRight,pointBotLeft;
    protected float shipLocationLon = 105.43f, shipLocationLat = 18.32f;
    private boolean lockDragging = false;// khóa không cho drag khi đang zoom
    private Paint landPaint = new Paint(), riverPaint = new Paint(), depthLinePaint = new Paint(), borderlinePaint = new Paint(), cusPaint = new Paint(),textPaint = new Paint();
    private ScaleGestureDetector scaleGestureDetector;
    protected PointF dragOldPoint, dragNewPoint;
    protected Context mCtx;
    private TimerTask mTask1;
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
        pointTopRight = ConvScrPointToWGS(scrCtX * 2,0);
        pointBotLeft = ConvScrPointToWGS(0, scrCtY * 2);
        initPaints();
        invalidate();
    }

    private void initPaints() {
        bufferBimap = Bitmap.createBitmap(this.getWidth(),this.getHeight(), Bitmap.Config.ARGB_8888);
        //bufferBimap1 = Bitmap.createBitmap(this.getWidth(),this.getHeight(), Bitmap.Config.ARGB_8888);
        canvasBuf = new Canvas(bufferBimap);
        landPaint.setAntiAlias(true);
        landPaint.setStyle(Paint.Style.FILL);
        landPaint.setColor(Color.rgb(201, 185, 123));

        riverPaint.setAntiAlias(true);
        riverPaint.setStyle(Paint.Style.FILL);
        riverPaint.setColor(Color.rgb(115, 178, 235));

        buoyPaint.setColor(Color.MAGENTA);
        drawMap();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        while(isBufferBusy)// wait if bufferBimap is updating
        {}
        Rect dst = new Rect(0,0,scrCtX*2,scrCtY*2);
        dst.offset((int)buf_x,(int)buf_y);
        if(mScale!=mOldScale) {
            float scaleFactor = mScale / mOldScale;
            dst.top = scrCtY + (int) ((dst.top - scrCtY) * scaleFactor);
            dst.bottom = scrCtY + (int) ((dst.bottom - scrCtY) * scaleFactor);
            dst.left = scrCtX + (int) ((dst.left - scrCtX) * scaleFactor);
            dst.right = scrCtX + (int) ((dst.right - scrCtX) * scaleFactor);
        }
        canvas.drawBitmap(bufferBimap,null,dst,null);
    }
    private int Distance(Point p1, Point p2){
        return (int) Math.sqrt(Math.pow((p1.x -p2.x),2) + Math.pow((p1.y - p2.y),2));
    }
    void DrawTextMap()
    {
        cusPaint.setAntiAlias(true);
        cusPaint.setStyle(Paint.Style.STROKE);
        cusPaint.setColor(Color.rgb(255, 239, 213));
        cusPaint.setStyle(Paint.Style.FILL);

        for(int lon = (int) pointBotLeft.x ; lon<= (int) pointTopRight.x  ; lon++) {
            for (int lat = (int) pointBotLeft.y; lat <= (int) pointTopRight.y; lat++) {
                String area = lon + "-" + lat;
//                //draw text
                Vector<Text> tT = ReadFile.tTexts.get(area);
                if (tT == null) continue;
                for (int k = 0; k < tT.size(); k++) {
                    Text text = tT.get(k);
                    if(text.getName().length() == 0) continue;
                    if (mScale < 5 && text.getType() != 3) continue;
                    Point p1 = ConvWGSToScrPoint(text.getCoordinate()[0], text.getCoordinate()[1]);
                    Point p2 = ConvWGSToScrPoint(text.getCoordinate()[2], text.getCoordinate()[3]);
                    int distance = Distance(p1, p2);
                    int color = text.getPen()[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    textPaint.setColor(Color.rgb(red, green, blue));
                    Path mPath = new Path();
                    mPath.moveTo(p1.x, p1.y);
                    mPath.lineTo(p2.x, p2.y);
                    int textSize = (int) distance * 2 / text.getName().length();
                    if(textSize > scrCtX )continue;
                    if(textSize < scrCtX * 0.05)continue;

                    if (mScale <= 4f && text.getType() == 3) {
                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
                        canvasBuf.drawText(text.getName(), p1.x, p1.y, textPaint);
                    } else if (mScale > 2 && mScale <= 10 && text.getType() == 0) {
                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    } else if (mScale > 6 && mScale <= 12 && text.getType() == 4) {
                        textPaint.setTextSize(distance / text.getName().length());
                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    } else if (mScale > 8 && text.getType() == 1) {
                        if (text.getName().length() != 0)
                            textPaint.setTextSize(distance / text.getName().length());
                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    }
                }
            }
        }
    }
    void drawMap()
    {
        isBufferBusy=true;
        canvasBuf.drawColor(Color.WHITE);
        pointTopRight = ConvScrPointToWGS(scrCtX * 2,0);
        pointBotLeft = ConvScrPointToWGS(0, scrCtY * 2);
        //DRAW POLYGON
        for(int lon = (int) pointBotLeft.x - 2; lon<= (int) pointTopRight.x + 2; lon++) {
            for (int lat = (int) pointBotLeft.y - 2; lat <= (int) pointTopRight.y + 2; lat++) {
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
                    canvasBuf.drawPath(pathRegion, landPaint);
                }
            }
        }

        for(int lon = (int) pointBotLeft.x ; lon<= (int) pointTopRight.x ; lon++) {
            for (int lat = (int) pointBotLeft.y ; lat <= (int) pointTopRight.y ; lat++) {
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
                    canvasBuf.drawPath(pathRegion, riverPaint);
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
                    canvasBuf.drawLines(pointis, borderlinePaint);
                }
            }
        }

        //Draw

        if( mScale >5){
            for(int lon = (int) pointBotLeft.x ; lon<= (int) pointTopRight.x ; lon++) {
                for (int lat = (int) pointBotLeft.y ; lat <= (int) pointTopRight.y ; lat++) {
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
                        canvasBuf.drawLines(pointis, depthLinePaint);
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
                            canvasBuf.drawCircle(p.x, p.y, 5, buoyPaint);
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
            canvasBuf.drawCircle(p1.x, p1.y, 10, locationPaint);
            //int height = mbitmap.getHeight();
            //int width = mbitmap.getWidth();

            //canvas.drawBitmap(mbitmap, p1.x - height/2, p1.y - width, locationPaint);
            //draw nearby ships
            if(nearbyShips!=null)
                if(nearbyShips.size()>0) {
                    locationPaint.setColor(Color.argb(150, 0, 0, 100));
                    for (Location ship : nearbyShips) {
                        Point pship = ConvWGSToScrPoint((float) ship.getLongitude(), (float) ship.getLatitude());
                        canvasBuf.drawCircle(pship.x, pship.y, 7, locationPaint);
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
            canvasBuf.drawLines(pointf, searchPl);
            SEARCHPLACE = true;
        }

        if(SEARCHPLACE){
            Point p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            int height = mbitmap.getHeight();
            int wight = mbitmap.getWidth();
            Paint searchPl = new Paint();
            canvasBuf.drawBitmap(mbitmap, p1.x - wight/2, p1.y - height, searchPl);
        }
        DrawDensityMap();
        DrawTextMap();

        mOldScale = mScale;
        isBufferBusy = false;
        invalidate();
    }
    void DrawDensityMap()
    {

        Paint pointDensity = new Paint();
        pointDensity.setColor(Color.rgb(255, 30, 30));
//        if(mScale < 15) pointDensity.setStrokeWidth(1f);
//        else pointDensity.setStrokeWidth(1.3f);
        for(int lon = (int) pointBotLeft.x ; lon<= (int) pointTopRight.x ; lon++) {
            for (int lat = (int) pointBotLeft.y ; lat <= (int) pointTopRight.y ; lat++) {
                String area = lon + "-" + lat;
                Vector<Density> vtDensity = ReadFile.listDensity.get(area);
                if (vtDensity == null) continue;
                int size = vtDensity.size() ;
//                float []pointf = new float[size * 2];
                for(int i =0; i < size * 2; i+= 2){
                    Density density = vtDensity.get(i / 2);
                    if(mScale < 10 && density.getCountMove() < 4) continue;
                    Point p1 = ConvWGSToScrPoint(density.getLongitude(), density.getLatitude());
//                    pointf[i] = p1.x;
//                    pointf[i + 1] = p1.y;

                    double brightness =  (density.getCountMove()/60.0);
                    if(brightness>1)brightness=1;
                    if(brightness<0.4)brightness=0.4;
                    pointDensity.setColor(Color.argb((int)(brightness*255),30, 230, 30));
                    pointDensity.setStrokeWidth((int) (  mScale /10.0));
                    canvasBuf.drawPoint(p1.x, p1.y, pointDensity);
                }
            }
        }
    }
    float PointDistancePixels(PointF p1,PointF p2)
    {
        float dx = abs(p1.x-p2.x);
        float dy = abs(p1.y-p2.y);
        return (float) sqrt(dx*dx+dy*dy);

    }

    private void pushBuffer()
    {
        PointF newLatLon = ConvScrPointToWGS((int) (scrCtX - buf_x), (int) (scrCtY - buf_y));
        mlat = newLatLon.y;
        mlon = newLatLon.x;
        buf_x = 0;
        buf_y = 0;
        drawMap();
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dragOldPoint = new PointF(event.getX(),event.getY());
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            pushBuffer();
            lockDragging = false;
        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            if(lockDragging)return true;//bỏ qua nếu đang khóa drag
            dragNewPoint = new PointF(event.getX(),event.getY());
            //if(PointDistancePixels(dragNewPoint, dragOldPoint)>scrCtX*0.5)return true;//không cho drag màn hình quá nhanh
            buf_x += dragNewPoint.x - dragOldPoint.x;
            buf_y += dragNewPoint.y - dragOldPoint.y;

            dragOldPoint = dragNewPoint;
            //if(buf_y>scrCtY/4||buf_x>scrCtX/4)pushBuffer();
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
        float refLat = (mlat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((int )(mScale*((m_Long - mlon) * 111.31949079327357)*cos(refLat))+scrCtX,
                (int)(mScale*((mlat- (m_Lat)) * 111.132954))+scrCtY
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
        drawMap();
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
