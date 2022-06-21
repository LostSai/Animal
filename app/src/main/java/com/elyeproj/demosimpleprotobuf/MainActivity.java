package com.elyeproj.demosimpleprotobuf;

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
import androidx.constraintlayout.solver.widgets.Snapshot;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.protobuf.InvalidProtocolBufferException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import tutorial.AnimalOuterClass.Animal;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener{
    private ListView list_food;

    private MyAdapter<Animal> myAdapter = null;
    private List<Animal> mDataFood = null;
    //private AnimalService animalService;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //animalService = new AnimalService(this);
        if (mDataFood == null){
            init();
            //refreshSize();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this, AddAnimalActivity.class);
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
                        .setMessage("即将删除所有食品数据")
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

    private void init() {
        Log.i("animal", "init: ");
        list_food = (ListView)findViewById(R.id.list_food);
        list_food.setOnItemLongClickListener(this);
        mDataFood = new ArrayList<Animal>();
//        if (animalService.getObjList().size() > 0){
//            mDataFood = animalService.getObjList();
//        }
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

    private void createPerson(){
//                Person john = Person.newBuilder()
//                .setName("john").setEmail("abc@123.com")
//                .addPhones(Addressbook.Person.PhoneNumber.newBuilder()
//                        .setNumber("911").setType(Addressbook.Person.PhoneType.HOME))
//                .build();
//
//        System.out.println("name=" + john.getName() + ",email=" + john.getEmail()
//                + ",phone=" + john.getPhonesCount());
//
//        for (Person.PhoneNumber phoneNumber : john.getPhonesList()) {
//            switch (phoneNumber.getType()) {
//                case MOBILE:
//                    System.out.print("  Mobile phone #: ");
//                    break;
//                case HOME:
//                    System.out.print("  Home phone #: ");
//                    break;
//                case WORK:
//                    System.out.print("  Work phone #: ");
//                    break;
//            }
//            System.out.println(phoneNumber.getNumber());
//        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("animalService", "onItemClick: position=" + position + ",id=" + id);
        parent.getAdapter().getItem(position);
        TextView tv = view.findViewById(R.id.txt_fId);
        if (tv == null){
            return false;
        }

        Log.i("animalService", "onItemClick: ");

        //animalService.deleteObj(Integer.valueOf(tv.getText().toString()));
        myAdapter.remove(position);
        list_food.setAdapter(myAdapter);

        //refreshSize();
        return true;
    }

    private void addFood(Intent data) throws InvalidProtocolBufferException {
        byte[] bytes = (byte[]) data.getSerializableExtra("animal");
        Animal f = Animal.parseFrom(bytes);
        if (f != null){
            myAdapter.add(f);
            //animalService.saveObj(f);
            //refreshSize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case Activity.RESULT_OK:
            {
                try {
                    addFood(data);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
