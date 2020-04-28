package com.SeaMap.myapplication.classes;

import android.content.Context;

import com.SeaMap.myapplication.object.*;
import com.SeaMap.myapplication.object.Buoy;

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
    public static HashMap<String, Vector<Polyline>> Border_Map = new HashMap<>();
    public static HashMap<String,Vector<Polyline>> PLines = new HashMap<String, Vector<Polyline>>();
    public static HashMap<String,Vector<Text>> tTexts = new HashMap <String, Vector<Text>>();
    public static HashMap<String,Vector<Region>> Poligons = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> PolygonRivers = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> BaseRegions = new HashMap<String, Vector<Region>>();
    public static HashMap<String,Vector<Region>> BasePlgRiver = new HashMap<String, Vector<Region>>();

    public static HashMap<String,Vector<Buoy>> listBuoys =new HashMap<>();
    public static HashMap<String, Vector<Density>> listDensity = new HashMap<>();
    public static HashMap<String, Vector<Density>> listDensity1 = new HashMap<>();
    public static List<Text> ListPlace = new ArrayList<>();
    public static boolean dataReady = false;

    private Context mCtx;
    public ReadFile(Context context){
        super();
        mCtx = context;
        //readBoat();
        //readDensity();

        readBaseRegions();
        readBasePlgRivers();
        readBouys();
        readBorderMap();
        getListPlaceOnText();

    }
    public void  ReadBigData()
    {
        readRiver();
        readDataSeaMap();
        readDensity();
        dataReady =true;
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
            int value;
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
                        value = ( buff[row * elementSize + 4]) & 0xFF;
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
     private void addDensityPoint100(String key, int lat, int lon, int  value)
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
