package com.example.myapplication.view;

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.classes.Route;
import com.example.myapplication.object.Line;
import com.example.myapplication.object.Region;
import com.example.myapplication.object.Text;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.object.Polyline;
import com.example.myapplication.classes.ReadFile;

import java.lang.String;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.cos;

/*
class objectClass {
    private Context mCtx;
    public Map <Point,Vector<ObjectLine>> listLine = new HashMap<Point, Vector<ObjectLine>>();
    public Vector<ObjectPoliline> listPLine = new Vector <ObjectPoliline>();
    public Map <Point,Vector<ObjectText>> listText = new HashMap<Point,Vector<ObjectText>>();
    public Vector<ObjectRegion> listRegion = new Vector<ObjectRegion>();

    public objectClass(Context context){
        super();
        mCtx = context;

        readFile();
        writerFile();
        //read();
    }

    private void readFile() {
        BufferedReader reader = null;
        try {
            String mline;
            ObjectLine obLine;
            ObjectPoliline obPline;
            ObjectText obText;
            int a= listLine.size();
            int b = listPLine.size();
            reader = new BufferedReader(new InputStreamReader(mCtx.getAssets().open("lines.txt"), "UTF-8"));

            while ((mline = reader.readLine()) !=null){
                if(mline.contains("Line")){
                    obLine = new ObjectLine();
                    String splitText[] = mline.split(" ");
                    float x1= Float.parseFloat(splitText[1]);
                    float y1= Float.parseFloat(splitText[2]);
                    float x2= Float.parseFloat(splitText[3]);
                    float y2= Float.parseFloat(splitText[4]);
                    String line = reader.readLine();

                    obLine.point1.set(x1,y1);
                    obLine.point2.set(x2,y2);

                    String ipen[] = line.substring(9,line.length()-1).split(",");
                    obLine.pen[0] = Integer.parseInt(ipen[0]);
                    obLine.pen[1] = Integer.parseInt(ipen[1]);
                    obLine.pen[2] = Integer.parseInt(ipen[2]);

                    Point ikey = new Point ((int) x1, (int) y1);
                    Vector<ObjectLine> cell;
                    if(listLine.containsKey(ikey)){
                        cell = listLine.get(ikey);
                    }
                    else {
                        cell = new Vector<ObjectLine>();
                    }
                    cell.add (obLine);
                    listLine.put(ikey, cell);
                }
                else if(mline.contains("Pline")){
                    String splitText [] = mline.split(" ");
                    int num = Integer.parseInt(splitText[1]);
                    obPline = new ObjectPoliline(num);
                    int i =0;
                    Point key;
                    Point ikey = new Point(0,0);
                    while(i<num){
                        mline = reader.readLine();
                        String strP [] = mline.split(" ");
                        float x= Float.parseFloat(strP[0]);
                        float y = Float.parseFloat(strP[1]);
                        key = new Point((int)x, (int)y);
                        Vector<PointF> cell;

                        //
                        if(key.equals(ikey)){
                            cell = obPline.lines.get(key);
                            cell.add(new PointF(x,y));
                            obPline.lines.put(key, cell);
                        }
                        else {
                            if(! (obPline.lines.containsKey(key))) {
                                //if not exist, create new key
                                Vector<PointF> newcell = new Vector<PointF>();
                                newcell.add(new PointF(x, y));
                                obPline.lines.put(key, newcell);
                            }
                            else {
                                // else exist, add a Poin(0,0) to differentiate Lines in cell
                                cell = obPline.lines.get(key);
                                cell.add(new PointF(0,0));
                                cell.add(new PointF(x, y));
                                obPline.lines.put(key, cell);
                            }

                            if(i > 0 && i < num-1) {
                                //
                                cell = obPline.lines.get(ikey);
                                cell.add(new PointF(x,y));
                                obPline.lines.put(ikey,cell);
                            }
                            ikey = key;
                        }
                        i++;
                    }
                    mline = reader.readLine();
                    String ipen[] = mline.substring(9,mline.length()-1).split(",");
                    obPline.pen[0] = Integer.parseInt(ipen[0]);
                    obPline.pen[1] = Integer.parseInt(ipen[1]);
                    obPline.pen[2] = Integer.parseInt(ipen[2]);
                    listPLine.add(obPline);
                }
                else if(mline.contains("Text")){
                    obText = new ObjectText();
                    obText.name = reader.readLine();
                    obText.name = obText.name.replace('"' , ' ');
                    obText.name = obText.name.trim();

                    String p = reader.readLine();
                    String pSplit[] = p.split(" ");
                    float x1= Float.parseFloat(pSplit[4]);
                    float y1= Float.parseFloat(pSplit[5]);
                    float x2= Float.parseFloat(pSplit[6]);
                    float y2= Float.parseFloat(pSplit[7]);

                    obText.point1.set(x1,y1);
                    obText.point2.set(x2,y2);


                    //String font = reader.readLine();
                    mline = reader.readLine();

                    String iline[] = mline.substring(10,mline.length()-1).split(",");
                    obText.font = iline[0].toLowerCase();
                    obText.pen[0] = Integer.parseInt(iline[1]);
                    obText.pen[1] = Integer.parseInt(iline[2]);
                    obText.pen[2] = Integer.parseInt(iline[3]);

                    String line = reader.readLine();
                    if(line.contains("Angle")) {
                        String angle[] = line.split(" ");
                        obText.angle = Float.parseFloat(angle[5]);
                    }
                    else if(!line.equals("")){
                        obText.location = line;
                    }

                    Point ikey = new Point ((int)x1, (int) y1);
                    Vector<ObjectText> cell;
                    if(listText.containsKey(ikey)){
                        cell = listText.get(ikey);
                    }
                    else {
                        cell = new Vector<ObjectText>();
                    }
                    cell.add (obText);
                    listText.put(ikey, cell);
                }
                else if(mline.contains("Region")){
                    String line[] = mline.split(" ");
                    int numberListPoint = Integer.parseInt(line[2]);
                    if(numberListPoint == 1) {
                        mline =reader.readLine();
                        String num[] = mline.split(" ");
                        int numberPoint = Integer.parseInt(num[2]);
                        ObjectRegion obRegion = new ObjectRegion(numberPoint);
                        Point key,ikey =new Point();
                        int i =0;
                        while(i<numberPoint){
                            mline = reader.readLine();
                            String strP [] = mline.split(" ");
                            float x= Float.parseFloat(strP[0]);
                            float y = Float.parseFloat(strP[1]);
                            key = new Point((int)x, (int)y);
                            Vector<PointF> cell;

                            if(key.equals(ikey)){
                                cell = obRegion.lines.get(key);
                                cell.add(new PointF(x,y));
                                obRegion.lines.put(key, cell);
                            }
                            else {
                                if(! (obRegion.lines.containsKey(key))) {
                                    Vector<PointF> newcell = new Vector<PointF>();
                                    newcell.add(new PointF(x, y));
                                    obRegion.lines.put(key, newcell);
                                }
                                else {
                                    cell = obRegion.lines.get(key);
                                    cell.add(new PointF(0,0));
                                    cell.add(new PointF(x, y));
                                    obRegion.lines.put(key, cell);
                                }

                                if(i > 0 && i < numberPoint-1) {
                                    cell = obRegion.lines.get(ikey);
                                    cell.add(new PointF(x,y));
                                    obRegion.lines.put(ikey,cell);
                                }
                                ikey = key;
                            }
                            i++;
                        }
                        String ipen = reader.readLine();
                        if(!ipen.equals("")) {
                            String pen[] = ipen.substring(9, ipen.length() - 1).split(",");
                            obRegion.pen[0] = Integer.parseInt(pen[0]);
                            obRegion.pen[1] = Integer.parseInt(pen[1]);
                            obRegion.pen[2] = Integer.parseInt(pen[2]);

                            String ibrush = reader.readLine();
                            String brush[] = ibrush.substring(11, ibrush.length() - 1).split(",");
                            obRegion.brush[0] = Float.parseFloat(brush[0]);
                            obRegion.brush[1] = Float.parseFloat(brush[1]);
                            obRegion.brush[2] = Float.parseFloat(brush[2]);

                            String ilocation = reader.readLine();
                            String location[] = ilocation.split(" ");
                            obRegion.location[0] = Float.parseFloat(location[5]);
                            obRegion.location[1] = Float.parseFloat(location[6]);

                            listRegion.add(obRegion);
                        }
                        else {
                            listRegion.add(obRegion);
                        }
                    }
                    else {
                        mline = reader.readLine();
                        Vector<ObjectRegion> obR = new Vector<ObjectRegion>();
                        while (!(mline.contains("Pen"))) {
                            String num[] = mline.split(" ");
                            int numberPoint = Integer.parseInt(num[2]);
                            ObjectRegion obRegion = new ObjectRegion(numberPoint);
                            Point key,ikey = new Point(0,0);
                            int i =0;
                            while(i<numberPoint){
                                mline = reader.readLine();
                                String strP [] = mline.split(" ");
                                float x= Float.parseFloat(strP[0]);
                                float y = Float.parseFloat(strP[1]);
                                key = new Point((int)x, (int)y);
                                Vector<PointF> cell;

                                if(key.equals(ikey)){
                                    cell = obRegion.lines.get(key);
                                    cell.add(new PointF(x,y));
                                    obRegion.lines.put(key, cell);
                                }
                                else {
                                    if(! (obRegion.lines.containsKey(key))) {
                                        Vector<PointF> newcell = new Vector<PointF>();
                                        newcell.add(new PointF(x, y));
                                        obRegion.lines.put(key, newcell);
                                    }
                                    else {
                                        cell = obRegion.lines.get(key);
                                        cell.add(new PointF(0,0));
                                        cell.add(new PointF(x, y));
                                        obRegion.lines.put(key, cell);
                                    }

                                    if(i > 0 && i < numberPoint-1) {
                                        cell = obRegion.lines.get(ikey);
                                        cell.add(new PointF(x,y));
                                        obRegion.lines.put(ikey,cell);
                                    }
                                    ikey = key;
                                }
                                i++;
                            }
                            obR.add(obRegion);
                            mline= reader.readLine();
                        }
                        int i = 0;
                        String ipen = mline;
                        String ibrush = reader.readLine();
                        String ilocation = reader.readLine();
                        while (i<obR.size()){
                            String pen[] = ipen.substring(9,ipen.length()-1).split(",");
                            obR.get(i).pen[0] = Integer.parseInt(pen[0]);
                            obR.get(i).pen[1] = Integer.parseInt(pen[1]);
                            obR.get(i).pen[2] = Integer.parseInt(pen[2]);

                            String brush[] = ibrush.substring(11,ibrush.length()-1).split(",");
                            obR.get(i).brush[0] = Float.parseFloat(brush[0]);
                            obR.get(i).brush[1] = Float.parseFloat(brush[1]);
                            obR.get(i).brush[2] = Float.parseFloat(brush[2]);

                            String location[] = ilocation.split(" ");
                            obR.get(i).location[0] = Float.parseFloat(location[5]);
                            obR.get(i).location[1] = Float.parseFloat(location[6]);

                            listRegion.add(obR.get(i));
                            i++;
                        }
                    }
                }
            }
            a= listLine.size();
            b = listPLine.size();
            int c = listText.size();
            int d= listRegion.size();
            int tru= 0;
        }
        catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void writerFile(){
        ObjectOutputStream writer = null;
        FileOutputStream fos = null;
        try {
            fos= mCtx.openFileOutput("Line.txt",Context.MODE_PRIVATE);
            writer = new ObjectOutputStream(fos);
//            ObjectLine obLine;
            int i=0;
            int size;
//           size = listLine.size();
//            while (i<size){
//                obLine = listLine.get(i);
//                writer.writeObject(obLine);
//                i++;
//            }
            writer.writeObject(listLine);
            writer.close();

            fos= mCtx.openFileOutput("Poliline.txt",Context.MODE_PRIVATE);
            writer = new ObjectOutputStream(fos);
            ObjectPoliline obPline;
            i=0;
            size = listPLine.size();
            while (i<size){
                obPline = listPLine.get(i);
                writer.writeObject(obPline);
                i++;
            }
            writer.close();

            fos= mCtx.openFileOutput("Text.txt",Context.MODE_PRIVATE);
            writer = new ObjectOutputStream(fos);
//            ObjectText obText;
//            i=0;
//            size = listText.size();
//            while (i<size){
//                obText = listText.get(i);
//                writer.writeObject(obText);
//                i++;
//            }
            writer.writeObject(listText);
            writer.close();

            fos= mCtx.openFileOutput("Region.txt",Context.MODE_PRIVATE);
            writer = new ObjectOutputStream(fos);
            ObjectRegion obRegion;
            i=0;
            size = listRegion.size();
            while (i<size){
                obRegion = listRegion.get(i);
                writer.writeObject(obRegion);
                i++;
            }
            writer.close();

        } catch (IOException e) {
            //log the exception
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }


    private void read (){
        ObjectInputStream in = null;
        FileInputStream fin = null;
        try {
            fin = (FileInputStream) mCtx.getAssets().open("Poliline.txt");
            in = new ObjectInputStream(fin);
            ObjectPoliline obPline;
            //obline = (ObjectLine) in.readObject();
            //p[0] = (iPont) in.readObject();
            obPline = (ObjectPoliline) in.readObject();
            int i =1;
            while (obPline != null){
                listPLine.add(obPline);
                obPline = (ObjectPoliline) in.readObject();
                i++;
            }
            int a = listPLine.size();
            int b=0;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

    }

}
*/

public class MapView extends View {
    private int scrWidth ;
    private int scrHeight ;
    public void zoomin() {
        mScale*=1.5;
        invalidate();
    }
    public void zoomout() {
        mScale/=1.5;
        invalidate();
    }

    public static int mHeight = 2000;
    public static int mWidth = 1580;
    Point currentCell;
    private float mlat = 13.32f;//lattitude of the center of the screen
    private float mlon = 109.43f;//longtitude of the center of the screen
    private float mScale = 5;// 1km = mScale*pixcels
    private int scrCtY,scrCtX;
    private ReadFile readFile ;
    int levelPreference =0;
    Point pointtemp;
    PointF dragStart,dragStop;
    PointF pointTestlatlon;
    private Context mCtx;
    Paint depthLinePaint, testpaint, paintRoute;
    Paint paintRegion;
    Path mPath;
    TextPaint textPaint;
    float textSize =30f;
    private Matrix mMatrix;
    boolean location = false;
    float location_lon, location_lat;
    PointF deltaPointF;

    boolean ontouch = false;
    Button buttonZoomIn,buttonZoomOut;
    //private Point pt = new Point(scrWidth/2,scrHeight/2);
    private ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;
    private BroadcastReceiver broadcast;
    int count;
    //lo trinh
    List<Text> routes;
    Route places;
    boolean onRoute = false;
    boolean chooseplace = false;

    Handler handler2;
    Thread thread1, thread2, thread3;
    boolean isItOk = false ;
    int dx1,dy1,dx2,dy2,delta;
    SurfaceHolder holder;
    int topX, topY, botX, botY;
    boolean start = true;

    //--------------//
    Vector<Line> lineScreens = new Vector<>();
    Vector<Vector<PointF>> polylineScreens = new Vector<>();
    Vector<Text> textScreen = new Vector<>();
    //--------------//

    public MapView(Context context,AttributeSet attr) {
        super(context, attr);
        mCtx = context; //<-- fill it with the Context you are passed
        readFile = new ReadFile(context);
        currentCell = new Point();
        textPaint = new TextPaint();
        depthLinePaint = new Paint();
        testpaint = new Paint();
        paintRegion = new Paint();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleLister());
        gestureDetector = new GestureDetector(context, new GestureListener());
        mMatrix = new Matrix();
        places = new Route();
        getDataUseThread();
//        handler2 = new Handler();

    }

    public void pause(){
        isItOk = false;
        while(true){
            try{
                thread1.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            break;
        }
        thread1 = null;
    }

//    public void resume(){
//        isItOk = true;
//        thread1 = new Thread(this);
//        thread1.start();
//
//    }

    @SuppressLint("DrawAllocation")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDraw(Canvas canvas) {// draw function
        super.onDraw(canvas);
        Log.d("draw","draw");

        mHeight = getHeight();
        mWidth = getWidth();
        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;

        int radius = Math.min(scrCtX, scrCtY);
        float pi = (float) Math.PI;
        //canvas.drawColor(Color.rgb(30,30,30));
        textPaint.setStyle(Paint.Style.STROKE);
        depthLinePaint.setStyle(Paint.Style.STROKE);
        depthLinePaint.setColor(Color.RED);
        //draw a condinate center
        Paint paint_center = new Paint();
        paint_center.setColor(Color.BLACK);
        paint_center.setTextSize(scrCtX / 10);
        canvas.drawCircle(scrCtX, scrCtY, 5, paint_center);
        canvas.drawText(mlat + " + " + mlon, scrCtX + 10, scrCtY + 15, paint_center);
        canvas.drawText("mScale : " + mScale, 20, 20, paint_center);

        if (location) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
        }
        if (onRoute){
            Path path= new Path();
            paintRoute = new Paint();
            paintRoute.setColor(Color.rgb(120,120,10));
            paintRoute.setStyle(Paint.Style.STROKE);
            paintRoute.setStrokeWidth(5);
            int i =0;
            for(Text t:routes) {
                Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
                Point p1 = ConvWGSToScrPoint(t.point1.x, t.point1.y);
                Paint locationPaint = new Paint();
                canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
                if(i == 0) path.moveTo(p1.x, p1.y);
                else path.lineTo(p1.x, p1.y);
                i++;
            }
            canvas.drawPath(path,paintRoute);
        }

        if(chooseplace){
            ondrawchoosePlacetoRoute(canvas);
        }
        //textPaint.setTextSize(textSize);
        paintRegion.setStyle(Paint.Style.FILL_AND_STROKE);

//        PointF topRightLatLon = ConvScrPointToWGS(scrCtX*2,0);
//        PointF botLeftLatLon = ConvScrPointToWGS(0,scrCtY*2);
////
////        int topX = (int) topRightLatLon.x;
////        int topY = (int) topRightLatLon.y;
//////
////        int botX = (int) botLeftLatLon.x;
////        int botY = (int) botLeftLatLon.y;
//        //sc all cells inside the screen and draw all text object
//
        PointF point = new PointF(0, 0);
//        int dem =0;




//        for (int cellLon = botX; cellLon <= topX; cellLon += 1) {
//            for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
//                //draw data of current cell
//                currentCell = new Point(cellLon, cellLat);
//
//                // draw text
//                if (readFile.listText.containsKey(currentCell)) {
//                    Vector<Text> objectList = readFile.listText.get(currentCell);
//                    for (Text obj : objectList) {
//                        Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
//                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
//                        int distance = Distance(p1, p2);
////                        if (mScale > getWidth() / 70) {
//                        int color = obj.pen[2];
//                        int red = (int) color / 65536;
//                        int green = (int) (color - red * 65536) / 256;
//                        int blue = (int) (color - red * 65536 - green * 256);
//                        try {
//                            Typeface tf = setFont(obj.pen[0], obj.font);
//                            textPaint.setTypeface(tf);
//                        } catch (Exception e) {
//                        }
//                        ;
//
//                        textPaint.setColor(Color.rgb(red, green, blue));
//                        mPath = new Path();
//                        mPath.moveTo(p1.x, p1.y);
//                        mPath.lineTo(p2.x, p2.y);
//                        if (mScale <= 1.5f && levelPreference == 1) {
////                            textPaint.setTextSize(distance / 11f);
//                            textPaint.setTextSize(distance / obj.name.length());
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        } else if (mScale > 2 && mScale <= 6 && levelPreference == 2) {
////                            textPaint.setTextSize(distance / 10f);
//                            textPaint.setTextSize(distance / obj.name.length());
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        } else if (levelPreference == 3 && mScale > 6) {
////                            textPaint.setTextSize(distance / 4f);
//                            textPaint.setTextSize(distance / obj.name.length() / 2);
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        }
//                        canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
//                    }
//                }
//                // draw lines
//                if(readFile.listLine.containsKey(currentCell)){
//                    Vector<Line> objLineList = readFile.listLine.get(currentCell);
//                    for (Line obj:objLineList){
//                        Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
//                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
//                        if(p1.equals(p2)) continue;
//                        //textPaint.setFontFeatureSettings(obj.font);
//                        int color = obj.pen[2];
//                        int red =(int) color /65536;
//                        int green = (int) (color - red * 65536) / 256;
//                        int blue = (int) (color - red * 65536 - green * 256);
//                        depthLinePaint.setColor(Color.rgb(red, green, blue));
//                        depthLinePaint.setStrokeWidth(obj.pen[0]);
//                        canvas.drawLine(p1.x, p1.y,p2.x,p2.y,depthLinePaint);
//
//                    }
//                }
//
//                // draw Polyline
//
//
//                for (Polyline pl: readFile.listPLine){
//                    Vector<PointF> pointfs = pl.lines.get(currentCell);
//                    if (pointfs == null) continue;
//                    dem ++;
//                    int size = pointfs.size() * 4;
//                    float pointis[] = new float[size];
//                    for (int i = 0; i < size - 4; i += 4) {
//                        PointF pointf1 = pointfs.elementAt(i / 4);
//                        PointF pointf2 = pointfs.elementAt(i / 4 + 1);
//                        if (pointf1.equals(point) || pointf2.equals(point) || pointf1.equals(pointf2))
//                            continue;
//                        Point p1 = ConvWGSToScrPoint(pointf1.x, pointf1.y);
//                        Point p2 = ConvWGSToScrPoint(pointf2.x, pointf2.y);
//                        pointis[i] = (float) p1.x;
//                        pointis[i + 1] = (float) p1.y;
//                        pointis[i + 2] = (float) p2.x;
//                        pointis[i + 3] = (float) p2.y;
//                    }
//                    int color = pl.pen[2];
//                    int red = (int) color / 65536;
//                    int green = (int) (color - red * 65536) / 256;
//                    int blue = (int) (color - red * 65536 - green * 256);
//                    depthLinePaint.setColor(Color.rgb(red, green, blue));
//                    depthLinePaint.setStrokeWidth(pl.pen[0]);
//                    //             canvas.drawPath(lines,depthLinePaint);
//                    canvas.drawLines(pointis, depthLinePaint);
//                }
//
//
//                // draw region
//
//                for(Region obr: readFile.listRegion){
//                    Vector<PointF> pointfs = obr.lines.get(currentCell);
//                    if(pointfs==null)continue;
//                    int size = pointfs.size()*4;
//                    float pointis[] = new float[size];
//                    for (int i=0;i<size-4;i+=4) {
//                        PointF pointf1 = pointfs.elementAt(i/4);
//                        PointF pointf2 = pointfs.elementAt(i/4 +1);
//                        if( pointf1.equals(point) || pointf2.equals(point)) continue;
//                        Point p1 = ConvWGSToScrPoint(pointf1.x, pointf1.y);
//                        Point p2 = ConvWGSToScrPoint(pointf2.x, pointf2.y);
//                        pointis[i] = (float) p1.x;
//                        pointis[i+1] =(float) p1.y;
//                        pointis[i+2] = (float) p2.x;
//                        pointis[i+3] =(float) p2.y;
//
//                    }
//                    int color = obr.pen[2];
//                    int red =(int) color /65536;
//                    int green = (int) (color - red * 65536) / 256;
//                    int blue = (int) (color - red * 65536 - green * 256);
//                    depthLinePaint.setColor(Color.rgb(red, green, blue));
//                    depthLinePaint.setStrokeWidth(obr.pen[0]);
//                    //             canvas.drawPath(lines,depthLinePaint);
//                    canvas.drawLines(pointis,depthLinePaint);
//                }
//
//            }
//        }
//        Log.d("ve binh thuong: ", "" + dem);
        //
        Log.d("hoang huy", "huy");

        for(Vector<PointF> p: polylineScreens){
            Log.d("hoang huy: ", "size: "+polylineScreens.size());
            int size = p.size() * 4;
                    float pointis[] = new float[size];
                    for (int i = 0; i < size - 4; i += 4) {
                        PointF pointf1 = p.elementAt(i / 4);
                        PointF pointf2 = p.elementAt(i / 4 + 1);
                        if (pointf1.equals(point) || pointf2.equals(point) || pointf1.equals(pointf2))
                            continue;
                        Point p1 = ConvWGSToScrPoint(pointf1.x, pointf1.y);
                        Point p2 = ConvWGSToScrPoint(pointf2.x, pointf2.y);
                        pointis[i] = (float) p1.x;
                        pointis[i + 1] = (float) p1.y;
                        pointis[i + 2] = (float) p2.x;
                        pointis[i + 3] = (float) p2.y;
                    }
//                    int color = pl.pen[2];
//                    int red = (int) color / 65536;
//                    int green = (int) (color - red * 65536) / 256;
//                    int blue = (int) (color - red * 65536 - green * 256);
//                    depthLinePaint.setColor(Color.rgb(red, green, blue));
//                    depthLinePaint.setStrokeWidth(pl.pen[0]);
                    //             canvas.drawPath(lines,depthLinePaint);
            canvas.drawLines(pointis, depthLinePaint);
        }

        for(Text obj: textScreen){
            Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
                        int distance = Distance(p1, p2);
//                        if (mScale > getWidth() / 70) {
                        int color = obj.pen[2];
                        int red = (int) color / 65536;
                        int green = (int) (color - red * 65536) / 256;
                        int blue = (int) (color - red * 65536 - green * 256);
                        try {
                            Typeface tf = setFont(obj.pen[0], obj.font);
                            textPaint.setTypeface(tf);
                        } catch (Exception e) {
                        }
                        ;

                        textPaint.setColor(Color.rgb(red, green, blue));
                        mPath = new Path();
                        mPath.moveTo(p1.x, p1.y);
                        mPath.lineTo(p2.x, p2.y);
                        if (mScale <= 1.5f && levelPreference == 1) {
//                            textPaint.setTextSize(distance / 11f);
                            textPaint.setTextSize(distance / obj.name.length());
                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
                        } else if (mScale > 2 && mScale <= 6 && levelPreference == 2) {
//                            textPaint.setTextSize(distance / 10f);
                            textPaint.setTextSize(distance / obj.name.length());
                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
                        } else if (levelPreference == 3 && mScale > 6) {
//                            textPaint.setTextSize(distance / 4f);
                            textPaint.setTextSize(distance / obj.name.length() / 2);
                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
                        }
                        canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
        }


        //

    }

    public void getDataUseThread(){


            PointF topRightLatLon = ConvScrPointToWGS(mWidth, 0);
            PointF botLeftLatLon = ConvScrPointToWGS(0, mHeight);
//
             topX = (int) topRightLatLon.x + 2;
             topY = (int) topRightLatLon.y + 2;
//
             botX = (int) botLeftLatLon.x - 2;
             botY = (int) botLeftLatLon.y - 2;
            //sc all cells inside the screen and draw all text object
                thread1 = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (int cellLon = botX; cellLon <= topX; cellLon += 1) {
                                    for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
                                        //draw data of current cell
                                        currentCell = new Point(cellLon, cellLat);

                                        //line
                                        if (readFile.listLine.containsKey(currentCell)) {
                                            Vector<Line> objLineList = readFile.listLine.get(currentCell);
                                            for (Line l : objLineList) {
                                                lineScreens.add(l);
                                                Log.d("line", l + "");
                                            }
                                        }
                                    }
                                }
                                Log.d("complete", "line");

                            }
                        });
                thread1.setPriority(Thread.MAX_PRIORITY);
            //plane
                thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (int cellLon = botX; cellLon <= topX; cellLon += 1) {
                            for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
                                //draw data of current cell
                                currentCell = new Point(cellLon, cellLat);
                                if (readFile.listText.containsKey(currentCell)) {
                                    Vector<Text> objectList = readFile.listText.get(currentCell);
                                    for (Text t : objectList) {
                                        textScreen.add(t);
                                    }
                                }

                            }
                        }


                    }
                });
                thread2.setPriority(Thread.MAX_PRIORITY);

                if(start) {
                    thread2.start();
                    thread1.start();
                }


//                thread3 = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        for (int cellLon = botX ; cellLon <= topX ; cellLon += 1) {
//                            for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
//                                //draw data of current cell
//                                currentCell = new Point(cellLon, cellLat);
//
//                                for (Polyline pl : readFile.listPLine) {
//                                    Vector<PointF> pointfs = pl.lines.get(currentCell);
//                                    if (pointfs == null) continue;
//                                    polylineScreens.add(pointfs);
//                                    Log.d("polyline", pointfs + "");
//                                }
//                            }
//                        }
//                    }
//                });
//                thread3.setPriority(Thread.MAX_PRIORITY);
//
//                if(start){
//                    thread3.start();
//                }

        for (int cellLon = botX ; cellLon <= topX ; cellLon += 1) {
            for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
                //draw data of current cell
                currentCell = new Point(cellLon, cellLat);

                for (Polyline pl : readFile.listPLine) {
                    Vector<PointF> pointfs = pl.lines.get(currentCell);
                    if (pointfs == null) continue;
                    polylineScreens.add(pointfs);
                    Log.d("polyline", pointfs + "");
                }
            }
        }

                invalidate();
    }




    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public  Typeface setFont(int valueFont, String font){
        Typeface tf;
        if(font.contains("arial")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.arial);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.arialb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.ariali);
            levelPreference = 3;
        }
        else if(font.contains("tahoma")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.tahoma);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.tahomab);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.tahomai);
            levelPreference = 2;
        }
        else if(font.contains("vnarial")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarial);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vnariali);
            levelPreference =3;
        }
        else if(font.contains("vnarialh")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialh);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialhb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vnarialhi);
            levelPreference = 2;
        }
        else if(font.contains("vntimeh")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimeh);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimehb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vntimehi);
            levelPreference =2;
        }
        else if(font.contains("vntime")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vntime);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimehb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vntimehi);
            levelPreference =2;
        }
        else if(font.contains("vnhelvetinsh")){
            levelPreference =1;
            tf = ResourcesCompat.getFont(mCtx,R.font.vnarialb);
        }
        else {
            levelPreference =2;
            tf = ResourcesCompat.getFont(mCtx,R.font.arial);
        }
        return tf;
    }

//    public int camparePoint(Point point1, Point point2){
//        if (point1.x != point2.x) {
//            return Integer.compare(point1.x, point2.x);
//        } else {
//            return Integer.compare(point1.y, point2.y);
//        }
//    }

    public Point ConvWGSToScrPoint(float m_Long,float m_Lat)// converting lat lon  WGS coordinates to screen XY coordinates
    {
        Point s = new Point();
        float refLat = (mlat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((int )(mScale*((m_Long - mlon) * 111.31949079327357)*cos(refLat))+scrCtX,
                (int)(mScale*((mlat- (m_Lat)) * 111.132954))+getHeight()/2
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

    //    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        points.add(new Point((int)event.getX(), (int)event.getY()));
//        invalidate();
//        return true;
//    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //points.add(new Point((int)event.getX(), (int)event.getY()));
            //pt.set((int)event.getX(), (int)event.getY());
            dragStart = new PointF(event.getX(),event.getY());
            dx1 = (int) event.getX();
            dy1 = (int) event.getY();

        }
//        else if (event.getAction() == MotionEvent.ACTION_UP) {
//            //pt.set((int)event.getX(), (int)event.getY());
//            //points.add(new Point((int)event.getX(), (int)event.getY()));
//            dragStop = new PointF(event.getX(),event.getY());
//            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
//            mlat=newLatLon.y;
//            mlon=newLatLon.x;
//            deltaPointF = ConvScrPointToWGS((int)(dragStart.x - dragStop.x), (int) (dragStart.y - dragStop.y));
//            ontouch = true;
//            postInvalidate();
//        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            dragStop = new PointF(event.getX(),event.getY());
            dx2 = (int) event.getX();
            dy2 = (int) event.getY();
            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
            //deltaPointF = new PointF(newLatLon.x - mlon, newLatLon.y -mlat);
            mlat=newLatLon.y;
            mlon=newLatLon.x;
            dragStart = dragStop;
            ontouch = true;

            PointF leftBottomCoor = ConvScrPointToWGS(0,mHeight);

            int iBotX = (int) leftBottomCoor.x;
            int iBotY = (int) leftBottomCoor.y;

            PointF rightTopCoor = ConvScrPointToWGS(mWidth,0);

            int iTopX = (int) rightTopCoor.x;
            int iTopY = (int) rightTopCoor.y;

            if(iTopX > topX || iBotX < botX || iBotY < botY || iTopY > topY){
                start = false;
                getDataUseThread();
            }
            else invalidate();


        }

        return true;
    }



    private class ScaleLister extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector sgd){
            mScale *= sgd.getScaleFactor();
            textSize *= sgd.getScaleFactor();
            mScale = Math.max(1f, Math.min(mScale, 20));
            start = false;
            getDataUseThread();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e){
            mScale *= 1.5f;
            start = false;
            getDataUseThread();
            return true;
        }
    }

    private int Distance(Point p1, Point p2){
        return (int) Math.sqrt(Math.pow((p1.x -p2.x),2) + Math.pow((p1.y - p2.y),2));
    }

    public void setLonLat(float latLoc, float lonLoc){
        mlat = location_lat = latLoc;
        mlon = location_lon = lonLoc;
        mScale =4;
        location =true;
        invalidate();

    }

    public void choosePlacetoRoute(){
        chooseplace = true;
        int i = places.size();
        Text t = new Text();
        t.name = "Dia diem "+ (i+1);
        t.point1 = ConvScrPointToWGS(scrCtX, scrCtY);
        places.add(t);
        invalidate();
    }

    private void ondrawchoosePlacetoRoute(Canvas canvas){
        int i=0;
        Path path = new Path();
        paintRoute = new Paint();
        paintRoute.setColor(Color.rgb(120,120,10));
        paintRoute.setStyle(Paint.Style.STROKE);
        paintRoute.setStrokeWidth(3);
        for(Text t:places) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
            Point p1 = ConvWGSToScrPoint(t.point1.x, t.point1.y);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
            if (i == 0) path.moveTo(p1.x, p1.y);
            else path.lineTo(p1.x, p1.y);
            i++;
        }
        if(i>1)
            canvas.drawPath(path,paintRoute);

    }

    public List<Text> listPlace(){
        List<Text> list = new ArrayList<>();
        Collection<Vector<Text>> listVector = readFile.listText.values();
        for(Vector<Text> v: listVector){
            for(int i=0; i<v.size(); i++){
                if(!v.get(i).name.matches("(^-)*\\d+"))
                    list.add(v.get(i));
            }
        }
        return list;
    }

    public Route coordinateRoute(){
        return places;
    }


    public void drawRoute(List<Text> list){
        onRoute = true;
        mlat = list.get(0).point1.y;
        mlon = list.get(0).point1.x;
        routes = list;
        invalidate();
    }
    public void canceldrawRoute(){
        onRoute = false;
        invalidate();
    }

    public void setPoliLines(Vector<Vector<PointF>> pl){
        polylineScreens = pl;
    }

//    public static class MyHandler extends Handler{
//
//        private MapView map;
//        public MyHandler(MapView map){
//            super();
//            this.map = map;
//        }
//        @Override
//        public void handleMessage(Message msg){
//            Bundle b = msg.getData();
//            Vector<Vector<PointF>> pl = b.getParcelable("polylines");
//            map.setPoliLines(pl);
//        }
//    }


}


