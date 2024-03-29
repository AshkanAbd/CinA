package ir.ashkanabd.cina.database;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.DataQueryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Connection implements Serializable {
    private Boolean validUser;
    private Boolean needNetwork;
    private UserData userData;

    public Connection() {
        this.validUser = false;
        this.needNetwork = false;
        this.userData = null;
    }

    /*
     * Connect to database and check user.
     */
    public void connectDatabase(Context context) {
        String userFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/.user.info";
        File userFile = new File(userFilePath);
        Backendless.setUrl(Encryption.decrypt(context, DataBaseDefaults.SERVER_URL));
        Backendless.initApp(context.getApplicationContext(), Encryption.decrypt(context, DataBaseDefaults.APPLICATION_ID)
                , Encryption.decrypt(context, DataBaseDefaults.API_KEY));
        Backendless.Data.mapTableToClass("UserData", UserData.class);
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        try {
            /*
             * Read user data from storage
             */
            UserData userData = UserData.readFile(userFile);
            this.userData = UserData.decryptData(userData);
            this.validUser = isValidUser(userData);
            Log.e("CinA", "User info read from storage");
        } catch (Exception e) {
            try {
                /*
                 * Storage problem, check server
                 */
                if (!userFile.exists()) {
                    userFile.createNewFile();
                }
                List<UserData> userDataList = Backendless.Data.of(UserData.class).find(queryBuilder);
                if (userDataList.size() != 0) {
                    /*
                     * Revive user data from server
                     */
                    UserData userData = userDataList.get(0);
                    userData.encrypted = true;
                    UserData.writeFile(userFile, userData);
                    this.userData = UserData.decryptData(userData);
                    this.validUser = isValidUser(userData);
                    Log.e("CinA", "User info read from server");
                } else {
                    /*
                     * Create new user in server
                     */
                    UserData userData = new UserData(UserData.getDeviceID(context)
                            , UserData.getString(Calendar.getInstance().getTime()), "7");
                    userData = Backendless.Data.of(UserData.class).save(UserData.encryptData(userData));
                    userData.encrypted = true;
                    UserData.writeFile(userFile, userData);
                    this.userData = UserData.decryptData(userData);
                    this.validUser = true;
                    Log.e("CinA", "New user");
                }
            } catch (BackendlessException be) {
                /*
                 * Network error.
                 */
                needNetwork = true;
                Log.e("CinA", "Unknown user");
            } catch (IOException ioe) {
                /*
                 * Storage error
                 */
                needNetwork = true;
                Log.e("CinA", "Unknown user");
                Log.e("CinA", e.toString());
            }
        }
//        queryBuilder.setWhereClause("phone_id = '" + Encryption.encrypt(context, UserData.getDeviceID(context)) + "'");
//        try {
//            if (!userFile.exists()) {
//                userFile.createNewFile();
//            }
//            List<UserData> userDataList = Backendless.Data.of(UserData.class).find(queryBuilder);
//            if (userDataList.size() != 0) {
//                /*
//                 * Revive user data from server
//                 */
//                UserData userData = userDataList.get(0);
//                userData.encrypted = true;
//                UserData.writeFile(userFile, userData);
//                this.userData = UserData.decryptData(userData);
//                this.validUser = isValidUser(userData);
//                Log.e("CinA", "User info read from server");
//            } else {
//                /*
//                 * Create new user in server
//                 */
//                UserData userData = new UserData(UserData.getDeviceID(context)
//                        , UserData.getString(Calendar.getInstance().getTime()), "7");
//                userData = Backendless.Data.of(UserData.class).save(UserData.encryptData(userData));
//                userData.encrypted = true;
//                UserData.writeFile(userFile, userData);
//                this.userData = UserData.decryptData(userData);
//                this.validUser = true;
//                Log.e("CinA", "New user");
//            }
//        } catch (BackendlessException e) {
//            /*
//             * Network problem, check storage
//             */
//            try {
//                /*
//                 * Read user data from storage
//                 */
//                UserData userData = UserData.readFile(userFile);
//                this.userData = UserData.decryptData(userData);
//                this.validUser = isValidUser(userData);
//                Log.e("CinA", "User info read from storage");
//            } catch (Exception e1) {
//                /*
//                 * No user data in storage
//                 */
//                needNetwork = true;
//                Log.e("CinA", "Unknown user");
//            }
//        } catch (IOException e) {
//            needNetwork = true;
//            Log.e("CinA", "Unknown user");
//            Log.e("CinA", e.toString());
//        }
    }

    public boolean updateDataBase(Context context, UserData userData) {
        String userFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/.user.info";
        File userFile = new File(userFilePath);
        Backendless.setUrl(Encryption.decrypt(context, DataBaseDefaults.SERVER_URL));
        Backendless.initApp(context.getApplicationContext(), Encryption.decrypt(context, DataBaseDefaults.APPLICATION_ID)
                , Encryption.decrypt(context, DataBaseDefaults.API_KEY));
        Backendless.Data.mapTableToClass("UserData", UserData.class);
        if (!userData.encrypted)
            userData = UserData.encryptData(userData);
        try {
            UserData ud = Backendless.Data.of(UserData.class).save(userData);
            ud.encrypted = true;
            UserData.writeFile(userFile, ud);
            return true;
        } catch (BackendlessException | IOException e) {
            return false;
        }
    }

    /*
     * Check user is valid and can use app or not.
     */
    private boolean isValidUser(UserData userData) {
        if (userData.encrypted)
            userData = UserData.decryptData(userData);
        Date startDate = UserData.getDate(userData.getPurchase_date());
        int days = Integer.parseInt(userData.getExpire_days());
        Calendar expireTime = Calendar.getInstance();
        expireTime.setTime(startDate);
        expireTime.add(Calendar.DAY_OF_MONTH, days);
        Calendar now = Calendar.getInstance();
        return now.before(expireTime);
    }

    public Date getExpireTime(UserData userData) {
        if (userData.encrypted)
            userData = UserData.decryptData(userData);
        Date startDate = UserData.getDate(userData.getPurchase_date());
        int days = Integer.parseInt(userData.getExpire_days());
        Calendar expireTime = Calendar.getInstance();
        expireTime.setTime(startDate);
        expireTime.add(Calendar.DAY_OF_MONTH, days);
        return expireTime.getTime();
    }

    public Boolean isValid() {
        return validUser;
    }

    public Boolean getNeedNetwork() {
        return needNetwork;
    }

    public UserData getUserData() {
        return userData;
    }
}
