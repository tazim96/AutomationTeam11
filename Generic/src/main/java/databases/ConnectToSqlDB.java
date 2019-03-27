package databases;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectToSqlDB {
    public static Connection connect = null;
    public static Statement statement = null;
    public static PreparedStatement ps = null;
    public static ResultSet resultSet = null;

    public static Properties loadProperties(String filepath) throws IOException {
        Properties prop = new Properties();
        InputStream ism = new FileInputStream(filepath);
        prop.load(ism);
        ism.close();
        return prop;
    }

    public static Connection connectToSqlDatabase(String filepath) throws IOException, SQLException, ClassNotFoundException {
        Properties prop = loadProperties(filepath);
        String driverClass = prop.getProperty("MYSQLJDBC.driver");
        String url = prop.getProperty("MYSQLJDBC.url");
        String userName = prop.getProperty("MYSQLJDBC.userName");
        String password = prop.getProperty("MYSQLJDBC.password");
        Class.forName(driverClass);
        connect = DriverManager.getConnection(url,userName,password);
        System.out.println("Database is connected");
        return connect;
    }

    public List<String> readDataBase(String tableName, String columnName, String propertiesFilePath) throws Exception {
        List<String> data = new ArrayList<String>();
        try {
            connectToSqlDatabase(propertiesFilePath);
            statement = connect.createStatement();
            resultSet = statement.executeQuery("select * from " + tableName);
            //data = getResultSetData(resultSet, columnName);
            while(resultSet.next()){
                String itemName = resultSet.getString(columnName);
                data.add(itemName);
            }
        } catch (ClassNotFoundException e) {
            throw e;
        } finally {
            close();
        }
        return data;
    }

    /**
     * This method assumes there is an existing table with two String columns(column1, column2) and returns
     * the content of column2 where in the row where column1 is equal to 'specific'. This method was created
     * by @anikasian for her AlJazeera module.
     *
     * @param filepath: The system.properties file path.
     * @param specifics: The String content/item in column1.
     * @param table: The table you're grabbing the info from. Must be a table of 2 String columns.
     * @param column1: First column name where you're searching 'specifics'. Must be String.
     * @param column2: Second column name where you're returning the content.
     * @return A string value of the content in column2.
     */
    public String readCol2FromSpecifiedCol1FromDB(String filepath, String specifics, String table, String column1, String column2) throws NullPointerException {
        String result = "";

        try {
            connectToSqlDatabase(filepath);
            // ex: select title(col2) from menu_titles(table) where menuname(col1)='home(specific)'
            String query = "SELECT " + column2 + " FROM " + table + " WHERE " + column1 + "=\'" + specifics + "\'";
            resultSet = connect.createStatement().executeQuery(query);
            if (resultSet.next())
                result = resultSet.getString(column2);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } catch (ClassNotFoundException cnfEx) {
            cnfEx.printStackTrace();
        } finally {
            close();
        }

        return result;
    }

    public void insertDataFromArrayListToSqlTable(String filepath, List<String> list, String tableName, String columnName)
    {
        try {
            connectToSqlDatabase(filepath);
            ps = connect.prepareStatement("DROP TABLE IF EXISTS "+ tableName);
            ps.executeUpdate();
            ps = connect.prepareStatement("CREATE TABLE " + tableName + " (" + columnName + " VARCHAR(120));");
            ps.executeUpdate();
            for(String st : list){
                ps = connect.prepareStatement("INSERT INTO " + tableName+ " ( " + columnName + " ) VALUES(?)");
                ps.setObject(1, st);
                ps.executeUpdate();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try{
            if(resultSet != null){
                resultSet.close();
            }
            if(statement != null){
                statement.close();
            }
            if(connect != null){
                connect.close();
            }
        }catch(Exception e){

        }
    }
}
