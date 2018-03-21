package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by 54286 on 2018/3/6.
 */

public class Map_testAdapter extends RecyclerView.Adapter<Map_testAdapter.ViewHolder> {
    private Context mContext;

    private List<Map_test> mMapList;

    private OnRecyclerItemLongListener mOnItemLong;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        ImageView MapImage;
        TextView MapName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            MapImage = (ImageView) view.findViewById(R.id.img_test);
            MapName = (TextView) view.findViewById(R.id.map_name);



        }
    }
    public Map_testAdapter(List<Map_test> mMapList) {
        this.mMapList = mMapList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.map_test_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Map_test map = mMapList.get(position);
                Intent intent = new Intent(mContext, MainInterface.class);
                intent.putExtra("num", map.getM_num());
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLong != null){
                    int position = holder.getAdapterPosition();
                    Map_test map = mMapList.get(position);
                    mOnItemLong.onItemLongClick(v, map.getM_num());
                    Toast.makeText(mContext, Integer.toString(map.getM_num()), Toast.LENGTH_LONG).show();
                    holder.cardView.setCardBackgroundColor(Color.GRAY);
               }
                //Toast.makeText(mContext, "see here", Toast.LENGTH_LONG).show();
                /*
                Intent intent = new Intent(mContext, select_page.class);
                intent.putExtra(select_page.LOC_DELETE_ITEM, map.getM_num());
                selectedNum = map.getM_num();
                //Toast.makeText(mContext, Integer.toString(map.getM_num()), Toast.LENGTH_LONG).show();
                //Toast.makeText(mContext, Integer.toString(map.getM_num()), Toast.LENGTH_LONG).show();
                //mContext.setTheme(R.style.DarkTheme);
                mContext.startActivity(intent);*/
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map_test map = mMapList.get(position);
        DecimalFormat df = new DecimalFormat("0.00");
        SharedPreferences pref1 = mContext.getSharedPreferences("latlong", MODE_PRIVATE);
        String mlatlong = pref1.getString("mlatlong", "");
        String[] latandlong;
        latandlong = mlatlong.split(",");
        double m_lat = Double.valueOf(latandlong[0]);
        double m_long = Double.valueOf(latandlong[1]);
        String[] latandlong1;
        latandlong1 = map.getM_center_latlong().split(",");
        double the_lat = Double.valueOf(latandlong1[0]);
        double the_long = Double.valueOf(latandlong1[1]);
        double distance = getDistancebtptandmap(map.getM_GPTS(), m_lat, m_long) / 1000;
        if (distance != 0) {
            holder.MapName.setText(map.getM_name() + "\n" + df.format(distance) + "公里");
        } else holder.MapName.setText(map.getM_name() + "\n" + "在地图上 ");
        /*holder.MapName.setText(map.getM_name() + "\n" + "距中心: " + df.format(algorithm(m_long, m_lat, the_long, the_lat) / 1000) + "公里"
                + "\n" + "在地图上 ");*/
        if (map.getM_imguri() != ""){
            holder.MapImage.setImageURI(Uri.parse(map.getM_imguri()));
        }else holder.MapImage.setImageResource(R.drawable.ic_android_black);

    }

    private Double getDistancebtptandmap(String GPTS, double lat, double longi){
        double distance = 0;
        Log.w(TAG, "catch you" + GPTS);
        String[] GPTString = GPTS.split(" ");
        float[] GPTSs = new float[GPTString.length];
        for (int i = 0; i < GPTString.length; i++) {
            GPTSs[i] = Float.valueOf(GPTString[i]);
        }
        float lat_axis, long_axis;
        PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
        lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
        long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
        for (int i = 0; i < GPTSs.length; i = i + 2){
            if (GPTSs[i] < lat_axis) {
                if (GPTSs[i + 1] < long_axis){
                    pt_lb.x = GPTSs[i];
                    pt_lb.y = GPTSs[i + 1];
                } else {
                    pt_rb.x = GPTSs[i];
                    pt_rb.y = GPTSs[i + 1];
                }
            } else {
                if (GPTSs[i + 1] < long_axis){
                    pt_lt.x = GPTSs[i];
                    pt_lt.y = GPTSs[i + 1];
                } else {
                    pt_rt.x = GPTSs[i];
                    pt_rt.y = GPTSs[i + 1];
                }
            }
        }
        float min_lat = (pt_lb.x + pt_rb.x) / 2;
        float max_lat = (pt_lt.x + pt_rt.x) / 2;
        float min_long = (pt_lt.y + pt_lb.y) / 2;
        float max_long = (pt_rt.y + pt_rb.y) / 2;

        if (lat >= min_lat && lat <= max_lat && longi >= min_long && longi <= max_long){
            distance = 0;
        }else if (lat >= min_lat && lat <= max_lat && (longi < min_long || longi > max_long)){
            if (longi < min_long ){
            distance = algorithm(longi, 0, min_long, 0);
            }else distance = algorithm(longi, 0, max_long, 0);
        }else if (( lat < min_lat || lat > max_lat) && longi >= min_long && longi <= max_long){
            if (lat < min_lat){
                distance = algorithm(0, lat, 0, min_lat);
            }else distance = algorithm(0, lat, 0, max_lat);
        }else {
            if (lat > max_lat && longi > max_long) distance = algorithm(longi, lat, max_long, max_lat);
            else if (lat > max_lat && longi < min_long) distance = algorithm(longi, lat, min_long, max_lat);
            else if (lat < min_lat && longi < min_long) distance = algorithm(longi, lat, min_long, min_lat);
            else distance = algorithm(longi, lat, max_long, min_lat);
        }
        return distance;
    }




    @Override
    public int getItemCount() {
        return mMapList.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,int position);
    }
    public void setOnItemLongClickListener(OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
    //距离量测(输入参数为 两点的经纬度)
    public static double algorithm(double longitude1, double latitude1, double longitude2, double latitude2) {

        double Lat1 = rad(latitude1); // 纬度

        double Lat2 = rad(latitude2);

        double a = Lat1 - Lat2;//两点纬度之差

        double b = rad(longitude1) - rad(longitude2); //经度之差

        double s = 2 * Math.asin(Math

                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));//计算两点距离的公式

        s = s * 6378137.0;//弧长乘地球半径（半径为米）

        s = Math.round(s * 10000d) / 10000d;//精确距离的数值

        return s;

    }

    //将角度转化为弧度
    private static double rad(double d) {

        return d * Math.PI / 180.00; //角度转换成弧度

    }
}
