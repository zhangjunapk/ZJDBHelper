package org.zj.demo;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class test {
    static String USERNAME="root";
    static String PASSWORD="zhangjun249";
    static String URL="jdbc:mysql://localhost:3306/jdbc_t";
    public static void main(String[] args) throws Exception {
        inflateDate();
    }

    public static void testMyUtil(){
        try {
            Class.forName("org.zj.demo.ZJDBHelper");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HashMap<String,String> fieldMap=new HashMap<>();
        fieldMap.put("id","110");
        fieldMap.put("content","afteer");

        HashMap<String,String> whereMap=new HashMap<>();
        whereMap.put("id","1");

        ZJDBHelper.update("test",fieldMap,whereMap);

    }


public static void inflateDate() throws IllegalAccessException, SQLException, InstantiationException {
        String sql="select * from test";
    System.out.println(ZJDBHelper.query(bean.class,sql).toString());
}

    public static void testJDBC() throws Exception {

        Properties properties=new Properties();
        properties.load(test.class.getClassLoader().getResourceAsStream("kk"));

        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(URL, USERNAME,"");
        PreparedStatement preparedStatement = connection.prepareStatement("select * from test");

        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            System.out.println(resultSet.getString("content"));
            System.out.println(resultSet.getInt("id"));
        }
        resultSet.close();
        preparedStatement.clearParameters();
        connection.close();
    }

}
