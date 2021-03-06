package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.SynchronizerDataBase;
import com.xingkaichun.helloworldblockchain.core.model.synchronizer.SynchronizerBlockDTO;
import com.xingkaichun.helloworldblockchain.core.tools.NodeTransportDtoTool;
import com.xingkaichun.helloworldblockchain.core.utils.FileUtil;
import com.xingkaichun.helloworldblockchain.core.utils.JdbcUtil;
import com.xingkaichun.helloworldblockchain.core.utils.LongUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认实现
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class SynchronizerDataBaseDefaultImpl extends SynchronizerDataBase {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizerDataBaseDefaultImpl.class);

    private static final String NODE_SYNCHRONIZE_DATABASE_DIRECT_NAME = "NodeSynchronizeDatabase";
    private static final String NODE_SYNCHRONIZE_DATABASE_File_Name = "NodeSynchronize.db";

    private String blockchainDataPath;
    private Connection connection;

    public SynchronizerDataBaseDefaultImpl(String blockchainDataPath) {
        this.blockchainDataPath = blockchainDataPath;
        init();
    }

    private void init() {
        String createTable1Sql1 = "CREATE TABLE IF NOT EXISTS NODE " +
                "(" +
                "nodeId CHAR(100) PRIMARY KEY NOT NULL," +
                "status CHAR(10)" +
                ")";
        JdbcUtil.executeSql(connection(),createTable1Sql1);

        String createTable1Sql2 = "CREATE TABLE IF NOT EXISTS DATA " +
                "(" +
                "nodeId CHAR(100) NOT NULL," +
                "blockHeight NUMERIC NOT NULL," +
                "blockDto TEXT NOT NULL," +
                "insertTime INTEGER NOT NULL" +
                ")";
        JdbcUtil.executeSql(connection(),createTable1Sql2);
    }

    @Override
    public boolean addBlockDTO(String nodeId, SynchronizerBlockDTO synchronizerBlockDTO) {

        String sql = "INSERT INTO DATA (nodeId,blockHeight,blockDto,insertTime) " +
                "VALUES (?,?,?,?);";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            preparedStatement.setString(1,nodeId);
            preparedStatement.setLong(2,synchronizerBlockDTO.getHeight());
            preparedStatement.setString(3, NodeTransportDtoTool.encode(synchronizerBlockDTO));
            preparedStatement.setLong(4,System.currentTimeMillis());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
        }
        return true;
    }

    @Override
    public long getMinBlockHeight(String nodeId) {
        String sql = "SELECT min(blockHeight) as minBlockHeight FROM DATA WHERE nodeId = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            preparedStatement.setString(1,nodeId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                long blockHeight = resultSet.getLong("minBlockHeight");
                return blockHeight;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return LongUtil.ZERO;
    }

    @Override
    public long getMaxBlockHeight(String nodeId) {
        String sql = "SELECT max(blockHeight) as maxBlockHeight FROM DATA WHERE nodeId = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            preparedStatement.setString(1,nodeId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                long blockHeight = resultSet.getLong("maxBlockHeight");
                return blockHeight;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return LongUtil.ZERO;
    }

    @Override
    public SynchronizerBlockDTO getBlockDto(String nodeId, long blockHeight) {
        String selectBlockDataSql = "SELECT * FROM DATA WHERE nodeId = ? and blockHeight=?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(selectBlockDataSql);
            preparedStatement.setString(1,nodeId);
            preparedStatement.setLong(2,blockHeight);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String stringSynchronizerBlockDTO = resultSet.getString("blockDto");
                return NodeTransportDtoTool.decodeToSynchronizerBlockDTO(stringSynchronizerBlockDTO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return null;
    }


    @Override
    public boolean hasDataTransferFinishFlag(String nodeId) {
        String sql = "SELECT * FROM NODE WHERE status = 'FINISH' and nodeId = ? limit 0,1";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            preparedStatement.setString(1,nodeId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return false;
    }

    @Override
    public String getDataTransferFinishFlagNodeId() {
        String sql = "SELECT * FROM NODE WHERE status = 'FINISH' limit 0,1";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String nodeId = resultSet.getString("nodeId");
                return nodeId;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return null;
    }

    @Override
    public List<String> getAllNodeId() {
        String sql = "SELECT * FROM NODE";
        List<String> nodeList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String nodeId = resultSet.getString("nodeId");
                nodeList.add(nodeId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return nodeList;
    }

    @Override
    public long getLastUpdateTimestamp(String nodeId) {
        String selectBlockDataSql = "SELECT insertTime FROM DATA WHERE nodeId = ? order by insertTime desc limit 0,1";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(selectBlockDataSql);
            preparedStatement.setString(1,nodeId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getLong("insertTime");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public void addDataTransferFinishFlag(String nodeId) {
        String sql1 = "DELETE FROM NODE WHERE nodeId = ?";
        PreparedStatement preparedStatement1 = null;
        String sql2 = "INSERT INTO NODE (nodeId,status) VALUES (? , ?)";
        PreparedStatement preparedStatement2 = null;
        try {
            preparedStatement1 = connection().prepareStatement(sql1);
            preparedStatement1.setString(1,nodeId);
            preparedStatement1.executeUpdate();
            preparedStatement2 = connection().prepareStatement(sql2);
            preparedStatement2.setString(1,nodeId);
            preparedStatement2.setString(2,"FINISH");
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement1);
            JdbcUtil.closeStatement(preparedStatement2);
        }
    }

    @Override
    public void clear(String nodeId) {
        String sql = "DELETE FROM DATA WHERE nodeId = ?";
        PreparedStatement preparedStatement1 = null;
        String sql2 = "DELETE FROM NODE WHERE nodeId = ?";
        PreparedStatement preparedStatement2 = null;
        try {
            preparedStatement1 = connection().prepareStatement(sql);
            preparedStatement1.setString(1,nodeId);
            preparedStatement1.executeUpdate();
            preparedStatement2 = connection().prepareStatement(sql2);
            preparedStatement2.setString(1,nodeId);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement1);
            JdbcUtil.closeStatement(preparedStatement2);
        }
    }

    private synchronized Connection connection() {
        try {
            if(connection != null && !connection.isClosed()){
                return connection;
            }
            File nodeSynchronizeDatabaseDirect = new File(blockchainDataPath,NODE_SYNCHRONIZE_DATABASE_DIRECT_NAME);
            FileUtil.mkdir(nodeSynchronizeDatabaseDirect);
            File nodeSynchronizeDatabasePath = new File(nodeSynchronizeDatabaseDirect,NODE_SYNCHRONIZE_DATABASE_File_Name);
            String jdbcConnectionUrl = JdbcUtil.getJdbcConnectionUrl(nodeSynchronizeDatabasePath.getAbsolutePath());
            connection = DriverManager.getConnection(jdbcConnectionUrl);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
