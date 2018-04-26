package org.zj.demo;

import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterState;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ZJDBHelper {
    static Properties PROP;
    static Connection connection;
    static PreparedStatement preparedStatement;
    static{
        InputStream is=ZJDBHelper.class.getClassLoader().getResourceAsStream("db-config.properties");
       PROP=new Properties();
        try {
            PROP.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class.forName(PROP.getProperty("driver-name"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        connection=getConnection();
    }
    public static Connection getConnection(){
        String url=PROP.getProperty("url");
        String username=PROP.getProperty("username");
        String password=PROP.getProperty("password");
        String databaseName=PROP.getProperty("database-name");

        try {
            return DriverManager.getConnection(url+"/"+databaseName,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean close(ResultSet resultSet, PreparedStatement preparedStatement,Connection connection){
        try {
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 插入
     * @param tableName
     * @param fieldMap
     * @return
     */
    public static boolean insert(String tableName, HashMap<String,String> fieldMap){
        StringBuilder sb=new StringBuilder("insert into "+tableName);
        sb.append("(");
        for(Map.Entry<String,String> entry:fieldMap.entrySet()){
            sb.append(entry.getKey()+",");
        }
        sb.replace(sb.length()-1,sb.length(),")");

        sb.append(" values(");

        for(Map.Entry<String,String> entry:fieldMap.entrySet()){
            sb.append("'"+entry.getValue()+"',");
        }
        sb.replace(sb.length()-1,sb.length(),")");

        try {
            preparedStatement=connection.prepareStatement(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }



        System.out.println(sb.toString());

        try {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 删除
     * @param tableName
     * @param fieldMap
     * @return
     */
    public static boolean delete(String tableName, HashMap<String,String> fieldMap,HashMap<String,String> whereMap){
        StringBuilder sb=new StringBuilder("delete from "+tableName +" where ");

        for(Map.Entry<String,String> entry:fieldMap.entrySet()){
            sb.append(entry.getKey()+"='"+entry.getValue()+"' and ");
        }

        if(whereMap!=null && whereMap.size()!=0){
            sb.append(" where ");
            for(Map.Entry<String,String> entry:whereMap.entrySet()){
                sb.append(entry.getKey()+"="+entry.getValue()+" and");
            }
            System.out.println(sb.toString()+"-------->");
            sb=new StringBuilder(sb.substring(0,sb.length()-4));
        }

        System.out.println(sb.toString()+"<--------->");
        sb=new StringBuilder(sb.substring(0,sb.length()-4));
        System.out.println(sb.toString());
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 更新
     * @param tableName
     * @param fieldMap
     * @param whereMap
     * @return
     */
    public static boolean update(String tableName,HashMap<String,String> fieldMap,HashMap<String,String> whereMap){
        StringBuilder sb=new StringBuilder("update "+tableName +" set ");

        for(Map.Entry<String,String> entry:fieldMap.entrySet()){
            sb.append(entry.getKey()+"='"+entry.getValue()+"' , ");
        }
        System.out.println(sb.toString()+"<--------->");
        sb=new StringBuilder(sb.substring(0,sb.length()-3));

        if(whereMap !=null&&whereMap.size()!=0){
            sb.append(" where ");
            for(Map.Entry<String,String> entry:whereMap.entrySet()){
                sb.append(entry.getKey()+"="+entry.getValue()+" and");
            }
            System.out.println(sb.toString()+"-------->");
            sb=new StringBuilder(sb.substring(0,sb.length()-4));
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return true;
    }

    /**
     * 查询
     */
    public  static  <T> T query(Class c,String sql) throws IllegalAccessException, InstantiationException, SQLException {
        Class result= c;
        
        preparedStatement=connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            System.out.println("在表中查找到数据了");
            //通过反射调用类里面的方法,然后将值给result赋值进去
            int columnCount = resultSet.getMetaData().getColumnCount();
            System.out.println("表中一共有"+columnCount+"个字段");
            for(int i=1;i<=columnCount;i++){
                String fieldName=resultSet.getMetaData().getColumnName(i);
                //获得类的所有字段名字
                Field[] fields = result.getDeclaredFields();
                for(Field field:fields){
                    System.out.println("遍历类中的所有字段");
                    //如果找到了,就从表中获得数据并将数据通过调用方法填入class
                    System.out.println("类中的字段名:"+field.getName()+"表中的字段名:"+fieldName);
                    if(field.getName().equals(fieldName)){
                        System.out.println("找到一个字段的名字和表中的字段一样");
                        try {
                            Method method = result.getMethod("set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),field.getType());
                            //调用方法来填入数据
                            System.out.println("填入数据到类中");
                            inflateData(result,method,field,resultSet.getString(i));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }

        return (T) result;
    }

    //通过反射来调用指定类中的方法来填入数据
    private static void inflateData(Class result, Method method, Field field, String fieldValue) {
        //判断类中字段的类型,然后对表中的字段进行类型转换
        System.out.println(field.getName()+" -->字段的类型是<--"+field.getType().toString());
         if(field.getType().toString().contains("String")){
             try {
                 method.invoke(result,fieldValue);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
             }
         }
         if(field.getType().toString().contains("Integer")){
             try {
                 method.invoke(result,Integer.parseInt(fieldValue));
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
             }
         }
        //然后调用方法来填入数据

    }

}
