package com.geopdfviewer.android;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FestivalAdapter extends RecyclerView.Adapter<FestivalAdapter.ViewHolder>{
    private Context mContext;

    private List<String> list;
    private List<KeyAndValue> keyAndValues = null;

    private FestivalAdapter.OnRecyclerItemLongListener mOnItemLong;

    private FestivalAdapter.OnRecyclerItemClickListener mOnItemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        TextView FileName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            FileName = (TextView) view.findViewById(R.id.txt_festivallname);



        }
    }
    public FestivalAdapter(List<String> mFileList) {
        this.list = mFileList;
    }
    public FestivalAdapter(List<String> mFileList, List<KeyAndValue> keyAndValues) {
        this.list = mFileList;
        this.keyAndValues = keyAndValues;
    }

    @Override
    public FestivalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.festival_item, parent, false);
        final FestivalAdapter.ViewHolder holder = new FestivalAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                String name = list.get(position);
                mOnItemClick.onItemClick(v, name, position);
                /*Intent intent = new Intent(mContext, singlepoi.class);
                intent.putExtra("POIC", poi.getM_POIC());
                mContext.startActivity(intent);*/
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLong != null){
                    int position = holder.getAdapterPosition();
                    String name = list.get(position);
                    mOnItemLong.onItemLongClick(v, name);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(FestivalAdapter.ViewHolder holder, int position) {
        if (keyAndValues == null) {
            String keyAndValue = list.get(position);
            holder.FileName.setText(keyAndValue);
            holder.FileName.setTextColor(Color.BLACK);
        }
        else{
            String keyAndValue = list.get(position);
            holder.FileName.setText(keyAndValue);
            holder.FileName.setTextColor(Color.BLACK);
            int color = 0;
            switch (keyAndValues.get(position).getValue()){
                case "0":
                    color = Color.RED;
                    break;
                case "1":
                    color = Color.YELLOW;
                    break;
                case "2":
                    color = Color.GREEN;
                    break;
                case "3":
                    color = Color.LTGRAY;
                    break;
                case "4":
                    color = Color.BLUE;
                    break;
                case "5":
                    color = Color.WHITE;
                    break;
                case "6":
                    color = Color.rgb(255,255,0);
                    break;
                case "7":
                    color = Color.rgb(255,0,255);
                    break;
            }
            holder.cardView.setCardBackgroundColor(color);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view, String ObjectName);
    }
    public void setOnItemLongClickListener(FestivalAdapter.OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
    public interface OnRecyclerItemClickListener{
        void onItemClick(View view, String Name, int position);
    }
    public void setOnItemClickListener(OnRecyclerItemClickListener listener){
        this.mOnItemClick =  listener;
    }
}
