package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.List;

/**
 * Created by 54286 on 2018/3/6.
 */

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {
    private Context mContext;

    private List<Map> mFruitList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        PDFView MapImage;
        TextView MapName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            MapImage = (PDFView) view.findViewById(R.id.map_item);
            MapName = (TextView) view.findViewById(R.id.map_name);

        }
    }
    public MapAdapter(List<Map> fruitList) {
        mFruitList = fruitList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.map_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Map map = mFruitList.get(position);
                Intent intent = new Intent(mContext, select_page.class);
                intent.putExtra(select_page.FRUIT_NAME, map.getName());
                intent.putExtra(select_page.FRUIT_IMAGE_ID, map.getImageId());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map map = mFruitList.get(position);
        holder.MapName.setText(map.getName());
    holder.MapImage.fromUri(map.getImageId());
        //Glide.with(mContext).load(fruit.getImageId()).into(holder.fruitImage);
    }


    @Override
    public int getItemCount() {
        return mFruitList.size();
    }
}
