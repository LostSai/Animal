package com.elyeproj.demosimpleprotobuf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import tutorial.AnimalOuterClass.Animal;
import tutorial.AnimalOuterClass.Animal.AnimalType;

public class AddAnimalActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Spinner spin_animal;
    //判断是否为刚进去时触发onItemSelected的标志
    private boolean animal_selected = false;
    private MyAdapter<Animal> myAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txt_type = (TextView) findViewById(R.id.txt_type);
                AnimalType type = AnimalUtil.getAnimalType(txt_type.getText().toString());
                addAnimal(type);
            }
        });

        FloatingActionButton fabClose = findViewById(R.id.close);
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void bindViews(){
        spin_animal = (Spinner) findViewById(R.id.spin_animal);
        myAdapter = new MyAdapter<Animal>((ArrayList) AnimalUtil.getAnimalTypeList(), R.layout.item_spin_hero) {
            @Override
            public void bindView(ViewHolder holder, Animal obj) {
                holder.setImageResource(R.id.img_icon,obj.getIcon());
                holder.setText(R.id.txt_name, obj.getName());
                holder.setText(R.id.txt_type, obj.getType().toString());
            }
        };
        spin_animal.setAdapter(myAdapter);
        spin_animal.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.spin_animal:
                if(animal_selected){

                }
                else {
                    animal_selected = true;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void addAnimal(AnimalType type){
        int index = getIntent().getIntExtra("index", 1);
        String name = AnimalUtil.getName(type);
        int icon = AnimalUtil.getAnimalIcon(type);
        Animal f = Animal.newBuilder().setId(index).setName(name).setType(type)
                .setIcon(icon).setCount(new Random().nextInt(99)).build();
        Intent intent = new Intent();
        intent.putExtra("animal", f.toByteArray());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}