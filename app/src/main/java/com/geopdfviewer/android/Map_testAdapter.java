package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;


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
        holder.MapName.setText(map.getM_name());
        if (map.getM_imguri() != ""){
            holder.MapImage.setImageURI(Uri.parse(map.getM_imguri()));
        }else holder.MapImage.setImageResource(R.drawable.ic_android_black);


        //holder.MapImage.fromUri(map.getImageId());
        //Glide.with(mContext).load(fruit.getImageId()).into(holder.fruitImage);
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
}