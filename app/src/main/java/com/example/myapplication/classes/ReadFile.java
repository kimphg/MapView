package com.example.myapplication.classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;

import com.example.myapplication.object.Buoy;
import com.example.myapplication.object.Density;
import com.example.myapplication.object.Line;
import com.example.myapplication.object.Polyline;
import com.example.myapplication.object.Region;
import com.example.myapplication.object.Text;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;



public class ReadFile {

    public static HashMap<String,Vector<Line>> Lines = new HashMap<String, Vector<Line>>();
    public static HashMap<String,Vector<Polyline>> PLines = new HashMap<String, Vector<Polyline>>();
    public static HashMap<String,Vector<Text>> tTexts = new HashMap <String, Vector<Text>>();
    public static HashMap<String,Vector<Region>> Poligons = new HashMap<String, Vector<Region>>();

    public static HashMap<String,Vector<Buoy>> vtBuoys =new HashMap<>();
    public static HashMap<String, Vector<Density>> listDensity = new HashMap<>();

    public static List<Text> ListPlace = new ArrayList<>();

    private Context mCtx;

    private int AreaX[];
    private int AreaY[];

    public ReadFile(Context context){
        super();
        mCtx = context;
        initArea();
        readFileByte();
        //readBoat();
        readDensity();
    }


    public void initArea(){
        AreaX = new int[20];
        AreaY = new int[20];
        int xT = 100;
        int yT = 6;
        for (int i = 0; i < 20; i++) {
            AreaX[i] = xT;
            AreaY[i] = yT;
            xT +=1;
            yT +=1;
        }
    }

    void readFileByte(){
        AssetManager am = mCtx.getAssets();
        for (int u = 0; u < 20; u++) {
            for(int v = 0; v<20; v++) {
                String area = AreaX[u] + "-" + AreaY[v];
                String keyLatLon = AreaX[u] + "-" + AreaY[v] + ".bin";
                ObjectInputStream ois = null;
                int size = 0;
                try {
                    InputStream is = am.open("locationBytes" + "/" + keyLatLon);
                    ois = new ObjectInputStream(is);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }

                //Text
                if (size != 0) {
                    Vector<Text> t = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Text obj = null;
                        try {
                            obj = (Text) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }

                        if(obj != null) {
                            t.add(obj);
                            if (obj.getType() != 1 && obj.getName().length() >= 6) ListPlace.add(obj);
                        }
                        //System.out.println(s);
                    }
                    tTexts.put(area, t);
                }

                // Line
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }
                if (size != 0) {
                    Vector<Line> l = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Line obj = null;
                        try {
                            obj = (Line) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            l.add(obj);
                        //areaLine[i].add(obj);
                        // System.out.println(s);
                    }
                    Lines.put(area, l);

                }

                //Pline
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }
                if (size != 0) {
                    Vector<Polyline> pl = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Polyline obj = null;
                        try {
                            obj = (Polyline) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            pl.add(obj);
                        //areaPline[i].add(obj);
                        // System.out.println(s);
                    }
                    PLines.put(area,pl);
                }

                //Region
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }

                if (size != 0) {
                    Vector<Region> r = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Region obj = null;
                        try {
                            obj = (Region) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            r.add(obj);
//                        areaRegion[i].add(obj);
                        // System.out.println(s);
                    }
                    if(area.equals("119-15")) {
                        Poligons.put("97-15",r);
                        continue;
                    }
                    Poligons.put(area,r);
                }
            }
        }

        int temp = 0;
    }

    private void readBoat(){
        ObjectInputStream ois = null;
        int size = 0;
        try {
            InputStream is = mCtx.getAssets().open("buoys.bin");
            ois = new ObjectInputStream(is);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            size = ois.readInt();
        } catch (IOException e) {
        }
        for (int j = 0; j < size; j++) {
                Buoy obj = null;
                try {
                    obj = (Buoy) ois.readObject();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                }

                String area = (int)obj.getCoordinates()[0] + "-" + (int)obj.getCoordinates()[1];
                if(vtBuoys.containsKey(area)) {
                    Vector<Buoy> vtFL = vtBuoys.get(area);
                    vtFL.add(obj);
                    vtBuoys.put(area, vtFL);
                }
                else {
                    Vector<Buoy> vtFL = new Vector<>();
                    vtFL.add(obj);
                    vtBuoys.put(area, vtFL);
                }

            }
        System.out.println("");
    }


    public void readDensity(){
        ObjectInputStream ois = null;
        int sizeList = 0, sizeVt = 0;
        Vector<Density> vtDensity = new Vector<>();
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("density.bin");
            ois = new ObjectInputStream(is);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            sizeList = ois.readInt();
        } catch (IOException e) {
        }
        for (int j = 0; j < sizeList; j++) {
            try {
                key = (String) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                vtDensity = (Vector<Density>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            listDensity.put(key, vtDensity);
        }
        System.out.println("");
    }

}
