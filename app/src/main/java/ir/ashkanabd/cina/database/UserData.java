package ir.ashkanabd.cina.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.NonNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class UserData implements Serializable {

    private String objectId;
    private Date created;
    private Date updated;

    private String phone_id;
    private String purchase_date;
    private String expire_days;

    public UserData() {

    }

    public UserData(String deviceID, Date purchaseData, String expireDays) {
        this.phone_id = deviceID;
        this.purchase_date = getString(purchaseData);
        this.expire_days = expireDays;
    }

    public UserData(String deviceID, String purchaseData, String expireDays) {
        this.phone_id = deviceID;
        this.purchase_date = purchaseData;
        this.expire_days = expireDays;
    }

    public void setPhone_id(String phone_id) {
        this.phone_id = phone_id;
    }

    public void setPurchase_date(String purchase_date) {
        this.purchase_date = purchase_date;
    }

    public void setExpire_days(String expire_days) {
        this.expire_days = expire_days;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getPhone_id() {
        return phone_id;
    }

    public String getPurchase_date() {
        return purchase_date;
    }

    public String getExpire_days() {
        return expire_days;
    }

    public JSONObject toJson() {
        HashMap<String, String> jsonMap = new HashMap<>();
        jsonMap.put("id", objectId);
        jsonMap.put("phone_id", phone_id);
        jsonMap.put("expire_days", expire_days);
        jsonMap.put("purchase_date", purchase_date);
        JSONObject jsonObj = new JSONObject(jsonMap);
        return jsonObj;
    }

    @NonNull
    @Override
    public String toString() {
        return toJson().toString();
    }

    /*
     * File info must be encrypted.
     */
    public static UserData readFile(File file) throws Exception {
        UserData userData = new UserData();
        StringBuilder builder = new StringBuilder();
        Scanner scn = new Scanner(file);
        while (scn.hasNextLine()) {
            builder.append(scn.nextLine());
        }
        scn.close();
        JSONObject jsonObj = new JSONObject(builder.toString());
        userData.setObjectId(jsonObj.getString("id"));
        userData.setPhone_id(jsonObj.getString("phone_id"));
        userData.setExpire_days(jsonObj.getString("expire_days"));
        userData.setPurchase_date(jsonObj.getString("purchase_date"));
        return userData;
    }

    /*
     * Given user data must be encrypted.
     */
    public static void writeFile(File file, UserData userData) throws IOException {
        PrintWriter pw = new PrintWriter(file);
        pw.println(userData.toString());
        pw.flush();
        pw.close();
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Date getDate(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTime();
    }

    public static Date getDate(String date) {
        String[] strings = date.split("-");
        return getDate(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
    }

    public static UserData encryptData(UserData userData) {
        UserData ud = new UserData();
        ud.setPhone_id(Encryption.encrypt(userData.getPhone_id()));
        ud.setExpire_days(Encryption.encrypt(userData.getExpire_days()));
        ud.setPurchase_date(Encryption.encrypt(userData.getPurchase_date()));
        return ud;
    }

    public static UserData decryptData(UserData userData) {
        UserData ud = new UserData();
        ud.setPhone_id(Encryption.decrypt(userData.getPhone_id()));
        ud.setExpire_days(Encryption.decrypt(userData.getExpire_days()));
        ud.setPurchase_date(Encryption.decrypt(userData.getPurchase_date()));
        return ud;
    }
}
