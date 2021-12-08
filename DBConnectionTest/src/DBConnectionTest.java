import java.sql.*;

public class DBConnectionTest {
    public static void main(String[] args){
        System.out.println("hi");
        Connection conn = null;
        try{
            String records = "";
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
            String user = "sql4456768";
            String pass = "gyFr8LHqQA";
            conn  = DriverManager.getConnection(url,user,pass);
            Statement statement = conn.createStatement();
            ResultSet resultset = statement.executeQuery("SELECT * FROM User");

            while (resultset.next()){
                records = "";
                for (int i = 2;i<=8;i++){
                    records += resultset.getString(i) + " ";
                }

            }
            System.out.println(records);
        } catch (SQLException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try{
                if (conn!=null){
                    conn.close();
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
    }
}
