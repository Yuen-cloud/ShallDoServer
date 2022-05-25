package dao;

import data.Data;
import db.Db;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeneralDao {
    public void showDebug(String msg) {
        System.out.println("[" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "][device/dao/Db]" + msg);
    }

    private void updateRecord(Data data, JSONObject json) throws JSONException, SQLException {
        /*--------------------获取变量 开始--------------------*/
        JSONObject param = data.getParam();
        int resultCode = 0;
        String resultMsg = "ok";
        /*--------------------获取变量 完毕--------------------*/
        /*--------------------数据操作 开始--------------------*/
        Db updateDb = new Db("shalldo");
        String sql = data.getParam().getString("sql");
        showDebug("[updateRecord]" + sql);
        updateDb.executeUpdate(sql);
        updateDb.close();
        /*--------------------数据操作 结束--------------------*/
        /*--------------------返回数据 开始--------------------*/
        json.put("result_msg", resultMsg);                                                            //如果发生错误就设置成"error"等
        json.put("result_code", resultCode);                                                        //返回0表示正常，不等于0就表示有错误产生，错误代码
        /*--------------------返回数据 结束--------------------*/
    }

    private void queryRecord(Data data, JSONObject json) throws JSONException, SQLException {
        /*--------------------获取变量 开始--------------------*/
        String resultMsg = "ok";
        int resultCode = 0;
        List jsonList = new ArrayList();
        List jsonName = new ArrayList();
        /*--------------------获取变量 完毕--------------------*/
        /*--------------------数据操作 开始--------------------*/
        Db queryDb = new Db("shalldo");
        String sql = data.getParam().getString("sql");
        showDebug("[queryRecord]构造的SQL语句是：" + sql);
        try {
            ResultSet rs = queryDb.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int fieldCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map map = new LinkedHashMap();
                for (int i = 0; i < fieldCount; i++) {
                    map.put(rsmd.getColumnName(i + 1), rs.getString(rsmd.getColumnName(i + 1)));
                }
                jsonList.add(map);
            }
            rs.close();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                String columnLabel = rsmd.getColumnLabel(i + 1);
                jsonName.add(columnLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showDebug("[queryRecord]查询数据库出现错误：" + sql);
            resultCode = 10;
            resultMsg = "查询数据库出现错误！" + e.getMessage();
        }
        queryDb.close();
        /*--------------------数据操作 结束--------------------*/
        /*--------------------返回数据 开始--------------------*/
        json.put("aaData", jsonList);
        json.put("aaFieldName", jsonName);
        json.put("result_msg", resultMsg);                                                            //如果发生错误就设置成"error"等
        json.put("result_code", resultCode);                                                        //返回0表示正常，不等于0就表示有错误产生，错误代码
        /*--------------------返回数据 结束--------------------*/
    }

    public void login(Data data, JSONObject json) throws IOException, JSONException {
        String resultMsg = "ok";
        int resultCode = 0;
        JSONObject jsonObject = new JSONObject();;
        String username = data.getParam().getString("username");
        String password = data.getParam().getString("password");
        Db db = new Db("shalldo");
        String sql = "select * from account where username='" + username + "' and password='" + password + "'";
        showDebug("[login]构造的SQL语句是：" + sql);
        try {
            ResultSet rs = db.executeQuery(sql);
            int i = 0;
            while (rs.next()) {
                jsonObject.put("userId", rs.getInt("userId"));
                jsonObject.put("username", rs.getString("username"));
                jsonObject.put("password", rs.getString("password"));
                resultCode = 0;
                resultMsg = "登录成功";
                i++;
            }
            if (i == 0) {
                resultCode = 1;
                resultMsg = "用户名或密码错误";
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            resultCode = 10;
            resultMsg = "查询数据库错误!" + e.getMessage();
        }
        db.close();
        showDebug(resultCode + " " + resultMsg);
        json.put("resultCode", resultCode);
        json.put("resultMsg", resultMsg);
        json.put("account", jsonObject);
    }

    public void register(Data data, JSONObject json) throws JSONException, SQLException {
        String resultMsg = "ok";
        int resultCode = 0;
        String userName = data.getParam().getString("username");
        String password = data.getParam().getString("password");
        Db db = new Db("shalldo");
        String sql = "select * from account where username='" + userName + "'";
        showDebug("[register]构造的SQL语句是：" + sql);
        try {
            ResultSet rs = db.executeQuery(sql);
            if (rs.next()) {
                resultCode = 1;
                resultMsg = "用户名已存在";
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            resultCode = 10;
            resultMsg = "查询数据库失败!" + e.getMessage();
        }
        if (userName != null && password != null && resultCode == 0) {
            sql = "insert into account(username,password)";
            sql = sql + " values('" + userName + "'";
            sql = sql + ", '" + password + "')";
            data.getParam().put("sql", sql);
            updateRecord(data, json);
            resultMsg = "注册成功";
        }
        db.close();
        json.put("resultMsg", resultMsg);
        json.put("resultCode", resultCode);
    }
}
