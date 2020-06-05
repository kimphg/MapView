package com.SeaMap.myapplication.classes;
import java.io.FileOutputStream;
import java.util.*;
import android.content.Context;
import android.location.Location;
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



public class GlobalDataManager {
    private static String configFileName = "cfg.txt";
    private static String loclogFileName = "log.bin";
    private static String pointFileName = "point.bin";
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
//    public static List<MapText> ListPlace = new ArrayList<>();
    public static boolean dataReady = false;
//    private static File userConfigFile;
    private  static Context mCtx;
    private  static List<MapPoint> locationHistory =  Collections.synchronizedList( new LinkedList<MapPoint>());
    public static void Init(Context context){
        mCtx = context;
        //readBoat();
        //readDensity();

        readBaseRegions();
        readBasePlgRivers();
        readBouys();
        readBorderMap();
        LoadConfig();

    }
    private static boolean checkFileExist(String fileName)
    {
        File file =  new File(mCtx.getFilesDir(), fileName);
        if(file.exists()) {
            try {
                FileInputStream stream = new FileInputStream(file);
                stream.close();
            }
            catch (FileNotFoundException ex)
            {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private static void SaveLocationHistory()
    {
        checkFileExist(loclogFileName);
        try {

            byte[] outputdata = new byte[locationHistory.size()*MapPoint.bytesSize];
            int pos = 0;
            for (MapPoint point:locationHistory)
            {
                System.arraycopy(point.toBytes(),0,outputdata,pos,MapPoint.bytesSize);
                pos+= MapPoint.bytesSize;
            }
            File file =  new File(mCtx.getFilesDir(), loclogFileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            fileOut.write(outputdata);
            fileOut.close();
            //System.out.println("The Object  was succesfully written to a file");
//            fileOut.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void LoadLocationHistory()  {
        if(!checkFileExist(loclogFileName))return;

        try {
            File file = new File(mCtx.getFilesDir(), loclogFileName);
            byte[] input = new byte[(int)file.length()];
            FileInputStream fileIn = new FileInputStream(file);
            fileIn.read(input);
            fileIn.close();
            byte[] data = new byte[MapPoint.bytesSize];
            for (int pos = 0;pos<input.length-MapPoint.bytesSize+1;pos+=MapPoint.bytesSize)
            {
                System.arraycopy(input,pos,data,0,MapPoint.bytesSize);
                MapPoint newPoint = new MapPoint(data);
                locationHistory.add(newPoint);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        ///
//        ObjectInputStream ois = null;
//        try {
//            // mở FileInputStream
//            File file =  new File(mCtx.getFilesDir(), loclogFileName);
//            long len = file.length();
//            FileInputStream fis = new FileInputStream(file);
//            ois = new ObjectInputStream(fis);
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        if(ois==null)return;
//        try {
//            while (true) {
//                //đọc từng point
//                MapPoint newPoint = (MapPoint) ois.readObject();
////                if(newPoint==null)break;
//                locationHistory.add(newPoint);
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private static  void LoadSavedPoints() {
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
    public static void SaveData()
    {
        SaveConfig();
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
    private static String  LatLonSeparator = ",";
    public  static ArrayList<MapPoint> mMapPointList = new ArrayList<MapPoint> ();
    private static void SaveMapPoints()
    {
        String mSavedPointsAsStrings = "";
        for(MapPoint point :mMapPointList) {
            if(point.mType !=4)
                mSavedPointsAsStrings += point.mName + KeyValSeparator + point.DataString() + ElementSeparator;

        }
        SetConfig("saved_points", mSavedPointsAsStrings);
    }
    public static void  removemapPoint(String name)
    {
        for(MapPoint point:mMapPointList)
        {
            if(point.mName.equals(name))
            {
                mMapPointList.remove(point);
                break;
            }
        }

    }
    public static void replaceMapPoint(String name, MapPoint newpoint)
    {
        for(MapPoint point:mMapPointList)
        {
            if(point.mName.equals(name))
            {
                point.copyData(newpoint);
                return;
            }
        }
    }
    public static void AddToSavedPoints(MapPoint newPoint )//float mlat, float mlon,String name)
    {
        newPoint.mName = GetUniqueMapPointName(newPoint.mName);
        mMapPointList.add(newPoint);

    }

    public static String GetUniqueMapPointName(String mName) {
        String newName = new String(mName);
        while(GlobalDataManager.checkMapPointNameExist(newName)>0)
        {
            String[] spaceSplited = newName.split(" - ");
            if(spaceSplited.length>1)
            {
                String indexString = spaceSplited[spaceSplited.length-1];
                try {

                    int index = Integer.parseInt(indexString);
                    for(int indexStep=1;;indexStep++) {
                        indexString = " " + String.valueOf(index + indexStep);
                        newName = "";
                        for (int i = 0; i < spaceSplited.length-1; i++) {
                            newName += spaceSplited[i] + "-";
                        }
                        newName += indexString;
                        if(GlobalDataManager.checkMapPointNameExist(newName)==0)
                        {
                            break;
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    newName+=" - 1";
                }
            }
            else newName+=" - 1";
        }
        return newName;
    }

    public static ArrayList<MapPoint>  GetSavedPoints()
    {
        return mMapPointList;
    }

    public static List<MapPoint> getLocationHistory() {
        return locationHistory;
    }

    private static void LoadConfig() {
        if(!checkFileExist(configFileName))return;
        try {
            FileInputStream  inputStream = mCtx.openFileInput(configFileName);
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
    private static void SaveConfig()
    {
        SaveMapPoints();
        if(!isConfigChanged)return;
        isConfigChanged = false;
        try {
            isConfigBusy = true;
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mCtx.openFileOutput(configFileName, Context.MODE_PRIVATE));
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
    public static  void  ReadBigData()
    {
        try {
            dataReady = false;
            LoadSavedPoints();
            LoadLocationHistory();
            readRiver();
            readDataSeaMap();
            if(Build.VERSION.SDK_INT>=23)readDensity();
            Thread.sleep(500);
            dataReady = true;
        } catch (InterruptedException e) {
            return;
        }
    }
    private static void readDataSeaMap(){
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

    private static  void readBouys(){
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


    private static  void readDensity(){
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
     private static void addDensityPoint100(String key, int lat, int lon, short  value)
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

    public static void readRiver(){
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

    public static void readBaseRegions(){
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
        //System.out.println("");
    }

    public static void readBasePlgRivers(){
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
        //System.out.println("");
    }

    public static List<Text> getListPlaceOnText() {
        List<Text> ListPlace = new ArrayList<>();
        for (Map.Entry place : tTexts.entrySet()) {
            String key = (String) place.getKey();
            Vector<Text> namePlace = (Vector<Text>) place.getValue();

            for (int i = 0; i < namePlace.size(); i++) {
                int type = namePlace.get(i).getType();
                if (type != 0 && type != 1) {
                    ListPlace.add(namePlace.get(i));
                }
            }
        }
        for (MapPoint point : mMapPointList) {
            float[] coor = new float[4];
            coor[0] = point.mlon;
            coor[1] = point.mlat;
            coor[2] = point.mlon;
            coor[3] = point.mlat;
            ListPlace.add(new Text(point.mName, coor, "", 0.0f, ""));
        }

        return ListPlace;
    }

    private static  void readBorderMap(){
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
        //System.out.println("");
    }
    private static MapPoint  lastSavedPoint = null;
    private static Long lastSavedTime = 0L;
    public static void AddLocationHistory(Location location) {
        MapPoint point = new MapPoint((float)location.getLatitude(),(float)location.getLongitude(),"",5);
        if(lastSavedPoint!=null)if((point.mTimeSec -lastSavedPoint.mTimeSec)<30)return;
        lastSavedPoint=(point);
        if(locationHistory.size()<30000)
        locationHistory.add(point);
        else
        {
            locationHistory.remove(0);
            locationHistory.add(point);
        }
        SaveLocationHistory();
    }

    public static MapPoint getMapPoint(String mapPointname) {
        for(MapPoint point:mMapPointList)
        {
            if(point.mName.equals(mapPointname))return point;
        }
        return null;
    }

    public static int checkMapPointNameExist(String mName) {
        int count=0;
        for(MapPoint point:mMapPointList)
        {
            if(point.mName.equals(mName))count++;
        }
        return count;
    }
}
