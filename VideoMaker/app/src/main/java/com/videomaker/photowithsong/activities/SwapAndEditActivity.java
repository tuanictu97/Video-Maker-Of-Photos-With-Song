package com.videomaker.photowithsong.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.videomaker.photowithsong.Ads;
import com.videomaker.photowithsong.R;
import com.videomaker.photowithsong.adapters.RecyclerListAdapter;
import com.videomaker.photowithsong.helper.OnStartDragListener;
import com.videomaker.photowithsong.helper.SimpleItemTouchHelperCallback;
import com.videomaker.photowithsong.objects.Image;
import com.videomaker.photowithsong.utils.AnimationTranslate;
import com.videomaker.photowithsong.utils.Constant;

import java.io.File;
import java.util.ArrayList;

public class SwapAndEditActivity extends AppCompatActivity implements View.OnClickListener, OnStartDragListener, RecyclerListAdapter.OnClickImageEdit {

    public static int CAMERA_PREVIEW_RESULT = 1;
    private ArrayList<Image> imageList;
    private ArrayList<String> paths;
    private RecyclerView recyclerView;
    private RecyclerListAdapter adapter;
    private ItemTouchHelper mItemTouchHelper;
    private TextView tvNext, tvtitle;
    private Image imageClick;
    private ImageView ivBack, ivNext;
    private int position;
    private RelativeLayout layoutAds;

    public static Bitmap getBitmapFromLocalPath(String path, int sampleSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_swap_and_edit);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        tvNext = (TextView) findViewById(R.id.tv_next);
        tvtitle = (TextView) findViewById(R.id.titleappbar);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(Constant.IMAGE);
        imageList = bundle.getParcelableArrayList(Constant.IMAGE);
        recyclerView = (RecyclerView) findViewById(R.id.rv_image);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new RecyclerListAdapter(this, this, imageList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        ivNext.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvNext.setOnClickListener(this);
        tvtitle.setOnClickListener(this);
        layoutAds = (RelativeLayout) findViewById(R.id.layout_ads);
        Ads.b(this, layoutAds, new Ads.OnAdsListener() {
            @Override
            public void onError() {
                layoutAds.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                layoutAds.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdOpened() {
                layoutAds.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
        imageList = adapter.getmItems();
    }

    @Override
    public void onClickImageEdit(int position) {
        imageList = adapter.getmItems();
        Uri imageUri = Uri.fromFile(new File(imageList.get(position).getPath()));
    /* 2) Create a new Intent */
        Intent imageEditorIntent = new AdobeImageIntent.Builder(this)
                .setData(imageUri)
                .build();
        this.position = position;
    /* 3) Start the Image Editor with request code 1 */
        startActivityForResult(imageEditorIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                /* 4) Make a case for the request code we passed to startActivityForResult() */
                case 1:

                    /* 5) Show the image! */
                    Uri editedImageUri = data.getParcelableExtra(AdobeImageIntent.EXTRA_OUTPUT_URI);
                    imageList.get(position).setPath(getRealPathFromURI(SwapAndEditActivity.this, editedImageUri));
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.titleappbar:
            case R.id.iv_back: {
                finish();
                AnimationTranslate.previewAnimation(SwapAndEditActivity.this);
                break;
            }
            case R.id.iv_next:
            case R.id.tv_next: {
                paths = new ArrayList<String>();
                for (int i = 0; i < imageList.size(); i++) {
                    paths.add(imageList.get(i).getPath());
                }
                if (paths.size()!=0){
                    Intent data = new Intent(SwapAndEditActivity.this, SlideShowVideoActivity.class);
                    data.putExtra(Constant.IMAGE_ARR, paths);
                    startActivity(data);
                    AnimationTranslate.nextAnimation(SwapAndEditActivity.this);
                }else {
                    Toast.makeText(this,getResources().getString(R.string.nullimg), Toast.LENGTH_SHORT).show();
                }

                break;
            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationTranslate.previewAnimation(SwapAndEditActivity.this);
    }
}
