package com.emc.caspian.common.account.model.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.skife.jdbi.v2.DBI;

import com.emc.caspian.ccs.account.model.AccountModel;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.mysql.MySQLAccountTable;
import com.emc.caspian.ccs.account.model.mysql.MySQLDataSource;

import static org.powermock.api.easymock.PowerMock.mockStatic;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*"})
@PrepareForTest({MySQLDataSource.class})
public final class AccountServiceTest {

  private static final Logger log = Logger.getLogger(AccountServiceTest.class);

  @Mock
  MySQLDataSource ds = new MySQLDataSource();

  @BeforeClass
  public static void setUpDb() throws Exception {

    Class.forName("org.hsqldb.jdbcDriver");
    Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:~/accounts");


    Statement stat = conn.createStatement();
    stat.executeUpdate("DROP TABLE IF EXISTS account");
    stat.executeUpdate("CREATE TABLE account ( id VARCHAR(64) NOT NULL PRIMARY KEY,state VARCHAR(16))");
    stat.executeUpdate("insert into account (id, state) values ('testingId', 'ACTIVE')");
    stat.executeUpdate("insert into account (id, state) values ('test1','ACTIVE')");

    ResultSet rs;
    rs = stat.executeQuery("select * from account");
    while (rs.next()) {
      log.info(rs.getString("id"));
    }
    stat.close();
    conn.close();
  }

  /**
   * This method will set the mock dataSource for H2 db which will be used by the model classes to access the database
   * 
   * @throws Exception
   */
  public void setupMockObjects() throws Exception {
    BasicDataSource ds = new BasicDataSource();
    ds.setUrl("jdbc:hsqldb:file:~/accounts");
    DBI connection = new DBI(ds);
    mockStatic(MySQLDataSource.class);
    EasyMock.expect(MySQLDataSource.getDataSourceAsDbiHandler()).andReturn(connection);
    PowerMock.replayAll();
  }

  @Test
  public void testAccountCreation() throws Exception {
    MySQLAccountTable tb = new MySQLAccountTable();
    AccountModel accountModel = new AccountModel();
    setupMockObjects();
    accountModel.setId("testingID");
    accountModel.setState("ACTIVE");
    DbResponse<Boolean> resp = tb.addAccount(accountModel);
    Assert.assertEquals("Successful account creation", true, resp.getResponseObj());
  }

  @Test
  public void testAccountCreationWithDuplicateEntry() throws Exception {
    MySQLAccountTable tb = new MySQLAccountTable();
    AccountModel accountModel = new AccountModel();
    setupMockObjects();
    accountModel.setId("testingId");
    accountModel.setState("ACTIVE");
    DbResponse<Boolean> resp = tb.addAccount(accountModel);
    Assert.assertEquals("Verify insertion status as false ", false, resp.getResponseObj());
  }

  @Test
  public void testGetAccount() throws Exception {
    MySQLAccountTable tb = new MySQLAccountTable();
    setupMockObjects();
    DbResponse<AccountModel> resp = tb.getAccount("testingId");
    Assert.assertEquals("Successful get of the account created in above step", "testingId", resp.getResponseObj()
        .getId());
  }

  @Test
  public void testDeleteAccount() throws Exception {
    MySQLAccountTable tb = new MySQLAccountTable();
    setupMockObjects();
    DbResponse<AccountModel> resp = tb.changeAccountState("test1", "DELETING");
    Assert.assertEquals("Verify that account deletion has de-activated the account", "DELETING", resp.getResponseObj()
        .getState());
  }

  @Test
  public void testDeleteNonExistentAccount() throws Exception {
    MySQLAccountTable tb = new MySQLAccountTable();
    setupMockObjects();
    DbResponse<AccountModel> resp = tb.changeAccountState("testing", "DELETING");
    Assert.assertEquals("Verify the response", ErrorCode.DB_RECORD_NOT_FOUND, resp.getErrorCode());
  }

  @AfterClass
  public static void tearDown() throws Exception {
    PowerMock.resetAll();
  }

}
