import com.asterdata.ncluster.sqlmr.HelpInfo;
import com.asterdata.ncluster.sqlmr.InputInfo;
import com.asterdata.ncluster.sqlmr.OutputInfo;
import com.asterdata.ncluster.sqlmr.PartitionFunction;
import com.asterdata.ncluster.sqlmr.RowFunction;
import com.asterdata.ncluster.sqlmr.RuntimeContract;
import com.asterdata.ncluster.sqlmr.data.PartitionDefinition;
import com.asterdata.ncluster.sqlmr.data.RowEmitter;
import com.asterdata.ncluster.sqlmr.data.RowIterator;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@HelpInfo(
        usageSyntax = "run_on_teradata( on ... [ partition by ... ] )",
        shortDescription = "Execute query on teradata.",
        longDescription = "",
        inputColumns = "*",
        outputColumns = "*",
        author = "aster community"
)
public final class run_on_teradata implements RowFunction, PartitionFunction
{
    private final String tdpid;
    private final String username;
    private final String password ;
    private final String query;

    public run_on_teradata(RuntimeContract contract)
            throws SQLException
    {
        tdpid = contract.useArgumentClause("tdpid").getSingleValue();
        username = contract.useArgumentClause("username").getSingleValue();
        password = contract.useArgumentClause("password").getSingleValue();
        query = contract.useArgumentClause("query").getSingleValue();

        if (contract.isExecutionMode() && !contract.isCompleted()) {
            executeQuery(tdpid, username, password, query);
        }

        InputInfo inputInfo = contract.getInputInfo();
        contract.setOutputInfo( new OutputInfo( inputInfo.getColumns() ) );
        contract.complete();
    }

    private void executeQuery(String tdpid, String username, String password, String query)
            throws SQLException
    {
        Driver driver = new com.teradata.jdbc.TeraDriver();
        String url = String.format("jdbc:teradata://%s", toIpAddress(tdpid));

        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);

        try (Connection con = driver.connect(url, props);
             Statement stmt = con.createStatement()) {
            stmt.execute(query);
        }
    }

    private String toIpAddress(String tdpid)
    {
        // This switch statement depends on your environment
        switch (tdpid.toLowerCase())
        {
            case "localhost" :
                return "127.0.0.1";
            default:
                return tdpid;
        }
    }

    public void operateOnSomeRows(RowIterator inputIterator, RowEmitter outputEmitter) { }

    public void operateOnPartition(PartitionDefinition partition, RowIterator inputIterator, RowEmitter outputEmitter) { }
}
