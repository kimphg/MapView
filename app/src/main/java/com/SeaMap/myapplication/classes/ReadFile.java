package com.SeaMap.myapplication.classes;
import java.util.*;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.object.*;
import com.SeaMap.myapplication.object.Buoy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;



public class ReadFile {

    public static HashMap<String,Vector<Line>> Lines = new HashMap<String, Vector<Line>>();
    public static HashMap<String, Vector<Polyline>> Border_Map = new HashMap<>();
    public static HashMap<String,Vector<Polyline>> PLines = new HashMap<String, Vector<Polyline>>();
    public static HashMap<String,Vector<Text>> tTexts = new HashMap <String, Vector<Text>>();
    public static HashMap<String,Vector<Region>> Poligons = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> PolygonRivers = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> BaseRegions = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> BasePlgRiver = new HashMap<String, Vector<Region>>();

    public static HashMap<String,Vector<Buoy>> listBuoys =new HashMap<>();
    public static HashMap<String, Vector<Density>> listDensity = new HashMap<>();
//    public static HashMap<String, Vector<Density>> listDensity1 = new HashMap<>();
    private static int mID = 0;
    private static HashMap<String, String> mConfig = new HashMap<String, String>();
//    private static HashMap<String, String> mSavedPointsAsStrings = new HashMap<String, String>();
    private static boolean  isConfigChanged = false;
    private static boolean  isConfigBusy = false;
    public static List<Text> ListPlace = new ArrayList<>();
    public static boolean dataReady = false;
    private static File userConfigFile;
    private  static Context mCtx;

    public ReadFile(Context context){
        super();
        mCtx = context;
        //readBoat();
        //readDensity();

        readBaseRegions();
        readBasePlgRivers();
        readBouys();
        readBorderMap();
        LoadConfig();
    }

    public static String GetConfig(String key) {
        if(!mConfig.containsKey(key))
        {
            SetConfig(key,"0");
            return "0";
        }
        else
        {
            return mConfig.get(key);
        }

    }

    public static int getID() {

        if(mID==0)
            {
                mID = Integer.parseInt(GetConfig("ID"));
                if(mID==0)
                {
                    // create random object
                    Random ran = new Random();

                    // generating integer
                    int nxt = ran.nextInt();
                    mID = Math.abs(nxt);
                    SetConfig("ID",String.valueOf(mID));
                    SaveConfig();
                }
            }
        return mID;
    }
    private static String  KeyValSeparator = "`";
    private static String  ElementSeparator = "#";
    private static String  ConfigSeparator = ";";
    public static Vector<MapPoint> mMapPointList = new Vector<MapPoint> ();
    public static void  removemapPoint(String latlonString)
    {
        for(MapPoint point:mMapPointList)
        {
            if(latlonString==point.mlatlonString)
            {
                mMapPointList.remove(point);
                break;
            }
        }
        String mSavedPointsAsStrings = "";
        for(MapPoint point :mMapPointList) {
            if(point.type!=4)
                mSavedPointsAsStrings += point.mName + KeyValSeparator + point.mlatlonString + ElementSeparator;

        }
        SetConfig("saved_points", mSavedPointsAsStrings);
    }
    public static void AddToSavedPoints(MapPoint newPoint )//float mlat, float mlon,String name)
    {
        if(newPoint.type==4)
        {
            removemapPoint(newPoint.mlatlonString);
        }
        else {
            boolean pointExist = false;
            for (MapPoint point : mMapPointList) {
                if (newPoint.mlatlonString == point.mlatlonString) {
                    point.SetPoint(newPoint);
                    pointExist = true;
                }
            }
            if (pointExist)//overwrite saved_points
            {
                String mSavedPointsAsStrings = "";
                for (MapPoint point : mMapPointList) {
                    if (point.type != 4)
                        mSavedPointsAsStrings += point.mName + KeyValSeparator + point.mlatlonString + ElementSeparator;

                }
                SetConfig("saved_points", mSavedPointsAsStrings);
            } else {//add to saved_points
                mMapPointList.add(newPoint);
                String mSavedPointsAsStrings = GetConfig("saved_points");
                mSavedPointsAsStrings += newPoint.mName + KeyValSeparator + newPoint.mlatlonString + ElementSeparator;
                SetConfig("saved_points", mSavedPointsAsStrings);
            }
        }

    }

    public static Vector<MapPoint>  GetSavedPoints()
    {
        return mMapPointList;
    }
    private void LoadConfig() {
        userConfigFile =  new File(mCtx.getFilesDir(), "cfg.txt");
        if(userConfigFile.exists()) {
            try {
                FileInputStream stream = new FileInputStream(userConfigFile);
            }
            catch (FileNotFoundException ex)
            {
                try {
                    userConfigFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try {
                userConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream  inputStream = mCtx.openFileInput(userConfigFile.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                while(true) {
                    String line = reader.readLine();
                    if(line==null)break;
                    String[] strList = line.split(ConfigSeparator);
                    if ( strList.length< 2)continue;
                    String key = strList[0];
                    String value = strList[1];
                    mConfig.put(key,value);
                }
                if(!mConfig.containsKey("ID"))
                {
                    SetConfig("ID","0");
                }
                String savedPoint = mConfig.get("saved_points");
                if(savedPoint!=null)
                if(savedPoint.length()>2) {
                    String[] strPoints = savedPoint.split(ElementSeparator);
                    if(strPoints.length>0)
                    {
                        for (String str:strPoints
                             ) {
                            String[] keylatlon = str.split(KeyValSeparator);
                            if(keylatlon.length==2)
                            {
                                String name = keylatlon[0];
                                String latlon = keylatlon[1];
                                MapPoint newPoint = new MapPoint(name,latlon);
                                mMapPointList.add(newPoint);
                            }

                        }
                    }
                }

            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            }
        }
        catch (IOException e)
        {

        }
    }
    public static void SetConfig(String key,String value)
    {
        while(isConfigBusy){};
        mConfig.put(key,value);
        isConfigChanged = true;
    }
    public static void SaveConfig()
    {
        if(!isConfigChanged)return;
        isConfigChanged = false;
        try {
            isConfigBusy = true;
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mCtx.openFileOutput("cfg.txt", Context.MODE_PRIVATE));
            for (HashMap.Entry<String, String> element : mConfig.entrySet()) {

                outputStreamWriter.write(element.getKey()+ConfigSeparator+element.getValue()+"\n");

            }
            outputStreamWriter.close();
            isConfigBusy = false;
        }
        catch (FileNotFoundException ex)
        {

        } catch (IOException e) {
            e.printStackTrace();
        }
        isConfigBusy = false;

    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void  ReadBigData()
    {
        try {
            dataReady = false;
            readRiver();
            readDataSeaMap();
            if(Build.VERSION.SDK_INT>=23)readDensity();
            Thread.sleep(500);
            dataReady = true;
        } catch (InterruptedException e) {
            return;
        }
    }
    private void readDataSeaMap(){
        ObjectInputStream ois = null;
        int sizeList = 0, sizeVt = 0;
        Vector<Region> vtRegion = new Vector<>();
        Vector<Text> vtText = new Vector<>();
        Vector<Polyline> vtPline = new Vector<>();
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("dataseamap.bin");
            ois = new ObjectInputStream(is);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Todo read poligons
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
                vtRegion = (Vector<Region>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Poligons.put(key, vtRegion);
        }

        //Todo read polyline
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
                vtPline = (Vector<Polyline>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PLines.put(key, vtPline);
        }

        //Todo read text
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
                vtText = (Vector<Text>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tTexts.put(key, vtText);
        }
        System.out.println("");
    }

    private void readBouys(){
        ObjectInputStream ois = null;
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
            listBuoys = (HashMap<String, Vector<Buoy>>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(listBuoys);
    }


    private void readDensity(){
        try {
            InputStream inputStream = mCtx.getAssets().open("density.bin");
            int max_row_count = 4000000;
            int elementSize = 5;
            int lat,lon, rowCount;
            short value;
            byte[] buff = new byte[max_row_count*elementSize];
            while(true)
            {
                try {
                    lat = inputStream.read() ;
                    lat += inputStream.read() << 8;
                    lat += inputStream.read() << 16;
                    lat += inputStream.read() << 24;
                    rowCount = inputStream.read() ;
                    rowCount += inputStream.read() << 8;
                    rowCount += inputStream.read() << 16;
                    rowCount += inputStream.read()<< 24;
//                if( (inputStream.read()&lat,sizeof(lat),1,binFile) )<1)break;//read lat
//                if( (fread(&rowCount,sizeof(rowCount),1,binFile) )<1)break;//read rowCount
                    if (rowCount < 1 || rowCount > max_row_count) break;//check rowcount
//                if( (fread(buff,sizeof(uchar),rowCount*elementSize,binFile))<1)break;//read data
                    inputStream.read(buff, 0, rowCount * elementSize);
                    for (int row = 0; row < rowCount; row++) {
                        //memcpy(&lon,buff+row*elementSize,sizeof(row));
                        lon = (buff[row * elementSize]) & 0xFF ;
                        lon += ( (buff[row * elementSize + 1] ) & 0xFF) << 8;
                        lon += ( (buff[row * elementSize + 2] ) & 0xFF) << 16;
                        lon += ( (buff[row * elementSize + 3] ) & 0xFF) << 24;
                        value = (short)(( buff[row * elementSize + 4]) & 0xFF);
                        String key = Integer.toString(lon/1000)+","+Integer.toString(lat/1000);
                        addDensityPoint100(key,lat,lon,value);

                    }
                }
                catch (IOException ex)
                {
                    break;
                }
            }

            return;
        }catch (FileNotFoundException ex)
        {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (OutOfMemoryError E)
        {
            listDensity.clear();
            return;
        }

        /*
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
        System.out.println("");*/
    }
     private void addDensityPoint100(String key, int lat, int lon, short  value)
     {
         if(listDensity.containsKey(key))
         {
             listDensity.get(key).add(new Density(lat/1000.0f,lon/1000.0f,value));

         }
         else {
             Vector<Density> vtDensity = new Vector<Density>();
             vtDensity.add(new Density(lat/1000.0f,lon/1000.0f,value));
             listDensity.put(key, vtDensity);
         }

     }

    public void readRiver(){
        ObjectInputStream ois = null;
        int sizeList = 0;
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("data_river_lake.bin");
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
            Vector<Region> vtriver = null;
            try {
                vtriver = (Vector<Region>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PolygonRivers.put(key, vtriver);
        }
        System.out.println("");
    }

    public void readBaseRegions(){
        ObjectInputStream ois = null;
        int sizeList = 0;
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("baseRegions.bin");
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
            Vector<Region> vtRegion = null;
            try {
                vtRegion = (Vector<Region>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BaseRegions.put(key, vtRegion);
        }
        System.out.println("");
    }

    public void readBasePlgRivers(){
        ObjectInputStream ois = null;
        int sizeList = 0;
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("basePlgRivers.bin");
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
            Vector<Region> vtriver = null;
            try {
                vtriver = (Vector<Region>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BasePlgRiver.put(key, vtriver);
        }
        System.out.println("");
    }

    public void getListPlaceOnText(){
        for (Map.Entry place : tTexts.entrySet()) {
            String key =(String) place.getKey();
            Vector<Text> namePlace = (Vector<Text>) place.getValue();

            for(int i =0; i<namePlace.size(); i++){
                int type = namePlace.get(i).getType();
                if(type != 0 && type != 1){
                    ListPlace.add(namePlace.get(i));
                }
            }
        }
    }

    private void readBorderMap(){
        ObjectInputStream ois = null;
        int sizeList = 0;
        String key = "";
        try {
            InputStream is = mCtx.getAssets().open("border_map.bin");
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
            Vector<Polyline> border_area = null;
            try {
                border_area = (Vector<Polyline>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Border_Map.put(key, border_area);
        }
        System.out.println("");
    }

}
