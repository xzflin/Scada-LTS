/*
 * (c) 2015 Abil'I.T. http://abilit.eu/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.scada_lts.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.model.pointhierarchy.PointHierarchyDataSource;
import org.scada_lts.dao.model.pointhierarchy.PointHierarchyNode;
import org.scada_lts.exception.PointHierarchyDaoException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.util.SerializationHelper;

/** 
 * DAO for point hierarchy (data points).
 * 
 * @author grzegorz bylica Abil'I.T. development team, sdt@abilit.eu
 * person supporting and coreecting translation Jerzy Piejko
 */

@Repository
public class PointHierarchyDAO {

	private static final Log LOG = LogFactory.getLog(PointHierarchyDAO.class);
	private final static int SELECT_COLUMN_INDEX_ID = 1;
	private final static int SELECT_COLUMN_INDEX_DATA = 2;
	private final static int SELECT_COLUMN_INDEX_DATA_SOURCE_ID = 3;
	private final static int SELECT_COLUMN_INDEX_DATA_SOURCE_NAME = 4;
	private final static int SELECT_COLUMN_INDEX_DATA_SOURCE_XID = 5;
	private final static int SELECT_COLUMN_INDEX_DATA_SOURCE_TYPE = 6;
	
	private final static boolean IS_NOT_FOLDER = false;
	

	// @formatter:off
		private static final String SQL = "" +
				"select "
				    + "dp.id,"
					+ "dp.data, "
					+ "dp.dataSourceId, "
					+ "ds.name, "
					+ "ds.xid, "
					+ "ds.dataSourceType "
	            + "from "
	            	+ "dataPoints dp join dataSources ds on ds.id = dp.dataSourceId ";
	             /* "select "
				    + "dp.id,"
					+ "dp.data "
	            + "from "
	            	+ "dataPoints dp " */;
			
		private static final String pointSQL = "" +
				"select "
				    + "dp.id,"
					+ "dp.data "
	            + "from "
	            	+ "dataPoints dp "
	            + "where "
	            	+ "id=?";
		
	
		private static final String deleteSQL = ""
				+ "delete "
				+ "from "
					+ "pointHierarchy "
				+ "where "
					+ "id=?";
		
		private static final String updateTitleSQL = ""
				+ "update "
					+ "pointHierarchy "
				+ "set "
					+ "name=? "
				+ "where " 
					+ "id=?";
		
		private static final String updateParentIdSQL = ""
				+ "update "
					+ "pointHierarchy "
				+ "set "
					+ "parentId=? "
				+ "where " 
					+ "id=?";
		
		private static final String updateParentIdsSQL = ""
				+ "update "
					+ "pointHierarchy "
				+ "set "
					+ "parentId=? "
				+ "where " 
					+ "parentId=?";
		
		private static final String updateParentIdPointSQL = ""
				+ "update "
					+ "dataPoints "
				+ "set " 
					+ "data=? "
				+ "where "
					+ "id=?";
		
		private static final String insertSQL = ""
				+ "insert into " 
				    + "pointHierarchy (parentId, name) "
				+ "values (?,?)";

					
	// @formatter:on
		
	public PointHierarchyDAO() {
		//
	}
		
	/**
	 * Return list of points hierarchy
	 * @return
	 */
	public List<PointHierarchyNode> getPointsHierarchy() {
		if (LOG.isTraceEnabled()) {
			LOG.trace("SQL PointsDAO");
		}
		
		try {
			
			List<PointHierarchyNode> listPointsHierarchyNode = DAO.getInstance().getJdbcTemp().query(SQL, new RowMapper<PointHierarchyNode>() {
			
				@Override
				public PointHierarchyNode mapRow(ResultSet rs, int rownumber) throws SQLException {
					PointHierarchyNode phn=null;
					try {
					  DataPointVO dp = new DataPointVO();
					  dp = (DataPointVO) SerializationHelper.readObject(rs.getBlob(SELECT_COLUMN_INDEX_DATA).getBinaryStream());
					  
					  PointHierarchyDataSource phds = new PointHierarchyDataSource();
					  phds.setId(rs.getInt(SELECT_COLUMN_INDEX_DATA_SOURCE_ID));
					  phds.setName(rs.getString(SELECT_COLUMN_INDEX_DATA_SOURCE_NAME));
					  phds.setXid(rs.getString(SELECT_COLUMN_INDEX_DATA_SOURCE_XID));
					  phds.setDataSourceType(String.valueOf(rs.getInt(SELECT_COLUMN_INDEX_DATA_SOURCE_TYPE)));
					  
					  phn = new PointHierarchyNode(
							  rs.getInt(SELECT_COLUMN_INDEX_ID),
							  dp.getPointFolderId(),
							  dp.getName(),
							  IS_NOT_FOLDER,
							  phds
							  );
					  
					} catch (Exception e) {
					  LOG.error(new PointHierarchyDaoException(e));
					}
					return phn;
				}
			});

			return listPointsHierarchyNode;
		} catch (Exception e) {
			LOG.error(new PointHierarchyDaoException(e));
		}
		return null;
    }
	
	/**
	 * Return DataPointVO on base key:id 
	 * @param id
	 * @return
	 */
	public DataPointVO getPointsHierarchy(int id) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("SQL Point");
		}
		
		List<DataPointVO> lstDataPointVO = DAO.getInstance().getJdbcTemp().query(pointSQL, new RowMapper<DataPointVO>() {
			@Override
			public DataPointVO mapRow(ResultSet rs, int rownumber) throws SQLException {
				try {
				  return (DataPointVO) SerializationHelper.readObject(rs.getBlob(SELECT_COLUMN_INDEX_DATA).getBinaryStream());
				} catch (Exception e) {
				  LOG.error(new PointHierarchyDaoException(e));
				}
				return null;
			 }
		}, new Object[]{id});

		return lstDataPointVO.get(0);
    }
	
	/**
	 * Update title
	 * @param id
	 * @param title
	 * @return
	 */
	@Transactional(readOnly = false,propagation=Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public boolean updateTitle(int id, String title) {
		int rows = DAO.getInstance().getJdbcTemp().update(updateTitleSQL, new Object[]{title,id});
		DataPointDao.cachedPointHierarchy = null;
		return rows > 0;
	}
	
	/**
	 * Update parentId for the data point
	 * @param id
	 * @param parentId
	 * @return
	 */
	@Transactional(readOnly = false,propagation=Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public boolean updateParentIdDataPoint(int id, int parentId) {
		DataPointVO dp = getPointsHierarchy(id);
		dp.setPointFolderId(parentId);
		int rows = DAO.getInstance().getJdbcTemp().update(updateParentIdPointSQL,new Object[]{dp,id});
		DataPointDao.cachedPointHierarchy = null;
		return rows > 0;
	}
	
	/**
	 * Update parentId for folder.
	 * @param id
	 * @param parentId
	 * @return
	 */
	@Transactional(readOnly = false,propagation=Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public boolean updateParentId(int id, int parentId) {
		int rows = DAO.getInstance().getJdbcTemp().update(updateParentIdSQL, new Object[]{parentId, id});
		DataPointDao.cachedPointHierarchy = null;
		return rows > 0;
	}
	
	/**
	 * Add folder
	 * @param parentId
	 * @param name
	 * @return
	 */
	@Transactional(readOnly = false,propagation=Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public int insert(int parentId, String name) {
		DAO.getInstance().getJdbcTemp().update(insertSQL, new Object[]{parentId, name.trim()});
		DataPointDao.cachedPointHierarchy = null;
		return DAO.getInstance().getId();
	}
	
	/**
	 * Delete folder
	 * @param key
	 * @return
	 */
	@Transactional(readOnly = false,propagation=Propagation.REQUIRES_NEW,isolation=Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public boolean deleteFolder(int key, int parentId) {
		if (LOG.isTraceEnabled()) {
			LOG.info("delete key:"+key);
		}
		int rows = DAO.getInstance().getJdbcTemp().update(deleteSQL, new Object[]{key});
		LOG.trace("delete rows:"+rows);
		
		int updates = DAO.getInstance().getJdbcTemp().update(updateParentIdsSQL, new Object[]{0,key});
		LOG.trace("update rows:"+updates);
		
		DataPointDao.cachedPointHierarchy = null;
		return rows > 0;
	}
	
	

}
