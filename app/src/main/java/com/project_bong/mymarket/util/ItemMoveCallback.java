package com.project_bong.mymarket.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.project_bong.mymarket.adapter.RegisterGoodsImageAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;
    private final String TAG = "ItemTouchHelper";

    public ItemMoveCallback(ItemTouchHelperContract adapter){
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        Log.d(TAG,"isLongPressDragEnabled 호출");
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG,"getMovementFlags 호출");
        int dragFlags;
        if(viewHolder instanceof RegisterGoodsImageAdapter.HeaderViewHolder){
            dragFlags = 0;
        }else{
            dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
        return makeMovementFlags(dragFlags,0);

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG,"onMove 호출");
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public interface ItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);

    }
}
