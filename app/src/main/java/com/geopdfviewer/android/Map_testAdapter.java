package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
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

import com.github.barteksc.pdfviewer.PDFView;

import java.util.List;

/**
 * Created by 54286 on 2018/3/6.
 */

public class Map_testAdapter extends RecyclerView.Adapter<Map_testAdapter.ViewHolder> {
    private Context mContext;

    private List<Map_test> mMapList;

    static class ViewHolder extends RecyclerView.ViewHolder {
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
                //intent.putExtra(select_page.FRUIT_NAME, map.getM_name());
                //intent.putExtra(select_page.FRUIT_IMAGE_ID, map.getImageId());
                /*intent.putExtra(MainInterface.name, map.getM_name());
                intent.putExtra(MainInterface.BBox, map.getM_BBox());
                intent.putExtra(MainInterface.WKT, map.getM_WKT());
                intent.putExtra(MainInterface.uri, map.getM_uri());
                intent.putExtra(MainInterface.GPTS, map.getM_GPTS());*/
                mContext.startActivity(intent);
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
}
