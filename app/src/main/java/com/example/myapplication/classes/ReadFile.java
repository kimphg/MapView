package com.example.myapplication.classes;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;

import com.example.myapplication.object.Line;
import com.example.myapplication.object.Polyline;
import com.example.myapplication.object.Region;
import com.example.myapplication.object.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ReadFile {
    private Context mCtx;
    public Map<Point, Vector<Line>> listLine = new HashMap<Point, Vector<Line>>();
    public Vector<Polyline> listPLine = new Vector <Polyline>();
    public Map <Point,Vector<Text>> listText = new HashMap<Point,Vector<Text>>();
    public Vector<Region> listRegion = new Vector<Region>();

    public ReadFile(Context context){
        super();
        mCtx = context;
        readFile();
    }

    private void readFile() {
        BufferedReader reader = null;
        try {
            String mline;
            Line obLine;
            Polyline obPline;
            Text obText;
            int a= listLine.size();
            int b = listPLine.size();
            reader = new BufferedReader(new InputStreamReader(mCtx.getAssets().open("lines.txt"), "UTF-8"));

            while ((mline = reader.readLine()) !=null){
                if(mline.contains("Line")){
                    obLine = new Line();
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
                    Vector<Line> cell;
                    if(listLine.containsKey(ikey)){
                        cell = listLine.get(ikey);
                    }
                    else {
                        cell = new Vector<Line>();
                    }
                    cell.add (obLine);
                    listLine.put(ikey, cell);
                }
                else if(mline.contains("Pline")){
                    String splitText [] = mline.split(" ");
                    int num = Integer.parseInt(splitText[1]);
                    obPline = new Polyline(num);
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
                    obText = new Text();
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
                    Vector<Text> cell;
                    if(listText.containsKey(ikey)){
                        cell = listText.get(ikey);
                    }
                    else {
                        cell = new Vector<Text>();
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
                        Region obRegion = new Region(numberPoint);
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
                        Vector<Region> obR = new Vector<Region>();
                        while (!(mline.contains("Pen"))) {
                            String num[] = mline.split(" ");
                            int numberPoint = Integer.parseInt(num[2]);
                            Region obRegion = new Region(numberPoint);
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
}
