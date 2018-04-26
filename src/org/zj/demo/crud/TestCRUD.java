package org.zj.demo.crud;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestCRUD {

    /**
     * query
     * @throws Exception
     */
    @Test
    public void demo01() throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_t","root","zhangjun249");
        PreparedStatement preparedStatement = conn.prepareStatement("select * from test");
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            System.out.println(resultSet.getInt("id"));
            System.out.println(resultSet.getString("content"));
        }
        resultSet.close();
        preparedStatement.close();
        conn.close();
        System.out.println(preparedStatement.executeQuery());
    }
    @Test
    public void delete()throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_t","root","zhangjun249");
        PreparedStatement preparedStatement = conn.prepareStatement("delete form test where id=?");

        preparedStatement.setInt(1,2);
        preparedStatement.execute();
        preparedStatement.close();
        conn.close();
        System.out.println(preparedStatement.executeQuery());
    }

    @Test
    public void update()throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_t","root","zhangjun249");
        PreparedStatement preparedStatement = conn.prepareStatement("update from test set id=?,content=?");

        preparedStatement.setInt(1,1);
        preparedStatement.setString(2,"zz");
        preparedStatement.close();
        conn.close();
        System.out.println(preparedStatement.executeQuery());
    }


}
