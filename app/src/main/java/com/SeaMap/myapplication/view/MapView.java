package com.SeaMap.myapplication.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.Activity.MainActivity;
import com.SeaMap.myapplication.Activity.MapPointEditor;
import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.MapPoint;
import com.SeaMap.myapplication.classes.MapPointUser;
import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.object.Buoy;
import com.SeaMap.myapplication.object.Density;
import com.SeaMap.myapplication.object.Polyline;
import com.SeaMap.myapplication.object.Region;
import com.SeaMap.myapplication.object.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class MapView extends View {
    public static final int REQUEST_INPUT = 1001;
    public List<Text> listTextRoute = null;
    private static double mlat = 20.0;//lattitude of the center of the screen
    private static double mlon = 106.5;//longtitude of the center of the screen
    private static float mScale = 10f;// 1km = mScale*pixcels
    private static float mOldScale = 10f;// 1km = mScale*pixcels
    //protected static float viewLat = 18.32f;//lattitude of the center of the screen
    //protected static float viewLon = 105.43f;//longtitude of the center of the screen
//    protected static boolean MYLOCATION = false; // vi tri hien tai
    private static boolean SEARCHPLACE = false;
    private static boolean DIRECTIONS = false;
    private static float waveHeight = 0;
    public static float azimuthCompass = 0;
    public static float azimuthShip = 0;

    public boolean mapOutdated = true;
    private Timer timer1s;
    private boolean isBufferBusy = false;
    float buf_x=0,buf_y=0;
    private static Location shiplocation = new Location("GPS");

    public static boolean isPointMode = false;

    private Bitmap bufferBimap,mRouteBitmap;
    public Canvas canvasBuf = new Canvas();
    float searchPlace_lon, searchPlace_lat;
    public int scrCtY,scrCtX;
    MapPoint pointTopRight,pointBotLeft;
    //protected double shipLocationLon = 105.43f, shipLocationLat = 18.32f;
    private boolean lockDragging = false;// khóa không cho drag khi đang zoom
    private Paint landPaint = new Paint(), riverPaint = new Paint(), depthLinePaint = new Paint(), borderlinePaint = new Paint(), cusPaint = new Paint(),textPaint = new Paint(), ownShipPaint;
    private ScaleGestureDetector scaleGestureDetector;
    protected PointF dragOldPoint, dragNewPoint;
    protected Context mCtx;
    private TimerTask mTask1;
    Path pathBouy = new Path();
    public boolean shipMove = false;

    Paint objectPaint = new Paint();
//    protected Bitmap bitmapBouy;
    protected Paint buoyPaint = new Paint();
//    private int heightBuoy, widthBuoy;
    private int blinkSize = 5;
    private boolean paintParamsReady = false;

    private Path pathOrientationPhone = new Path();
    private Path pathOrientationShip = new Path();

    OrientationEventListener myOrientationEventListener;
    public MapView(Context context) {
        super(context);
        shiplocation.setLatitude(20);
        shiplocation.setLongitude(106);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleLister());
        setOnLongClickListener(infoAcoordinate);
        mCtx = context;

//        bitmapBouy = BitmapFactory.decodeResource(getResources(), R.drawable.buoy_object); //getBitmap(R.drawable.buoy_object);

//        ListenerRotate();
        mTask1 = new TimerTask() {
            @Override
            public void run() {
                if(blinkSize >1) blinkSize/=2.0;
                if(mapOutdated)drawMap();
                updateView();
                isBufferBusy = false;// allow access to draw buffer after drawmap() is done

            }
        };
        timer1s = new Timer();
        timer1s.schedule(mTask1,1000,500);
        this.setOnCreateContextMenuListener(contextMenu);

    }
    float pointSize =1;
    void updateView()
    {
        postInvalidate();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        scrCtY = h / 2;
        scrCtX = w / 2;
        pointSize = Math.max(4, Math.min(scrCtX,scrCtY) * 0.01f);
        mRouteBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker),(int)pointSize*20,(int)pointSize*20,false);
        pointTopRight = ConvScrPointToWGS(scrCtX * 2,0);
        pointBotLeft = ConvScrPointToWGS(0, scrCtY * 2);
        initPaints();
        mapOutdated = true;
    }

    private void initPaints() {
        bufferBimap = Bitmap.createBitmap(this.getWidth(),this.getHeight(), Bitmap.Config.ARGB_8888);

        //bufferBimap1 = Bitmap.createBitmap(this.getWidth(),this.getHeight(), Bitmap.Config.ARGB_8888);
        canvasBuf = new Canvas(bufferBimap);
        landPaint.setAntiAlias(false);
        landPaint.setStyle(Paint.Style.FILL);
        landPaint.setColor(Color.rgb(201, 185, 123));

        riverPaint.setAntiAlias(false);
        riverPaint.setStyle(Paint.Style.FILL);
        riverPaint.setColor(Color.rgb(180, 220, 240));

        buoyPaint.setColor(Color.MAGENTA);
        buoyPaint.setStyle(Paint.Style.FILL);
        buoyPaint.setStrokeWidth(3);

        pathBouy = new Path();
        PointF p = new PointF(0,0);
        PointF pm1=new PointF(p.x,p.y),pm2 = new PointF(p.x,p.y);
        pm1.offset(0,-20);
        pm2.offset(-6,0);
        p.offset(6,0);
        pathBouy.moveTo(p.x,p.y);
        pathBouy.lineTo(pm1.x,pm1.y);
        pathBouy.lineTo(pm2.x,pm2.y);
        float basicObjectSize = scrCtX/10.0f;
        //path de ve orientation phone
        ownShipPaint = new Paint();
        ownShipPaint.setStyle(Paint.Style.FILL);
        ownShipPaint.setColor(Color.argb(180, 10, 100, 10));
        pathOrientationPhone = new Path();
        PointF oriPP1 = new PointF(0,0);
        PointF oriPP2 =  new PointF(basicObjectSize*0.4f,-basicObjectSize*0.5f);
        PointF oriPP3 = new PointF(-basicObjectSize*0.4f,-basicObjectSize*0.5f);
        pathOrientationPhone.moveTo(oriPP1.x,oriPP1.y);
        pathOrientationPhone.lineTo(oriPP2.x,oriPP2.y);
        pathOrientationPhone.lineTo(oriPP3.x,oriPP3.y);

        pathOrientationShip = new Path();
        PointF oriSP1 = new PointF(0,-basicObjectSize);
        PointF oriSP3 =  new PointF(0,0);
        PointF oriSP2 = new PointF(basicObjectSize/2.0f,basicObjectSize/2.0f);
        PointF oriSP4 = new PointF(-basicObjectSize/2.0f,basicObjectSize/2.0f);
        pathOrientationShip.moveTo(oriSP1.x,oriSP1.y);
        pathOrientationShip.lineTo(oriSP2.x,oriSP2.y);
        pathOrientationShip.lineTo(oriSP3.x,oriSP3.y);
        pathOrientationShip.lineTo(oriSP4.x,oriSP4.y);

        borderlinePaint.setStrokeWidth(2);
        mapOutdated = true;
        this.paintParamsReady = true;

        objectPaint.setStrokeWidth(Math.max(2, pointSize ));
        objectPaint.setColor(Color.argb(150, 50, 10, 0));
        objectPaint.setTextSize(pointSize*6);
        objectPaint.setStyle(Paint.Style.FILL);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int nwait = 0;
        while(isBufferBusy)// wait if bufferBimap is updating
        {
            if(nwait>1000) return;
            nwait++;
        }
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
        DrawScrObjects(canvas);
    }
    private float Distance(PointF p1, PointF p2){
        return (float) Math.sqrt((p1.x -p2.x)*(p1.x -p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }
    void DrawTextMap()
    {
        cusPaint.setAntiAlias(false);
        cusPaint.setStyle(Paint.Style.STROKE);
        cusPaint.setColor(Color.rgb(255, 239, 213));
        cusPaint.setStyle(Paint.Style.FILL);
        for(int lon = (int) pointBotLeft.mlon ; lon<= (int) pointTopRight.mlon  ; lon++) {
            for (int lat = (int) pointBotLeft.mlat; lat <= (int) pointTopRight.mlat; lat++) {
                String area = lon + "-" + lat;
//                //draw text
                Vector<Text> tT = GlobalDataManager.tTexts.get(area);
                if (tT == null) continue;
                for (int k = 0; k < tT.size(); k++) {
                    Text text = tT.get(k);
                    //Todo: Du lieu da loai bo
//                    if(text.getName().length() == 0) continue;
//                    if(text.getName().contains("HL")) continue;//todo:remove later

                    PointF p1 = ConvWGSToScrPoint(text.getCoordinate()[0], text.getCoordinate()[1]);
                    PointF p2 = ConvWGSToScrPoint(text.getCoordinate()[2], text.getCoordinate()[3]);
                    float distance = Distance(p1, p2);

                    int color = text.getPen()[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    textPaint.setColor(Color.rgb(red, green, blue));
                    int fontSize = (int) (distance*0.9 / text.getName().length());
                    if ( text.getType() == 3|| text.getType()==0) fontSize*=2;
                    //todo: du lieu da duoc loai bo
//                    {
//
////                        if(text.getName().length()<3)
////                            continue;//todo:remove later
////                        if(text.getName().contains("Báo Cáo"))continue;//todo:remove later
////                        if(text.getName().contains("Cn"))continue;//todo:remove later
//                        fontSize*=2;
//
//                    }
                    if(fontSize>scrCtX*0.3||fontSize<scrCtX*0.03)continue;
                    textPaint.setTextSize(fontSize);
                    if(text.getType() == 3)canvasBuf.drawText(text.getName(), p1.x, p1.y, textPaint);
                    else
                    {
                        Path mPath = new Path();
                        mPath.moveTo(p1.x, p1.y);
                        mPath.lineTo(p2.x, p2.y);
                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    }

                    //type 0: ten cac thanh pho
                    //type 1: do sau
                    //type 2: hon dao nho
                    //type 3: quan dao, ha noi
//
//                    if (mScale <= 4f && text.getType() == 3) {
//                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
//                        canvasBuf.drawText(text.getName(), p1.x, p1.y, textPaint);
//                    } else if (mScale > 2 && mScale <= 10 && text.getType() == 0) {
//                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
//                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
//                    } else if (mScale > 6 && mScale <= 12 && text.getType() == 4) {
//                        textPaint.setTextSize(distance / text.getName().length());
//                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
//                    } else if (mScale > 8 && text.getType() == 1) {
//                        if (text.getName().length() != 0)
//                            textPaint.setTextSize(distance / text.getName().length());
//                        canvasBuf.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
//                    }

                }
                //draw buoys
                if( mScale > 10) {
                    //draw buoy
                    Vector<Buoy> bouyVectors = GlobalDataManager.listBuoys.get(area);
                    if (bouyVectors == null) continue;
                    for (int k = 0; k < bouyVectors.size(); k++) {
                        Buoy buoy = bouyVectors.get(k);
                        float[] coor = buoy.getCoordinates();
                        PointF p = ConvWGSToScrPoint(coor[0], coor[1]);
                        Path pat = new Path();
                        pathBouy.offset(p.x,p.y,pat);
                        //p.offset(-bitmapBouy.getWidth()/2,-bitmapBouy.getHeight()/2);
                        //canvasBuf.drawBitmap(bitmapBouy, p.x , p.y , buoyPaint);
                        canvasBuf.drawPath(pat,buoyPaint);
                    }
                }
            }
        }
    }
    private void drawMap()
    {
        if(!paintParamsReady)return;
        mapOutdated = false;
        long tStart = System.currentTimeMillis();
        if(isBufferBusy){isBufferBusy = false; return;}
        else isBufferBusy=true;
        canvasBuf.drawColor(Color.WHITE);
        pointTopRight = ConvScrPointToWGS(scrCtX * 2,0);
        pointBotLeft = ConvScrPointToWGS(0, scrCtY * 2);
        //DRAW POLYGON
        Vector<Region> regions;
        if(GlobalDataManager.dataReady)
        for(int lon = (int) pointBotLeft.mlon - 2; lon<= (int) pointTopRight.mlon + 2; lon++) {
            for (int lat = (int) pointBotLeft.mlat - 2; lat <= (int) pointTopRight.mlat + 2; lat++) {

                if(mScale < 8) {
                    regions = GlobalDataManager.BaseRegions.get(lon + "-" + lat);
                }
                else
                    regions = GlobalDataManager.Poligons.get(lon + "-" + lat);
                if(regions == null) continue;
                for(int k =0;  k< regions.size() ; k++){
                    Region polygon = regions.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    float[] coordinate = polygon.getCoordinate();
                    PointF point1 = ConvWGSToScrPoint(coordinate[0], coordinate[1]);
                    pathRegion.moveTo(point1.x, point1.y);
                    for (int i = 0; i < coordinate.length-1; i = i + 2) {
                        point1 = ConvWGSToScrPoint(coordinate[i], coordinate[i + 1]);
                        pathRegion.lineTo(point1.x, point1.y);
                    }
                    canvasBuf.drawPath(pathRegion, landPaint);
                }
            }
        }

        for(int lon = (int) pointBotLeft.mlon ; lon<= (int) pointTopRight.mlon ; lon++) {
            for (int lat = (int) pointBotLeft.mlat ; lat <= (int) pointTopRight.mlat ; lat++) {
                String area = lon + "-" + lat;

                ////DRAW RIVER

                Vector<Region> river;
                if(mScale < 8) {
                    river = GlobalDataManager.BasePlgRiver.get(area);
                }
                else river = GlobalDataManager.PolygonRivers.get(area);
                if(river == null) continue;
                for(int k =0;  k< river.size() ; k++){
                    Region polygon = river.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    float[] coordinate = polygon.getCoordinate();
                    PointF point1 = ConvWGSToScrPoint(coordinate[0], coordinate[1]);
                    pathRegion.moveTo(point1.x, point1.y);
                    for (int i = 0; i < coordinate.length-1; i = i + 2) {
                        point1 = ConvWGSToScrPoint(coordinate[i], coordinate[i + 1]);
                        pathRegion.lineTo(point1.x, point1.y);
                    }
                    canvasBuf.drawPath(pathRegion, riverPaint);
                }

                //DrawBorder
                Vector<Polyline> border = GlobalDataManager.Border_Map.get(area);
                if (border== null) continue;
                for (int k = 0; k < border.size(); k++) {
                    Polyline pl = border.get(k);
                    int size = pl.getCoordinate().length;
                    float[] pointis = new float[size * 2];
                    for (int i = 0; i < size - 2; i += 2) {
                        PointF p1 = ConvWGSToScrPoint(pl.getCoordinate()[i], pl.getCoordinate()[i + 1]);
                        PointF p2 = ConvWGSToScrPoint(pl.getCoordinate()[i + 2], pl.getCoordinate()[i + 3]);
                        pointis[2 * i] = (float) p1.x;
                        pointis[2 * i + 1] = (float) p1.y;
                        pointis[i * 2 + 2] = (float) p2.x;
                        pointis[i * 2 + 3] = (float) p2.y;
                    }
                    canvasBuf.drawLines(pointis, borderlinePaint);
                }
            }
        }

        //vẽ các đường đẳng sâu

        {
            for(int lon = (int) pointBotLeft.mlon ; lon<= (int) pointTopRight.mlon ; lon++) {
                for (int lat = (int) pointBotLeft.mlat ; lat <= (int) pointTopRight.mlat ; lat++) {
                    String area = lon + "-" + lat;
                    //draw polyline
                    Vector<Polyline> PL = GlobalDataManager.PLines.get(area);
                    if (PL == null) continue;
                    for (int k = 0; k < PL.size(); k++) {
                        Polyline pl = PL.get(k);
                        int size = pl.getCoordinate().length;
                        float pointis[] = new float[size * 2];
                        for (int i = 0; i < size - 2; i += 2) {
                            PointF p1 = ConvWGSToScrPoint(pl.getCoordinate()[i], pl.getCoordinate()[i + 1]);
                            PointF p2 = ConvWGSToScrPoint(pl.getCoordinate()[i + 2], pl.getCoordinate()[i + 3]);
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

                }
            }
        }


        DrawDensityMap();
        DrawTextMap();

        mOldScale = mScale;
        isBufferBusy = false;

        switch (MainActivity.CHOOSE_DISTANE_OR_ROUTE){
            case 0:{
                break;
            }
            default:{
                MainActivity.routingModeView.postInvalidate();
                break;
            }
        }
//        invalidate();
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        Log.d("tDelta",Long.toString(tDelta));
    }
    void DrawDensityMap()
    {
        if(!GlobalDataManager.dataReady)return;
        boolean recudeResolution = (mScale<10);
        Paint pointDensity = new Paint();
        float size = (float) Math.max(1, mScale /8.0 );
        if(recudeResolution)pointDensity.setStrokeWidth(size*2);
        else pointDensity.setStrokeWidth(size);

        for(int lon = (int) pointBotLeft.mlon ; lon<= (int) pointTopRight.mlon ; lon++) {
            for (int lat = (int) pointBotLeft.mlat ; lat <= (int) pointTopRight.mlat ; lat++) {
                String area = lon + "," + lat;
                Vector<Density> vtDensity = GlobalDataManager.listDensity.get(area);
                if (vtDensity == null) continue;

//                float []pointf = new float[size * 2];
                for(Density density: vtDensity){

                    if(recudeResolution) {
                        if (!density.reduceRes) continue;
                    }
                    PointF p1 = ConvWGSToScrPoint(density.longitude, density.latitude);
                    double brightness =  (density.countMove/5.0);
                    if(brightness>1)brightness=1;
                    if(brightness<0.4)brightness=0.4;
                    pointDensity.setColor(Color.argb((int)(brightness*255),30, (int)(brightness*150), 180));
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
    void DrawShip(float lat,float lon, float cogDeg,Canvas canvas)
    {
        PointF p1 = ConvWGSToScrPoint(lon,lat);
        //p1.offset((float)buf_x, (float)buf_y);
        Path pat = new Path(pathOrientationShip);
        Matrix matrix = new Matrix();
        matrix.setRotate(cogDeg, 0, 0);
        pat.transform(matrix);
        pat.offset(p1.x,p1.y);
        canvas.drawPath(pat, objectPaint);
    }

    void DrawSavedPoint(MapPointUser point, Canvas canvas)
    {
        PointF p1 = ConvWGSToScrPoint(point.mlon,point.mlat);
        //p1.offset((float)buf_x, (float)buf_y);
        switch (point.mType)
        {
            case 0:
                objectPaint.setColor(Color.argb(160, 20,200 , 20));
                break;
            case 1:
                objectPaint.setColor(Color.argb(160, 200,20 , 20));
                break;
            case 2:
                objectPaint.setColor(Color.argb(160, 200,200 , 20));
                break;
            case 3:
                objectPaint.setColor(Color.argb(160, 20,20 , 20));
                break;
            case 4:
                objectPaint.setColor(Color.argb(50, 20,20 , 20));
                return;
        }
        drawRomb(p1,objectPaint,canvas);
        canvas.drawText(point.mName,p1.x+pointSize*5,p1.y,objectPaint);
    }
    private boolean isInsiseScr(float plat,float plon)
    {
        if(plat>pointBotLeft.mlat)
            if(plat<pointTopRight.mlat)
                if(plon>pointBotLeft.mlon)
                    if(plon<pointTopRight.mlon)
                    {
                        return true;
                    }
        return false;
    }
    void drawCross(PointF scrPoint,Paint paint,Canvas canvas)
    {
        float crossSize = pointSize * 6;
        canvas.drawLine(scrPoint.x + crossSize, scrPoint.y, scrPoint.x - crossSize, scrPoint.y, paint);
        canvas.drawLine(scrPoint.x, scrPoint.y + crossSize, scrPoint.x, scrPoint.y - crossSize, paint);
    }
    void drawRomb(PointF scrPoint,Paint paint,Canvas canvas)
    {
        float crossSize = pointSize * 6;
        canvas.drawLine(scrPoint.x- crossSize, scrPoint.y ,     scrPoint.x , scrPoint.y-crossSize, paint);
        canvas.drawLine(scrPoint.x, scrPoint.y - crossSize,         scrPoint.x + crossSize, scrPoint.y , paint);
        canvas.drawLine(scrPoint.x + crossSize, scrPoint.y,         scrPoint.x , scrPoint.y + crossSize, paint);
        canvas.drawLine(scrPoint.x, scrPoint.y + crossSize,         scrPoint.x- crossSize, scrPoint.y , paint);
        canvas.drawLine(scrPoint.x, scrPoint.y ,         scrPoint.x, scrPoint.y -crossSize, paint);
    }
    void DrawScrObjects(Canvas canvas)
    {
        float xoffset = (float)buf_x;
        float yoffset = (float)buf_y;

        {
            PointF p1 = ConvWGSToScrPoint((float) shiplocation.getLongitude(), (float) shiplocation.getLatitude());
            boolean shipInsideScreen = (p1.x > 0) && (p1.y > 0) && (p1.x < scrCtX * 2) && (p1.y < scrCtY * 2);
            DrawRoute(canvas);

            if (shipInsideScreen) {//ve hinh tron o toa do hien tai

                objectPaint.setColor(Color.argb(80, 30, 150, 50));
                canvas.drawCircle(p1.x, p1.y, pointSize *3 +blinkSize, objectPaint);
                //ve huong la ban
                Path pat = new Path(pathOrientationPhone);
                Matrix matrix = new Matrix();
                matrix.setRotate(( azimuthCompass), 0, 0);
                pat.transform(matrix);
                pat.offset(p1.x,p1.y);
                canvas.drawPath(pat, ownShipPaint);
                ownShipPaint.setStrokeWidth(Math.max(2, pointSize ));
                canvas.drawLine(p1.x , p1.y, p1.x+ (float)(scrCtX/2.0f*Math.sin(Math.toRadians(  azimuthCompass))) , p1.y- (float)(scrCtX/2.0f*Math.cos(Math.toRadians(  azimuthCompass))), ownShipPaint);
                // Ve huong di chuyen tau
                if(shipMove) {
                    objectPaint.setColor(Color.argb(180, 200, 50 , 10));
                    DrawShip((float) shiplocation.getLatitude(),(float) shiplocation.getLongitude(),azimuthShip,canvas);
                }
            }

            if (isPointMode) {// ve vi tri tam man hinh
                Location scrLocation = new Location("GPS");
                scrLocation.setLatitude(mlat);
                scrLocation.setLongitude(mlon);
                PointF p2 = new PointF(scrCtX,scrCtY);
                objectPaint.setColor(Color.argb(180, 200, 50 , 10));
                drawCross(p2, objectPaint,canvas);
                float distance = scrLocation.distanceTo(shiplocation);
                float bearing = shiplocation.bearingTo(scrLocation);
                if(bearing<0)bearing+=360;
                PointF p3 = new PointF(p2.x,p2.y);
                p3.offset(pointSize * 2, pointSize * 8);

                canvas.drawText(String.format("%.1f",  bearing) + "\260 "+ String.format("%.1f", distance / 1852.0) + "Nm", p3.x, p3.y, objectPaint);
                p3.offset(0, pointSize * 7);
                String dms= MapPoint.decimalToDMS(mlon,mlat);
                canvas.drawText(dms, p3.x, p3.y, objectPaint);
                objectPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y , objectPaint);
                objectPaint.setPathEffect(null);
            }

        }

        //draw nearby ships
        if(nearbyShips!=null)
            if(nearbyShips.size()>0) {
                objectPaint.setColor(Color.argb(150, 150, 0, 0));
                for (Location ship : nearbyShips) {

                    DrawShip((float) ship.getLatitude(),(float) ship.getLongitude(),ship.getBearing(),canvas);
                    //canvas.drawCircle(pship.x+xoffset, pship.y+yoffset, 7, locationPaint);
                }
            }
        //draw location history
        if(locationHistory!=null)
            if(locationHistory.size()>0) {
                objectPaint.setColor(Color.argb(150, 70, 50, 30));
                for (Location ship : locationHistory) {
                    canvas.drawPoint((float) ship.getLongitude(), (float) ship.getLatitude(), objectPaint);
                }
            }
        if(DIRECTIONS) {
            PointF p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            PointF p2 = ConvWGSToScrPoint((float)shiplocation.getLongitude(),(float)shiplocation.getLatitude());
            Paint searchPl = new Paint();
            searchPl.setColor(Color.RED);
            searchPl.setStrokeWidth(3);
            float pointf[] = {p1.x, p1.y, p2.x, p2.y};
            canvas.drawLines(pointf, searchPl);
            SEARCHPLACE = true;
        }

        if(SEARCHPLACE){
            PointF p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            int height = mbitmap.getHeight();
            int wight = mbitmap.getWidth();
            Paint searchPl = new Paint();
            searchPl.setTextSize(scrCtX/12);
            searchPl.setColor(Color.RED);
            canvas.drawBitmap(mbitmap, p1.x - wight/2, p1.y - height, searchPl);
            float[] distance = {0};
            Location.distanceBetween(shiplocation.getLatitude(),shiplocation.getLongitude(),searchPlace_lat,searchPlace_lon,distance);
            canvas.drawText(String.format("%.1f",distance[0]/1000.0)+"km",p1.x, p1.y+scrCtX/12,searchPl);
        }

        for (MapPointUser mPoint: GlobalDataManager.GetSavedPoints()) {
            float pLat = mPoint.mlat;
            float pLon = mPoint.mlon;
            if(isInsiseScr(pLat,pLon))
            {
                DrawSavedPoint(mPoint,canvas);
            }
        }
    }
    public void pushBuffer()
    {
        MapPoint newLatLon = ConvScrPointToWGS((int) (scrCtX ), (int) (scrCtY ));
        mlat = newLatLon.mlat;
        mlon = newLatLon.mlon;
        buf_x = 0;
        buf_y = 0;
        drawMap();
    }
    PointF dragStartPoint;
    long dragStartTime;
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dragOldPoint = new PointF(event.getX(),event.getY());
            dragStartPoint = dragOldPoint;
            dragStartTime = System.currentTimeMillis();
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            PointF dragStopPoint = new PointF(event.getX(),event.getY());
            if(Distance(dragStopPoint,dragStartPoint)<scrCtX/10) {
                long dragStopTime = System.currentTimeMillis();
                if (dragStopTime - dragStartTime < 700) {
                    OnTap(dragStopPoint);
                    return true;

                }
            }
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
        }

        return true;
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void OnTap(PointF tapPoint) {
        if(isPointMode) {
            PointF center = new PointF(scrCtX, scrCtY);
            if (Distance(center, tapPoint) < scrCtX / 10) {
                MapPointUser newPoint = new MapPointUser((float) mlat, (float) mlon, "Điểm",0);
                GlobalDataManager.AddToSavedPoints(newPoint);
            }
            else {
                float minDistance = scrCtX;
                MapPointUser nearestPoint = null;
                for (MapPointUser mPoint : GlobalDataManager.GetSavedPoints()) {
                    float pLat = mPoint.mlat;
                    float pLon = mPoint.mlon;

                    if (isInsiseScr(pLat, pLon)) {
                        PointF ptScr = ConvWGSToScrPoint(pLon, pLat);
                        float dis = Distance(ptScr, tapPoint);
                        if (dis < minDistance) {
                            minDistance = dis;
                            nearestPoint = mPoint;
                        }
                    }
                }
                if (minDistance < scrCtX / 10) {
                    selectedMapPoint = nearestPoint;
                    if (selectedMapPoint != null) {
                        EditMapPoint(selectedMapPoint);
//                        showContextMenu();
                    }
                }
            }
        }
        else
        {

        }
    }
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void EditMapPoint(MapPointUser selectedMapPoint){
            Intent intent = new Intent(mCtx.getApplicationContext(), MapPointEditor.class);
            intent.putExtra("MapPoint",selectedMapPoint.mName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCtx.getApplicationContext().startActivity(intent);

    }
    private View.OnCreateContextMenuListener contextMenu = new View.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu arg0, View arg1,
                                        ContextMenu.ContextMenuInfo arg2) {
            // TODO Auto-generated method stub
            arg0.add(0, 0, 0, "Điểm an toàn").setOnMenuItemClickListener(mMenuItemClickListener);
            arg0.add(0, 1, 0, "Điểm nguy hiểm").setOnMenuItemClickListener(mMenuItemClickListener);
            arg0.add(0, 2, 0, "Điểm đánh cá").setOnMenuItemClickListener(mMenuItemClickListener);
            arg0.add(0, 3, 0, "Điểm mốc").setOnMenuItemClickListener(mMenuItemClickListener);
            arg0.add(0, 4, 0, "Xóa").setOnMenuItemClickListener(mMenuItemClickListener);
        }


    };

    private MenuItem.OnMenuItemClickListener mMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            selectedMapPoint.mType = item.getItemId();
            GlobalDataManager.AddToSavedPoints(selectedMapPoint);

            return false;
        }
    };

    MapPointUser selectedMapPoint;
    public void clearNearbyShips() {
        nearbyShips.clear();
    }

    private class ScaleLister extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector sgd){
            mScale *= sgd.getScaleFactor();
            mScale = Math.max(2.0f, Math.min(mScale, 200));
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

    public PointF ConvWGSToScrPoint(float m_Long,float m_Lat)// converting lat lon  WGS coordinates to screen XY coordinates
    {
        PointF s = new PointF();
        double refLat = (mlat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((float )(mScale*((m_Long - mlon) * 111.31949079327357)*cos(refLat))+(float )scrCtX,
                (float)(mScale*((mlat- (m_Lat)) * 111.132954))+(float )scrCtY
        );
        s.offset(buf_x,buf_y);
        return s;
    }

    public MapPoint ConvScrPointToWGS(int x,int y)
    {
        float olat  = (float)mlat -  (float)(((y-buf_y-scrCtY)/mScale)/(111.132954f));
        float refLat = ((float)mlat +(olat))*0.00872664625997f;//3.14159265358979324/180.0/2;
        float olon = (x-buf_x-scrCtX)/mScale/(111.31949079327357f*(float)cos(refLat))+ (float)mlon;
        return new MapPoint(olat,olon);
    }
    List<Location> nearbyShips = new LinkedList<Location>();
    List<Location> locationHistory  = new ArrayList<Location>();

    public void addNearbyShip(Location ship)
    {
        nearbyShips.add(ship);
        while(nearbyShips.size()>50)nearbyShips.remove(0);
    }

    public void disableSearch_Direction(int choose){
        // Khi disable Direct
        if(choose == 1) {
            DIRECTIONS = false;
            mScale = 10f;
            setLonLatSearchPlace(searchPlace_lat, searchPlace_lon);
        }
        // Khi disable Search
        else if(choose == 0){
            SEARCHPLACE = false;
            mlat = shiplocation.getLatitude();
            mlon = shiplocation.getLongitude();
            mapOutdated = true;
        }

    }
    public void DrawRoute(Canvas canvas)
    {
        if(listTextRoute ==null)return;
        if(mRouteBitmap==null)return;

        int height = mRouteBitmap.getHeight();
        int wight = mRouteBitmap.getWidth();
        objectPaint.setTextSize(pointSize*10);
        objectPaint.setAlpha(255);
        objectPaint.setColor(Color.rgb(0, 0, 0));
        objectPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
//        searchPl.setColor(Color.RED);

        int size = listTextRoute.size();
        PointF pold = ConvWGSToScrPoint((float) shiplocation.getLongitude(), (float) shiplocation.getLatitude());

        for(int i = 0; i< size; i++){
            float [] coor = listTextRoute.get(i).getCoordinate();
            PointF p = ConvWGSToScrPoint(coor[0], coor[1]);
            //canvas.drawBitmap(bitmap,p.x - height / 4, p.y - wight,locationPaint);
            canvas.drawLine(pold.x,pold.y,p.x,p.y,objectPaint);
            pold = p;
            canvas.drawBitmap(mRouteBitmap, p.x - wight*0.4f, p.y - height, objectPaint);
            canvas.drawText(String.valueOf(i+1),p.x - wight*0.3f ,p.y-height*0.5f,objectPaint);
        }

        objectPaint.setPathEffect(null);
    }
    public void setLonLatMyLocation(double latLoc, double lonLoc,boolean gotoLocation ){
        blinkSize = 200;
        //save old location

        Location newLocation = new Location("GPS");
        //getnew location
        newLocation.setLatitude(latLoc);
        newLocation.setLongitude(lonLoc);
        //set location to new location
        shiplocation = newLocation;
        locationHistory.add(newLocation);
        while (locationHistory.size() > 50) locationHistory.remove(0);
        if (gotoLocation ) {
            mlat = shiplocation.getLatitude();
            mlon = shiplocation.getLongitude();
            mapOutdated = true;
            //if(mScale<10)mScale = 10;
        }
    }

    public void myLocationToDirection(int type, float latDirectionLoc, float lonDirectionLoc){
        DIRECTIONS = true;
        if(type == 1){
            searchPlace_lat = latDirectionLoc;
            searchPlace_lon = lonDirectionLoc;
        }
        mlat = (searchPlace_lat + shiplocation.getLatitude()) / 2;
        mlon = (searchPlace_lon + shiplocation.getLongitude()) / 2;
        mScale = scrCtY / (abs((float)shiplocation.getLatitude() - searchPlace_lat) * 111.132954f) ;
        mapOutdated = true;
    }

    public void setLonLatSearchPlace(float latSearchLoc, float lonSearchLoc){
        mlat = searchPlace_lat = latSearchLoc;
        mlon = searchPlace_lon = lonSearchLoc;
        if(mScale > 20)mScale = 20;
        SEARCHPLACE = true;
        DIRECTIONS = false;
        mapOutdated = true;
    }

    public void updateAzimuthCompass(float azimuth){
        azimuthCompass = azimuth/3.1415926535f*180.0f;
    }

    public void updateAzimuthShip(float azimith){

    }
}
