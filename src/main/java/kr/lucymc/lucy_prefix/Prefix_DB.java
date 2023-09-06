package kr.lucymc.lucy_prefix;

import org.json.simple.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static kr.lucymc.lucy_prefix.Lucy_Prefix.connection;

public class Prefix_DB {
    public static void PrefixInsert(UUID userid, String Prefix, int Count) {
        String sql = "insert into prefix (UserID, Prefix, Count) values ('" + userid +"','" + Prefix +"','" + Count +"');";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void PrefixUpdate(UUID userid, JSONArray Prefix, int Count) {
        String sql = "update prefix set Prefix='"+Prefix+"',Count='"+Count+"' where UserID='"+userid+"';";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSet PrefixSelect(UUID userid) {
        String sql = "select * from prefix where UserID='"+userid+"';";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isDataExists(String tableName, String columnName, String value) {
        boolean exists = false;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, value);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                exists = (count > 0);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return exists;
    }
}
