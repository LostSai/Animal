package com.sai.animal;


import android.util.Log;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.sai.animal.AnimalUtil.TAG;

public class DbUtil {
    public static DB db = null;
    private static boolean cleanup = false;
    public static Charset charset = Charset.forName("utf-8");
    public static String LAST_INDEX = "last_index";
    //private static String path = "/data/data/com.sai.iq80";

    public static void initDB(String path) throws IOException {
        Log.i(TAG, "initDB path=" + path);
        //init
        DBFactory factory = Iq80DBFactory.factory;
        File dir = new File(path);
        //如果数据不需要reload，则每次重启，尝试清理磁盘中path下的旧数据。
        if(cleanup) {
            factory.destroy(dir,null);//清除文件夹内的所有文件。
        }
        Options options = new Options().createIfMissing(true);
        //重新open新的db
        db = factory.open(dir,options);
        // 检查last_index是否存在
        String value = readDB("last_index");
        if ("".equals(value)){
            writeDB("last_index", "0".getBytes(charset));
        }
    }

    public static void writeDB(String key, byte[] value){
        Log.i(TAG, "writeDB key=" + key + ",value=" + value);
        //write后立即进行磁盘同步写
        WriteOptions writeOptions = new WriteOptions().sync(true);//线程安全
        db.put(key.getBytes(charset),value,writeOptions);
    }

    public void writeDbBatch() throws IOException {
        Log.i(TAG, "writeDbBatch: ");
        //batch write；
        WriteBatch writeBatch = db.createWriteBatch();
        writeBatch.put("key-03".getBytes(charset),"value-03".getBytes(charset));
        writeBatch.put("key-04".getBytes(charset),"value-04".getBytes(charset));
        writeBatch.delete("key-01".getBytes(charset));
        db.write(writeBatch);
        writeBatch.close();
    }

    public static String readDB(String key){
        Log.i(TAG, "readDB key=" + key);
        String value = "";
        //read
        byte[] bv = db.get(key.getBytes(charset));
        if(bv != null && bv.length > 0) {
            value = new String(bv,charset);
            Log.i(TAG, "readDB value=" + value);
        }

        return value;
    }

    public static int getNextIndex(){
        String sIndex = DbUtil.readDB(LAST_INDEX);
        return Integer.valueOf(sIndex) + 1;
    }

    private void readDBIterator() throws IOException {
        Log.i(TAG, "readDBIterator");
        //iterator，遍历，顺序读
        //读取当前snapshot，快照，读取期间数据的变更，不会反应出来
        Snapshot snapshot = db.getSnapshot();
        //读选项
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
        readOptions.snapshot(snapshot);//默认snapshot为当前。
        DBIterator iterator = db.iterator(readOptions);
        while (iterator.hasNext()) {
            Map.Entry<byte[],byte[]> item = iterator.next();
            String key = new String(item.getKey(),charset);
            String value = new String(item.getValue(),charset);//null,check.
            Log.i(TAG, "readDBIterator key=" + key + ":value=" + value);
        }
        iterator.close();//must be
    }

    public static void delete(String key){
        Log.i(TAG, "delete");
        //delete
        db.delete(key.getBytes(charset));
    }

    private void compactRange() throws IOException {
        Log.i(TAG, "compactRange: ");
        //compaction，手动
        db.compactRange("key-".getBytes(charset),null);
        db.close();
    }
}
