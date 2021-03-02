package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.geopdfviewer.android.JZActivity.TAG;

/**
 * Created by 54286 on 2018/3/20.
 */

public class OtherTypeMapAdapter extends RecyclerView.Adapter<OtherTypeMapAdapter.ViewHolder>{
    private Context mContext;

    private List<String> list;

    private OtherTypeMapAdapter.OnRecyclerItemLongListener mOnItemLong;

    private OtherTypeMapAdapter.OnRecyclerItemClickListener mOnItemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        TextView MapName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            MapName = (TextView) view.findViewById(R.id.xzqtree_txt);



        }
    }
    public OtherTypeMapAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public OtherTypeMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.xzqtree_item, parent, false);
        final OtherTypeMapAdapter.ViewHolder holder = new OtherTypeMapAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                String MapName = list.get(position);
                mOnItemClick.onItemClick(v, MapName, position);
                /*MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(mtapeobj.getM_path()));
                mediaPlayer.start();*/
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLong != null){
                    int position = holder.getAdapterPosition();
                    String MapName = list.get(position);
                    mOnItemLong.onItemLongClick(v, MapName);
                    holder.cardView.setCardBackgroundColor(Color.GRAY);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(OtherTypeMapAdapter.ViewHolder holder, int position) {
        String MapName = list.get(position);
        try {
            if (MapName.equals("社会经济图组") || MapName.equals("资源与环境图组")){
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
            else{
                holder.cardView.setCardBackgroundColor(Color.GRAY);
            }
            holder.MapName.setText(MapName);
        }
        catch (Exception e){
            Log.w(TAG, "出现意外错误，错误如下: " + e.toString());
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,String path);
    }
    public void setOnItemLongClickListener(OtherTypeMapAdapter.OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
    public interface OnRecyclerItemClickListener{
        void onItemClick(View view, String MapName, int position);
    }
    public void setOnItemClickListener(OnRecyclerItemClickListener listener){
        this.mOnItemClick =  listener;
    }
}

