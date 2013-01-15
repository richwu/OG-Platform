/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Types;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.DbTest;

/**
 * Base tests for DbConfigMasterWorker via DbConfigMaster.
 */
public abstract class AbstractDbConfigMasterWorkerTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbConfigMasterWorkerTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  protected DbConfigMaster _cfgMaster;
  protected Instant _version1aInstant;
  protected Instant _version1bInstant;
  protected Instant _version1cInstant;
  protected Instant _version2Instant;
  protected int _totalConfigs;
  protected int _totalExternalIds;
  protected int _totalBundles;
  protected boolean _readOnly;  // attempt to speed up tests

  public AbstractDbConfigMasterWorkerTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion, databaseVersion);
    _readOnly = readOnly;
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeClass
  public void setUpClass() throws Exception {
    if (_readOnly) {
      init();
    }
  }

  @BeforeMethod
  public void setUp() throws Exception {
    if (_readOnly == false) {
      init();
    }
  }

  protected ObjectId setupTestData(Instant now) {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      _cfgMaster.setTimeSource(TimeSource.fixed(now));

      String initialValue = "initial";
      ConfigItem<String> initial = ConfigItem.of(initialValue, "some_name");      
      _cfgMaster.add(new ConfigDocument(initial));

      ObjectId baseOid = initial.getObjectId();

      //------------------------------------------------------------------------------------------------------------------
      List<ConfigDocument> firstReplacement = newArrayList();
      for (int i = 0; i < 5; i++) {
        String val = "setup_" + i;
        ConfigItem<String> item = ConfigItem.of(val, "some_name_"+i);
        ConfigDocument doc = new ConfigDocument(item);
        doc.setVersionFromInstant(now.plus(i, TimeUnit.MINUTES));
        firstReplacement.add(doc);
      }
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(1, TimeUnit.HOURS)));
      _cfgMaster.replaceVersions(baseOid.getObjectId(), firstReplacement);

      return baseOid;
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  private void init() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _cfgMaster = (DbConfigMaster) context.getBean(getDatabaseType() + "DbConfigMaster");
    
    Instant now = Instant.now();
    _cfgMaster.setTimeSource(TimeSource.fixed(now));
    _version1aInstant = now.minusSeconds(102);
    _version1bInstant = now.minusSeconds(101);
    _version1cInstant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    addExternalIds();
    addExternalIdBundles();
    _totalConfigs = 6;
  }

  private void addExternalIds() {
    FudgeMsgEnvelope env = s_fudgeContext.toFudgeMsg(ExternalId.of("A", "B"));
    byte[] bytes = s_fudgeContext.toByteArray(env.getMessage());
    String cls = ExternalId.class.getName();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _cfgMaster.getDbConnector().getJdbcTemplate();
    template.update("INSERT INTO cfg_type VALUES (?)", cls);    // insert config type string into look-up table
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig101", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig102", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig201", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig202", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalExternalIds = 3;
  }
  
  private void addExternalIdBundles() {
    FudgeMsgEnvelope env = s_fudgeContext.toFudgeMsg(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    byte[] bytes = s_fudgeContext.toByteArray(env.getMessage());
    String cls = ExternalIdBundle.class.getName();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _cfgMaster.getDbConnector().getJdbcTemplate();
    template.update("INSERT INTO cfg_type VALUES (?)", cls);    // insert config type string into look-up table
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        301, 301, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig301", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        302, 302, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig302", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        401, 401, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig401", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        402, 401, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig402", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalBundles = 3;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_readOnly == false) {
      _cfgMaster = null;
      super.tearDown();
    }
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    if (_readOnly) {
      _cfgMaster = null;
      super.tearDown();
    }
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ConfigDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1aInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1aInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig101", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert102(final ConfigDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCfg", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1bInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1bInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig102", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert201(final ConfigDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCfg", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1cInstant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1cInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig201", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert202(final ConfigDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCfg", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

}
