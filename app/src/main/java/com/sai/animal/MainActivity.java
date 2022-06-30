package com.sai.animal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import tutorial.AnimalOuterClass.Animal;

import static com.sai.animal.DbUtil.LAST_INDEX;
import static com.sai.animal.DbUtil.charset;
import static com.sai.animal.DbUtil.initDB;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener{
    private ListView list_food;
    private MyAdapter<Animal> myAdapter = null;
    private List<Animal> mDataFood = null;
    private ProgressDialog pd;
    private String TAG = "Animal_Flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        String path = "/data/data/" + this.getPackageName();
        try {
            initDB(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //animalService = new AnimalService(this);
        if (mDataFood == null){
            try {
                init();
                refreshSize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAnimalActivity.class);
                //intent.putExtra("index", animalService.getLastDdIndex() + 1);
                //启动
                startActivityForResult(intent, 0);
            }
        });

        FloatingActionButton fabClean = findViewById(R.id.clean);
        fabClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示确认对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alert = builder.setTitle("删除提示：")
                        .setMessage("即将删除所有数据")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "你点击了取消按钮~", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "你点击了确定按钮~", Toast.LENGTH_SHORT).show();
                                pdAutoClose();
                            }
                        }).create();             //创建AlertDialog对象
                alert.show();                    //显示对话框
            }
        });
    }

    // 进度条自动关闭后删除数据
    private void pdAutoClose(){
        Log.i(TAG, "pdAutoClose: ");
        pd = ProgressDialog.show(MainActivity.this, "数据删除中", "删除中。。。。");
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //删除全部
                //animalService.cleanAllObjs();
                myAdapter.clear();
            }
        });
        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pd.dismiss();
            }
        };
        thread.start();
    }

    private void init() throws IOException {
        Log.i(TAG, "init: ");
        list_food = (ListView)findViewById(R.id.list_food);
        list_food.setOnItemLongClickListener(this);
        mDataFood = new ArrayList<Animal>();
        if (AnimalUtil.getAnimalList().size() > 0){
            mDataFood = AnimalUtil.getAnimalList();
        }
        myAdapter = new MyAdapter<Animal>((ArrayList) mDataFood, R.layout.item_three) {
            @Override
            public void bindView(ViewHolder holder, Animal obj) {
                Date curDate = new Date(obj.getCurTime());
                SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
                String retStrFormatNowDate = sdFormatter.format(curDate);
                holder.setImageResource(R.id.img_icon,obj.getIcon());
                holder.setText(R.id.txt_fname, obj.getName());
                holder.setText(R.id.txt_fDate, retStrFormatNowDate);
                holder.setText(R.id.txt_fNum, "" + obj.getCount());
                holder.setText(R.id.txt_fId, "" + obj.getId());
            }
        };

        //ListView设置下Adapter：
        list_food.setAdapter(myAdapter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
        Log.i(TAG, "onItemLongClick: position=" + position + ",id=" + id);
        // 显示确认对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alert = builder.setTitle("删除提示：")
                .setMessage("是否删除该条数据？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "你点击了取消按钮~", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "你点击了确定按钮~", Toast.LENGTH_SHORT).show();
                        deleteAnimal(view, position);
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
        return true;
    }

    private void deleteAnimal(View view, int position){
        Log.i(TAG, "deleteAnimal: position=" + position);
        TextView tv = view.findViewById(R.id.txt_fId);
        if (tv == null){
            return;
        }

        String key = tv.getText().toString();
        DbUtil.delete(key);

        myAdapter.remove(position);
        list_food.setAdapter(myAdapter);
        refreshSize();
    }

    private void addAnimal(Intent data) throws InvalidProtocolBufferException {
        byte[] bytes = (byte[]) data.getSerializableExtra("animal");
        Log.i(TAG, "addAnimal btyes=" + bytes);
        Animal f = Animal.parseFrom(bytes);
        if (f != null){
            myAdapter.add(f);
            int sIndex = DbUtil.getNextIndex();
            DbUtil.writeDB("" + sIndex, bytes);
            DbUtil.writeDB(LAST_INDEX, ("" + sIndex).getBytes(charset));
            refreshSize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case Activity.RESULT_OK:
            {
                try {
                    addAnimal(data);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    private void refreshSize(){
        TextView tvSize = (TextView)findViewById(R.id.txt_fSize);
        if (tvSize != null){
            int size = 0;
            if (mDataFood != null){
                size = mDataFood.size();
            }
            tvSize.setText("" + size);
        }
    }
}
