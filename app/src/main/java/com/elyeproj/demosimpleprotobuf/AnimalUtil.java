package com.elyeproj.demosimpleprotobuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tutorial.AnimalOuterClass.Animal;
import tutorial.AnimalOuterClass.Animal.AnimalType;

public class AnimalUtil {

    private static List<Animal> mDataAnimal = null;

    /**
     *随机一个食品类型
     * @return
     */
    public static AnimalType random() {
        int pick = new Random().nextInt(AnimalType.values().length);
        return AnimalType.values()[pick];
    }

    /**
     * 获得食品名称
     * @param animalType
     * @return
     */
    public static String getName(AnimalType animalType) {
        String sName = "";
        switch (animalType) {
            case PIG:
                sName = "猪";
                break;
            case BULL:
                sName = "牛";
                break;
            case SHEEP:
                sName = "羊";
                break;
            default:
                break;
        }
        return sName;
    }

    /**
     * 获得食品icon
     * @param animalType
     * @return
     */
    public static int getAnimalIcon(AnimalType animalType) {
        int icon = R.mipmap.fu_1;
        switch (animalType) {
            case PIG:
                icon = R.mipmap.fu_1;
                break;
            case BULL:
                icon = R.mipmap.fu_2;
                break;
            case SHEEP:
                icon = R.mipmap.fu_3;
                break;
            default:
                break;
        }

        return icon;
    }

    /**
     * 获得食品类型列表
     * @return
     */
    public static List<Animal> getAnimalTypeList(){
        if (mDataAnimal == null){
            mDataAnimal = new ArrayList<Animal>();
            for (AnimalType type : AnimalType.values()){
                if (type == AnimalType.UNRECOGNIZED)
                {
                    continue;
                }
                mDataAnimal.add(createAnimal(type));
            }
        }
        return mDataAnimal;
    }

    private static Animal createAnimal(AnimalType type){
        String name = getName(type);
        int icon = getAnimalIcon(type);
        Animal animal = Animal.newBuilder().setName(name).setType(type).setIcon(icon).build();
        return animal;
    }

    /**
     * 通过类型名称获得枚举
     * @param name
     * @return
     */
    public static AnimalType getAnimalType(String name){
        return getEnumFormString(AnimalType.class, name);
    }

    private static <T extends Enum<T>> T getEnumFormString(Class<T> c, String name){
        if (c != null && name != null){
            try {
                return Enum.valueOf(c,name.trim().toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
