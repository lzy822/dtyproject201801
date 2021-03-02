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

public class XZQTreeAdapter extends RecyclerView.Adapter<XZQTreeAdapter.ViewHolder>{
    private Context mContext;

    private List<XZQTree> xzqTreeList;

    private XZQTreeAdapter.OnRecyclerItemLongListener mOnItemLong;

    private XZQTreeAdapter.OnRecyclerItemClickListener mOnItemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        TextView XZQName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            XZQName = (TextView) view.findViewById(R.id.xzqtree_txt);



        }
    }
    public XZQTreeAdapter(List<XZQTree> xzqTreeList) {
        this.xzqTreeList = xzqTreeList;
    }

    @Override
    public XZQTreeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.xzqtree_item, parent, false);
        final XZQTreeAdapter.ViewHolder holder = new XZQTreeAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                XZQTree xzqTree = xzqTreeList.get(position);
                mOnItemClick.onItemClick(v, xzqTree.getXZQName(), position, xzqTree.getXZQNum());
                /*MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(mtapeobj.getM_path()));
                mediaPlayer.start();*/
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLong != null){
                    int position = holder.getAdapterPosition();
                    XZQTree xzqTree = xzqTreeList.get(position);
                    mOnItemLong.onItemLongClick(v, xzqTree.getXZQName());
                    holder.cardView.setCardBackgroundColor(Color.GRAY);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(XZQTreeAdapter.ViewHolder holder, int position) {
        XZQTree xzqTree = xzqTreeList.get(position);
        try {
            if (xzqTree.getXZQNum() == 5){
                holder.cardView.setCardBackgroundColor(Color.GRAY);
            }
            else
                holder.cardView.setCardBackgroundColor(Color.WHITE);

            switch (xzqTree.getXZQNum())
            {
                case 0:
                    holder.XZQName.setText(xzqTree.getXZQName());
                    break;
                case 1:
                    holder.XZQName.setText("-" + xzqTree.getXZQName());
                    break;
                case 2:
                    holder.XZQName.setText("--" + xzqTree.getXZQName());
                    break;
                case 3:
                    holder.XZQName.setText("---" + xzqTree.getXZQName());
                    break;
                case 4:
                    holder.XZQName.setText("----" + xzqTree.getXZQName());
                    break;
                case 5:
                    holder.XZQName.setText("----" + xzqTree.getXZQName());
                    break;
            }
        }
        catch (Exception e){
            Log.w(TAG, "出现意外错误，错误如下: " + e.toString());
        }
    }


    @Override
    public int getItemCount() {
        return xzqTreeList.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,String path);
    }
    public void setOnItemLongClickListener(XZQTreeAdapter.OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
    public interface OnRecyclerItemClickListener{
        void onItemClick(View view, String XZQName, int position, int XZQNum);
    }
    public void setOnItemClickListener(OnRecyclerItemClickListener listener){
        this.mOnItemClick =  listener;
    }
}

