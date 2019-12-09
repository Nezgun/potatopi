package buildtrust;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDB {
    Connection c = null;
    public DerbyDB() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            this.c = DriverManager.getConnection("jdbc:derby:TrustDB; create=true");
            Statement s = this.c.createStatement(); //1           2               3              4                    5            6         7          8           9           10
            s.execute("CREATE TABLE TrustDB (guid int, uuid varchar(12), joincount int, names varchar(10000), wins int, losses int, trust int, mutes int, kicks int, bannedprev int)");
        } catch (ClassNotFoundException | SQLException ignored) {
        }

    }
    public void closeCon() {
        try {
            this.c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
